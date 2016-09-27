package models

import database.drivers.CustomPostgresDiver
import CustomPostgresDiver.api._

import com.sksamuel.elastic4s.BulkCompatibleDefinition
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.streams.RequestBuilder

import java.time.LocalDateTime

import database.drivers.CustomPostgresDiver
import play.api.libs.json.{JsValue, Json}
import slick.jdbc.GetResult

/**
 * Created by simun on 26.9.2016..
 */
case class TaxiRide(
  vendor_id: Int,
  tpep_pickup_datetime: LocalDateTime,
  tpep_dropoff_datetime: LocalDateTime,
  passenger_count: Int,
  trip_distance: Double,
  pickup_longitude: Double,
  pickup_latitude: Double,
  rate_code_id: Int,
  store_and_fwd_flag: Boolean,
  dropoff_longitude: Double,
  dropoff_latitude: Double,
  payment_type: Int,
  fare_amount: Double,
  extra: Double,
  mta_tax: Double,
  tip_amount: Double,
  tolls_amount: Double,
  improvement_surcharge: Double,
  total_amount: Double)

object TaxiRide {
  implicit val getTaxiRideResult: GetResult[TaxiRide] = GetResult { r =>
    TaxiRide(
      vendor_id = r.<<,
      tpep_pickup_datetime = r.<<,
      tpep_dropoff_datetime = r.<<,
      passenger_count = r.<<,
      trip_distance = r.<<,
      pickup_longitude = r.<<,
      pickup_latitude = r.<<,
      rate_code_id = r.<<,
      store_and_fwd_flag = r.<<,
      dropoff_longitude = r.<<,
      dropoff_latitude = r.<<,
      payment_type = r.<<,
      fare_amount = r.<<,
      extra = r.<<,
      mta_tax = r.<<,
      tip_amount = r.<<,
      tolls_amount = r.<<,
      improvement_surcharge = r.<<,
      total_amount = r.<<)
  }
}

// TaxiRide but adds a "fake_description" field.
case class TaxiRideWithDescription(
  tr: TaxiRide,
  fake_description: String)

object FullyEnrichedTaxiRide {

  import JsonTaxiFormats.fullyEnrichedTaxiRideFormat

  // EnrichedTaxiRide => Elasticsearch compatible documents
  def builder(indexName: String) = new RequestBuilder[FullyEnrichedTaxiRide] {
    def request(e: FullyEnrichedTaxiRide): BulkCompatibleDefinition = {
      index into indexName -> "taxi_rides" source jsValue2String(Json.toJson(e)(fullyEnrichedTaxiRideFormat))
    }
  }

  private[this] def jsValue2String(json: JsValue): String = json.toString()

}
// Fully 'enriched' TaxiRide - adds "fake_description" and "price_per_distance" fields.
case class FullyEnrichedTaxiRide(
  trwd: TaxiRideWithDescription,
  price_per_distance: Option[Double])

object JsonTaxiFormats {
  import play.api.libs.json._

  implicit val taxiRideFormat = Json.format[TaxiRide]
  implicit val taxiRideWithDescriptionFormat = Json.format[TaxiRideWithDescription]
  implicit val fullyEnrichedTaxiRideFormat = Json.format[FullyEnrichedTaxiRide]
}


