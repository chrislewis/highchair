package highchair.util

/**
 * Simple control over a GAE dev server instance.
 */
class DevServer(val sdkHome: String, val javaBin: String, val port: Int) {
  import scala.util.control.Exception.catching
  
  /* GAE sdk components. */
  val sdkLibPath = sdkHome + "/lib/"
  val sdkAgent = sdkLibPath + "agent/appengine-agent.jar"
  val sdkTools = sdkLibPath + "appengine-tools-api.jar"
  val sdkOverrides = sdkLibPath + "/override/appengine-dev-jdk-overrides.jar"
  val devServerClass = "com.google.appengine.tools.development.DevAppServerMain"
  
  /* Java. */
  val jvmOpts =
    "-ea" :: "-javaagent:" + sdkAgent ::
    "-cp" :: sdkTools ::
    "-Xbootclasspath/p:" + sdkOverrides :: Nil
  val javaLaunch = javaBin :: jvmOpts
  
  private var proc: Option[java.lang.Process] = None
  
  def start(warPath: String): Option[String] = {
    val appCmd = devServerClass :: "-p" :: port.toString :: warPath :: Nil
    val cmdArray = (javaLaunch ::: appCmd).toArray
    proc = Some(Runtime.getRuntime.exec(cmdArray))
    
    for {
      p <- proc
    } IO.readUntil(_ contains "server is running")(p.getErrorStream)
    
    None
  }
  
  def isRunning = proc.isDefined
  
  /**
   * Forcibly stop a running server and yield its return code on successful
   * shutdown or the error that occurred.
   */
  def stop(): Option[Either[Throwable, Int]] =
    proc.map { p =>
      p.destroy()
      catching(classOf[Exception]) andFinally(proc = None) either {
        p.waitFor()
        p.exitValue()
      }
    }
}


object DevServer {
  
  lazy val javaBin = new java.io.File(System.getProperty("java.home") + "/bin/java").getAbsolutePath
  lazy val sdk = System.getenv("APPENGINE_SDK_HOME") match {
    case null => None
    case p    => Some(new java.io.File(p).getAbsolutePath)
  }
  
  def apply(
    sdkHome: String   = sdk.getOrElse(error("Environment variable APPENGINE_SDK_HOME not set!")),
    javaHome: String  = javaBin,
    port: Int         = Port.any) =
    new DevServer(sdkHome, javaHome, port)
}
