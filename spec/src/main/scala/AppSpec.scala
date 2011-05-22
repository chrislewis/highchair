package highchair.specs

trait AppSpec extends org.specs.Specification {
  
  def warDirectory: String
  def configureDevServer = highchair.util.DevServer()
  
  val devServer = configureDevServer
  
  /* Deploy a GAE web app to a DevServer. */
  doBeforeSpec {
    devServer.start(warDirectory)
  }
  
  /* Shutdown the DevServer. */
  doAfterSpec {
    devServer.stop()
  }
}
