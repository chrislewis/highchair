package highchair.datastore

import meta._

import com.google.appengine.api.datastore.{
  DatastoreService,
  FetchOptions,
  Key,
  Query => GQuery
}

case class Query[E <: Entity[E], K <: Kind[E]](
  kind:     K,
  filters:  List[Filter[E, _]],
  sorts:    List[Sort[E, _]]) {
  
  def rawQuery =
    bindParams(new GQuery(kind.reflector.simpleName), (filters ::: sorts):_*)
  
  def and(f: K => Filter[E, _]) =
    copy(filters = f(kind) :: filters)
  
  def orderAsc(f: K => PropertyMapping[E, _]) =
    copy(sorts = Asc(f(kind)) :: sorts)
  
  def orderDesc(f: K => PropertyMapping[E, _]) =
    copy(sorts = Desc(f(kind)) :: sorts)
  
  def fetchOne()(implicit dss: DatastoreService) = 
    fetch() headOption
  
  def fetchKeys()(implicit dss: DatastoreService) = {
    val q = rawQuery setKeysOnly()
    collection.JavaConversions.asIterable(dss.prepare(q).asIterable) map (_ getKey)
  }
    
  // TODO better default + clean up
  def fetch(limit: Int = 500, offset: Int = 0)(implicit dss: DatastoreService) = {
    val opts = FetchOptions.Builder withOffset(offset) limit(limit)
    collection.JavaConversions.asIterable(dss.prepare(rawQuery).asIterable(opts)) map kind.entity2Object
  }
  
  private def bindParams(q: GQuery, params: Filter[E, _]*) =
    (q /: params) { (q, f) => f bind q }
}
