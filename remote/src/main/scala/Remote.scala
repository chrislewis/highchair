package highchair.remote

import com.google.appengine.tools.remoteapi.{
  RemoteApiInstaller,
  RemoteApiOptions
}
import scala.util.control.Exception.catching

/**
 * @see http://code.google.com/appengine/articles/remote_api.html
 * @see http://code.google.com/appengine/docs/java/tools/remoteapi.html
 * @author Chris Lewis <chris@thegodcode.net>
 */
class Remote(server: (String, Int), user: (String, String)) {
  private val (srvr, prt) = server
  private val (usr, psswd) = user
  private val options =
    new RemoteApiOptions()
      .server(srvr, prt)
      .credentials(usr, psswd)
  
  /* Invoke a block in a remote context and clean up automatically. */
  def apply[A](block: => A): Either[Throwable, A] =
    install fold (Left(_), withInstaller(block)(_))
  
  private def install =
    catching (classOf[Exception]) either { val i = new RemoteApiInstaller; i.install(options); i }
  
  private def withInstaller[A](block: => A)(i: RemoteApiInstaller) =
    catching (classOf[Exception]) andFinally (i.uninstall) either (block)
}


object Remote {
  lazy val local = Remote("localhost" -> 8080, "admin" -> "password")
  
  def apply(server: (String, Int), user: (String, String)) =
    new Remote(server, user)
}
