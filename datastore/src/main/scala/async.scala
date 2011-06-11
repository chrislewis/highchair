package highchair.datastore

import com.google.appengine.api.datastore.{
  AsyncDatastoreService,
  Key,
  Entity => GEntity
}

class AsyncKind[E <: Entity[E]](k: Kind[E]) {
  import FutureF._ // Future => FutureF
  def get(key: Key)(implicit adss: AsyncDatastoreService): FutureF[GEntity, E] =
    adss.get(key).map(k.entity2Object)
}

/** Mix into a Kind for asynchronous support. */
trait Async[E <: Entity[E]] { this: Kind[E] =>
  lazy val async = new AsyncKind(this)
}

class AsyncKindWrapper[E <: Entity[E]](k: Kind[E]) {
  val async = new AsyncKind(k)
}

object Async {
  def apply[E <: Entity[E]](k: Kind[E]) = new AsyncKindWrapper(k)
  implicit def kindToAsync[E <: Entity[E]](k: Kind[E]) = Async(k)
}
