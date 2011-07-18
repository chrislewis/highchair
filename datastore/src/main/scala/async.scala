package highchair.datastore

import com.google.appengine.api.datastore.{
  AsyncDatastoreService,
  Key,
  Entity => GEntity
}

/**
 * Support for executing non-blocking queries through the datastore's [[http://code.google.com/appengine/docs/java/datastore/async.html asynchrnonous API]].
 * The asynchronous API enables high-performance parallel execution of queries.
 */
class AsyncKind[E <: Entity[E]](k: Kind[E]) {
  import FutureF._ // Future => FutureF
  def get(key: Key)(implicit adss: AsyncDatastoreService): FutureF[GEntity, E] =
    adss.get(key).map(k.entity2Object)
}

/** Mixin to [[highchair.datastore.Kind]] for asynchronous support. */
trait Async[E <: Entity[E]] { this: Kind[E] =>
  lazy val async = new AsyncKind(this)
}

/** Pimp for adding asynchronous support to [[highchair.datastore.Kind]]. */
class AsyncKindWrapper[E <: Entity[E]](k: Kind[E]) {
  val async = new AsyncKind(k)
}

/**
 * Implicit enabler for pimped asynchronous support. This is convenient
 * if the [[highchair.datastore.Kind]] inquestion is out of your control, but
 * it's better to wire asynchrony into the object to avoid additional allocations.
 */
object Async {
  def apply[E <: Entity[E]](k: Kind[E]) = new AsyncKindWrapper(k)
  implicit def kindToAsync[E <: Entity[E]](k: Kind[E]) = Async(k)
}
