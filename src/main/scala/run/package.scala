import akka.stream.scaladsl.Sink

/**
 * Created by simun on 27.9.2016..
 */
package object run {

  def sumElementsSink[T] = Sink.fold[Int, T](0) { (sum, _) =>
    val newSum = sum + 1

    if (newSum % 5000 == 0) {
      print(s"\rCount: $newSum")
    }

    newSum
  }
}
