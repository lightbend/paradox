object Multiple {
  // #multiple
  import scala.concurrent.duration._

  // #multiple

  // just here to make the code look nice
  private class Method

  // #multiple-something-else
  private class SomethingElse
  // #multiple-something-else

  // #multiple
  case class Measurement(method: Method, duration: Duration)
  // #multiple
}