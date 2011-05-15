package highchair.remote

import com.google.appengine.tools.remoteapi.{
  RemoteApiInstaller,
  RemoteApiOptions
}
import scala.util.control.Exception.catching

/**
 * val remote = Remote("localhost" -> 8080, "user@foo.com" -> "foopass")
 * val remoteSave = remote {
 *   val ds = DatastoreServiceFactory.getDatastoreService
 *   ds.put(new Entity("RemoteEntity"))
 * }
 *
 * Remote#apply is a "loaner," so the remote options are properly
 * cleaned up when the block terminates. The result of apply is
 * an Either[Throwable, A]: a caught exception, if any, or some
 * A when succssful. In the previous example. remoteSave is a
 * Right(Key), as the put opertion results in an entity Key
 * (when successful).
 *
 * @see http://code.google.com/appengine/articles/remote_api.html
 * @see http://code.google.com/appengine/docs/java/tools/remoteapi.html
 * @author Chris Lewis <chris@thegodcode.net>
 */
case class Remote(server: (String, Int), user: (String, String)) {
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



