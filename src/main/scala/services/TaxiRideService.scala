package services

import models.{TaxiRideWithDescription, TaxiRide}

import scala.concurrent.Future

/**
 * Created by simun on 26.9.2016..
 */
object TaxiRideService {

  def fakeApiCall(tr: TaxiRide): Future[TaxiRideWithDescription] = {
    Future.successful {
      TaxiRideWithDescription(
        tr = tr,
        fake_description = "Fake computation from API call"
      )
    }
  }

  def pricePerDistanceRation(totalAmount: Double, tripDistance: Double): Option[Double] = {
    tripDistance <= 0d match {
      case true => None
      case false => Some(totalAmount / tripDistance)
    }
  }
}
