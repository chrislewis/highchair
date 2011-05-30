package highchair.datastore

import meta._
import meta.{ Query => Query3 }
import com.google.appengine.api.datastore.{
  DatastoreService,
  Entity => GEntity,
  Key,
  KeyFactory,
  Query => GQuery,
  EntityNotFoundException
}

/* Base trait for a "schema" of some kind E. */
abstract class Kind[E <: Entity[E]](implicit m: Manifest[E]) extends PropertyImplicits {
  
  lazy val reflector = new poso.Reflector[E]
  lazy val c = findConstructor
  
  def * : Mapping[E]
  
  def keyFor(id: Long) = KeyFactory.createKey(reflector.simpleName, id)
    
  def childOf(ancestor: Key): Key = new GEntity(reflector.simpleName, ancestor).getKey
  
  def putProp[A : Manifest](pm: PropertyMapping[E, _], e: E, _e: GEntity) = {
    val a = reflector.field[A](e, pm.name)
    pm.prop.asInstanceOf[Prop[A]].set(_e, pm.name, a)
  }
  
  def put(e: E)(implicit dss: DatastoreService) = {
    val entity = e.key match {
      case Some(k) => new GEntity(k)
      case None => new GEntity(reflector.simpleName)
    }
    
    val key = dss.put(*.mappings.foldLeft(entity) {
      case (_e, pm) => putProp(pm._2, e, _e) 
    })
    
    entity2Object(entity)
  }
  
  @deprecated("use Kind#where instead - since 0.0.4")
  def find(query: Query3[E])(implicit dss: DatastoreService) = {
    val q = bindParams(new GQuery(reflector.simpleName), (query.filters ::: query.sorts):_*)
    collection.JavaConversions.asIterable(dss.prepare(q).asIterable) map entity2Object
  }
  
  def delete(e: E)(implicit dss: DatastoreService) {
    e.key.map(dss.delete(_))
  }
  
  def get(key: Key)(implicit dss: DatastoreService) = 
    try {
      val _e = dss.get(key)
      Some(entity2Object(_e))
    } catch {
      case e: EntityNotFoundException => None
    }
  
  @deprecated("use datastore.query - since 0.0.4")
  def bindParams(q: GQuery, params: Filter[E, _]*) =
    (q /: params) { (q, f) => f bind q }
  
  /**/
  def where[A](f: this.type => meta.Filter[E, A]) =
    Query[E, this.type](this, f(this) :: Nil, Nil)
  
  implicit def Kind2Query[K <: Kind[E]](k: K) =
    Query[E, this.type](this, Nil, Nil)
  /**/
  
  private def findConstructor = 
    reflector.findConstructor { c =>
      val p_types = c.getParameterTypes.toList
      val g_types = c.getGenericParameterTypes.toList
      p_types.containsSlice(*.classes) &&
      findKey(p_types.zip(g_types)).isDefined
    } getOrElse error("No suitable constructor could be found!")
  
  private def findKey(types: Seq[(Class[_], java.lang.reflect.Type)]) = 
    types.find {
      case(c, t) =>
        c == classOf[Option[_]] &&
        t.isInstanceOf[java.lang.reflect.ParameterizedType] &&
        t.asInstanceOf[java.lang.reflect.ParameterizedType].getActualTypeArguments.head == classOf[Key]
    }
  
  def entity2Object(e: GEntity) = {
    val args = Some(e.getKey) :: (*.mappings map {
      case(name, pm) => pm.prop.get(e, name)
    }).toList.asInstanceOf[List[java.lang.Object]]
    c.newInstance(args:_*).asInstanceOf[E]
  }
  
  implicit def pm2m(pm: PropertyMapping[E, _]) = new Mapping(pm)
  
  /* Function which, given a type A, will yield an appropriate Prop instance via an implicit. */ 
  def property[A](name: String)(implicit p: Prop[A], m: Manifest[A]) = {
    new PropertyMapping[E, A](this, name, m.erasure)
  }
  
  @deprecated("use datastore.query - since 0.0.4")
  implicit def Filter2Query[E <: highchair.datastore.Entity[E]](f: Filter[E, _]) = Query3(List(f), Nil)
}
