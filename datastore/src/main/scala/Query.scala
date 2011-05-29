package highchair.datastore

import meta._

import com.google.appengine.api.datastore.{
  DatastoreService,
  FetchOptions,
  Query => GQuery
}

case class Query[E <: Entity[E], K <: Kind[E]](
  kind:     K,
  filters:  List[Filter[E, _]],
  sorts:    List[Sort[E, _]]) {
  
  def and(f: K => Filter[E, _]) =
    Query(kind, f(kind) :: filters, sorts)
  
  def orderAsc(f: K => PropertyMapping[E, _]) =
    Query(kind, filters, Asc(f(kind)) :: sorts)
  
  def orderDesc(f: K => PropertyMapping[E, _]) =
    Query(kind, filters, Desc(f(kind)) :: sorts)
  
  def fetchOne()(implicit dss: DatastoreService) = 
    fetch() headOption
  
  // TODO better default + clean up
  def fetch(offset: Int = 0, limit: Int = 500)(implicit dss: DatastoreService) = {
    val q = bindParams(new GQuery(kind.reflector.simpleName), (filters ::: sorts):_*)
    val opts = FetchOptions.Builder withOffset(offset) limit(limit)
    collection.JavaConversions.asIterable(dss.prepare(q).asIterable(opts)) map kind.entity2Object
  }
  
  private def bindParams(q: GQuery, params: Filter[E, _]*) =
    (q /: params) { (q, f) => f bind q }
}
