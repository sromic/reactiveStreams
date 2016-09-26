import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Flow, Source}
import models.{FullyEnrichedTaxiRide, TaxiRideWithDescription, TaxiRide}

import database.drivers.CustomPostgresDiver.api._

import com.sksamuel.elastic4s.streams.ReactiveElastic._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Future, Await, Promise }
import scala.concurrent.duration._

/**
 * Created by simun on 26.9.2016..
 */
object Boot extends App {

  import database.Db._
  import database.ElasticSearch._
  import services.TaxiRideService._

  implicit lazy val system = ActorSystem("reactive-streams")
  implicit lazy val materializer = ActorMaterializer()

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

  def sumElementsSink[T] = Sink.fold[Int, T](0) { (sum, _) =>
    val newSum = sum + 1

    if (newSum % 5000 == 0) {
      print(s"\rCount: $newSum")
    }

    newSum
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
      .via(addDescriptionFlow(fakeApiCall))
      .via(addPricePerDistanceFlow)
      .alsoTo(sumElementsSink)
      .runWith(esSink)

    p.future
  }

  Await.result(bulkInsertToES, Duration.Inf)

  Await.result(system.terminate(), Duration.Inf)
}



