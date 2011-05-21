package highchair.util

import dispatch._
import dispatch.Http._
import org.specs._

class DevServerSpec extends Specification {
  
  val server = DevServer()
  val guestbookApp = DevServer.sdk.map(_ + "/demos/guestbook/war/").getOrElse(error("No SDK!"))
  val host = :/ ("localhost", server.port)
  
  doBeforeSpec {
    server.start(guestbookApp)
  }
  
  doAfterSpec {
    server.stop()
  }
  
  "a DevServer should serve the guestbook app" in {
    Http x(host as_str) {
      case (s, ss, sss, out) => Some(s -> out().contains("guestbook.jsp"))
      case _ => None
    } must_== Some(200, true)
  }
  
}
