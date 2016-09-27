package run

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorAttributes._
import akka.stream.ActorMaterializer
import akka.stream.Supervision._
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.sksamuel.elastic4s.RichSearchHit
import models.FullyEnrichedTaxiRide
import play.api.libs.json.{JsResult, JsValue, Json}

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

/**
 * Created by simun on 26.9.2016..
 */
object BootES extends App {

  import com.sksamuel.elastic4s.ElasticDsl._
  import com.sksamuel.elastic4s.streams.ReactiveElastic._
  import database.ElasticSearch._
  import models.JsonTaxiFormats._

  implicit lazy val duration = Duration.Inf
  implicit lazy val actorSystem = ActorSystem("ESStream")
  implicit lazy val materializer = ActorMaterializer()
  implicit lazy val ec = actorSystem.dispatcher

  val publisherSource = Source.fromPublisher(esClient.publisher("nyc-taxi-rides", keepAlive = "1m"))
  val publisherSourceSearch = Source.fromPublisher(esClient.publisher(search in "nyc-taxi-rides" query "1" scroll "1m"))

  val flow: Flow[RichSearchHit, FullyEnrichedTaxiRide, NotUsed] = Flow[RichSearchHit].map[FullyEnrichedTaxiRide] { rsh =>
      val json: JsValue = Json.parse(rsh.sourceAsString)
      val fullyEnrichedTaxiRideJson: Try[JsResult[FullyEnrichedTaxiRide]] =  Try(Json.fromJson[FullyEnrichedTaxiRide](json)(fullyEnrichedTaxiRideFormat))

      fullyEnrichedTaxiRideJson match {
        case Success(jsResult) => jsResult.get
        case Failure(f) => FullyEnrichedTaxiRide(null, None)
      }

    } withAttributes(supervisionStrategy(resumingDecider))

  val sink = Sink.ignore

  val resultFromSink = publisherSource via flow alsoTo sumElementsSink[FullyEnrichedTaxiRide] runWith sink

  Await.result(resultFromSink, Duration.Inf)

  Await.result(actorSystem.terminate(), Duration.Inf)
}