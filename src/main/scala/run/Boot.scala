package run

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorAttributes._
import akka.stream.ActorMaterializer
import akka.stream.Supervision._
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.sksamuel.elastic4s.streams.ReactiveElastic._
import database.drivers.CustomPostgresDiver.api._
import models.{FullyEnrichedTaxiRide, TaxiRide, TaxiRideWithDescription}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}

/**
 * Created by simun on 26.9.2016..
 */
object Boot extends App {

  import database.Db._
  import database.ElasticSearch._
  import services.TaxiRideService._

  implicit lazy val system = ActorSystem("reactive-streams")
  implicit lazy val materializer = ActorMaterializer()
  implicit lazy val ec = system.dispatcher

  val allTaxiData = sql"SELECT * FROM nyc_taxi_data;".as[TaxiRide]

  val taxiRideSource: Source[TaxiRide, NotUsed] = Source.fromPublisher[TaxiRide] {
    db.stream[TaxiRide] {
      allTaxiData.transactionally.withStatementParameters(fetchSize = 5000)
    }
  }

  def addDescriptionFlow(f: TaxiRide => Future[TaxiRideWithDescription]): Flow[TaxiRide, TaxiRideWithDescription, NotUsed] = Flow[TaxiRide]
    .mapAsync(parallelism = 8) {
      tr => f(tr)
    }

  val addPricePerDistanceFlow: Flow[TaxiRideWithDescription, FullyEnrichedTaxiRide, NotUsed] =
    Flow[TaxiRideWithDescription].map { trwd =>
      val pricePerDistance = pricePerDistanceRation(trwd.tr.total_amount, trwd.tr.trip_distance)

      FullyEnrichedTaxiRide(trwd, pricePerDistance)
    }

  def bulkInsertToES: Future[Unit] = {
    val p = Promise[Unit]()

    val esSink = Sink.fromSubscriber {
      esClient.subscriber[FullyEnrichedTaxiRide](
        batchSize = 5000,
        completionFn = { () => p.success(()); ()},
        errorFn = { (t: Throwable) => p.failure(t); () })(FullyEnrichedTaxiRide.builder("nyc-taxi-rides"), system)
    }

    taxiRideSource
      .via(addDescriptionFlow(fakeApiCall)).withAttributes(supervisionStrategy(resumingDecider))
      .via(addPricePerDistanceFlow).withAttributes(supervisionStrategy(resumingDecider))
      .alsoTo(sumElementsSink)
      .runWith(esSink)

    p.future
  }

  Await.result(bulkInsertToES, Duration.Inf)

  Await.result(system.terminate(), Duration.Inf)
}