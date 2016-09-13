// #parseint-imports
import scala.util.Try
// #parseint-imports

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

  // #parseint-def

  def parseInt(s: String): Option[Int] = Try(s.toInt).toOption
  // #parseint-def

  val config = """
    #http-config
    # HTTP Configuration
    http {
      port=80
      host=0.0.0.0
    }

    #http-config
    http.port=${?HTTP_PORT}

    #db-config
    # Database Configuration
    db {
      url=jdbc:mysql://mydb/mytable
      user=dev
      pass=secret
    }
    #db-config
    """
}
