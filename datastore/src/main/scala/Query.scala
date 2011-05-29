package highchair.datastore

import meta._

import com.google.appengine.api.datastore.{
  DatastoreService,
  Entity => GEntity,
  FetchOptions,
  Query => GQuery
}

case class Query[E <: Entity[E], K <: Kind[E]](
  kind:     K,
  filters:  List[Filter[E, _]],
  sorts:    List[Sort[E, _]]) {
  
  def and(f: K => Query[E, K]) = f(kind) match {
    case Query(_, fs, ss) =>
      Query(kind, fs ::: filters, ss ::: sorts)
  }
  
  def orderAsc[A](p: PropertyMapping[E, A]) =
    Query(kind, filters, FilterOps.Asc(p) :: sorts)
  
  def orderDesc[A](p: PropertyMapping[E, A]) =
    Query(kind, filters, FilterOps.Desc(p) :: sorts)
  
  def fetchOne()(implicit dss: DatastoreService) = 
    fetch() headOption
  
  def fetch(offset: Int = 0, limit: Int = 500)(implicit dss: DatastoreService) = { //TODO better default
    val q = bindParams(new GQuery(kind.reflector.simpleName), (filters ::: sorts):_*)
    val opts = FetchOptions.Builder withOffset(offset) limit(limit)
    collection.JavaConversions.asIterable(dss.prepare(q).asIterable(opts)) map kind.entity2Object
  }
  
  private def bindParams(q: GQuery, params: Filter[E, _]*) =
    (q /: params) { (q, f) => f bind q }
}

object Query { //TODO saner implicitry
  implicit def Filter2Query[E <: Entity[E], K <: Kind[E]](f: Filter[E, _])(implicit kind: K) =
    Query(kind, List(f), Nil)
}
