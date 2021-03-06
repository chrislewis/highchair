package highchair.util

/** Ripped from unfiltered-util. */
object Port {
  /** Finds any available port and returns it */
  def any = {
    val s = new java.net.ServerSocket(0)
    val p = s.getLocalPort
    s.close()
    p
  }
}

object IO {
  import java.io.{
    BufferedReader,
    InputStream,
    InputStreamReader
  }
  import scala.util.control.Exception.catching
  
  type Resource = { def close(): Unit }
  
  /** Control a closeable resource. */
  def loan[A, R <: Resource](r: R)(op: R => A): Either[Throwable, A] =
    catching(classOf[Exception]) andFinally(r.close()) either(op(r))
  
  /**
   * Consume input until the predicate returns true, blocking on each read.
   * TODO investigate iteratees
   */
  def readUntil(test: String => Boolean)(is: java.io.InputStream) = {
    val out = new scala.collection.mutable.ListBuffer[String]()
    
    loan(new BufferedReader(new InputStreamReader(is))) { r =>
      Iterator.continually(r.readLine).takeWhile { line =>
        line != null && ! test(line)
      } foreach(out+=)
      out.toList
    }
  }
}
