package highchair.specs

import highchair.util.{DevServer, Port}
import org.specs._

class AppStoreSpec(war: String) extends Specification {
  
  val serverPort = Port.any
  val gae = devServer
  
  def devServer = DevServer(port = serverPort)
  
  /* Boot a GAE web app through google tooling. */
  doBeforeSpec {
    gae.start(war)
  }
  
  /* Shutdown the GAE app. */
  doAfterSpec {
    gae.stop()
  }
}
