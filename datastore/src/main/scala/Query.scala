package highchair.datastore

import meta._
import collection.JavaConversions.asIterable
import com.google.appengine.api.datastore.{
  DatastoreService,
  FetchOptions,
  Query => GQuery
}

case class Query[E <: Entity[E], K <: Kind[E]](
  kind:         K,
  filters:      List[Filter[E, _]] = Nil,
  sorts:        List[Sort[E, _]] = Nil,
  fetchOptions: Option[Fetch] = None) {
  
  def baseQuery = new GQuery(kind.name)
  /** Construct a native datastore query. */
  def rawQuery = 
    (baseQuery /: (filters ::: sorts)) { (q, f) => f bind q }
  
  /** Add a filter. */
  def and(f: K => Filter[E, _]) =
    copy(filters = f(kind) :: filters)
  
  def limit(l: Int) =
    copy(fetchOptions = initFetch(_.copy(limit = l)))
  def offset(o: Int) =
    copy(fetchOptions = initFetch(_.copy(skip = o)))
  
  // clearly this is general..
  def init[A](init: Option[A])(zero: => A)(f: A => A) =
    (init match {
      case None         => Some(zero)
      case p @ Some(_)  => p
    }) map f
  
  val initFetch = init(fetchOptions)(Fetch())_
  
  /** Sort ascending on some property. TODO moar types */
  def orderAsc(f: K => PropertyMapping[E, _]) =
    copy(sorts = Asc(f(kind)) :: sorts)
  /** Sort descending on some property. TODO moar types */
  def orderDesc(f: K => PropertyMapping[E, _]) =
    copy(sorts = Desc(f(kind)) :: sorts)
  
  /** Fetch a single record matching this query. */
  def fetchOne()(implicit dss: DatastoreService) = 
    limit (1) fetch() headOption
  /** Fetch only the keys of entities matching this query. More efficient. */
  def fetchKeys()(implicit dss: DatastoreService) = {
    val q = rawQuery setKeysOnly()
    asIterable(dss.prepare(q).asIterable) map (_ getKey)
  }
  /** Fetch entities matching this query, optionally providing limits and/or offsets. */
  @deprecated(message = "since 0.0.5")
  // TODO better default; clean up; what happens when offset extends the bounds?
  def fetch(limit: Int = 500, skip: Int = 0)(implicit dss: DatastoreService) = {
    val opts = FetchOptions.Builder withOffset(skip) limit(limit)
    asIterable(dss.prepare(rawQuery).asIterable(opts)) map kind.entity2Object
  }
  def fetch()(implicit dss: DatastoreService) = {
    val pq = dss.prepare(rawQuery)
    val iterable = fetchOptions match {
      case Some(opts) => pq.asIterable(opts.fetchOptions)
      case None => pq.asIterable()
    }
    asIterable(iterable).map(kind.entity2Object)
  }
    
  override def toString = //TODO don't override - TODO fetchopts
    "SELECT * FROM " + kind.name + " WHERE " +
      filters.reverse.mkString(" AND ") +
      (if (sorts == Nil) "" else " " + sorts.reverse.mkString(",")) +
      fetchOptions.map(" " + _.toGQL).getOrElse("")
}
