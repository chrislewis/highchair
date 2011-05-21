package highchair.specs

import highchair.util.DevServer
import org.specs._

class AppStoreSpec(war: String) extends Specification {
  
  def configureDevServer = DevServer()
  
  val devServer = configureDevServer
  
  /* Deploy a GAE web app to a DevServer. */
  doBeforeSpec {
    devServer.start(war)
  }
  
  /* Shutdown the DevServer. */
  doAfterSpec {
    devServer.stop()
  }
}
