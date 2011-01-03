package highchair

import meta._
import com.google.appengine.api.datastore.{DatastoreService, Entity => GEntity, Key, KeyFactory, Query, EntityNotFoundException}

/* Base trait for a "schema" of some kind E. */
abstract class Kind[E <: Entity[E]](implicit m: Manifest[E]) {
  
  lazy val reflector = new highchair.poso.Reflector[E]
  lazy val c = findConstructor
  
  def * : Mapping[E]
  
  def keyFor(id: Long) = KeyFactory.createKey(reflector.simpleName, id)
  
  def newKey: Key = new GEntity(reflector.simpleName).getKey
  
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
  
  def find(params: Filter[E, _]*)(implicit dss: DatastoreService) = {
    val q = bindParams(params:_*)
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
  
  def bindParams(params: Filter[E, _]*) =
    params.foldLeft(new Query(reflector.simpleName)) {
      (q, f) => f bind q
    }
  
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
  
  /* Set of implicits yielding properties for our mapped primitives. */
  implicit object boolProp extends BooleanProp
  implicit object intProp extends IntProp
  implicit object longProp extends LongProp
  implicit object floatProp extends FloatProp
  implicit object doubleProp extends DoubleProp
  implicit object stringProp extends StringProp
  implicit object dateProp extends DateProp
  implicit object keyProp extends KeyProp
  
  implicit def type2option[A](implicit prop: Prop[A]): OptionalProp[A] =
    new OptionalProp(prop)
    
  implicit def type2list[A](implicit prop: Prop[A]): ListProp[A] = new ListProp(prop)

  implicit def pm2m(pm: PropertyMapping[E, _]) = new Mapping(pm)
  
  /* Function which, given a type A, will yield an appropriate Prop instance via an implicit. */ 
  def property[A](name: String)(implicit p: Prop[A], m: Manifest[A]) = {
    new PropertyMapping[E, A](name, m.erasure)
  }
  
}
