package highchair.datastore

import java.util.concurrent.Future
import scala.util.control.Exception.catching

/**
 * A future functor.
 */
class FutureF[A, B](val jf: Future[A], f: A => B = identity[A] _) {
  /**
   * Retrieve the value from the Future and apply the transformation `f`.
   * This will block the calling thread until the result is available.
   */
  def get(): Either[Throwable, B] =
    catching(classOf[Exception]) either(f(jf.get()))
  
  def map[C](g: B => C) = new FutureF[A, C](jf, f andThen g)
  
  override def toString = "<future functor>"
}

object FutureF {
  implicit def futureToFunctor[A](jf: Future[A]): FutureF[A, A] =
    new FutureF(jf)
}
