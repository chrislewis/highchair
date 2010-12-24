package highchair

import meta._
import com.google.appengine.api.datastore.{DatastoreService, Entity => GEntity, Key, Query, EntityNotFoundException}

/* Base trait for a "schema" of some kind E. */
abstract class Kind[E <: Entity[E]](implicit m: Manifest[E]) {
  
  /* Must be lazy! */
  lazy val reflector = new highchair.poso.Reflector[E]
  
  def * : Mapping[E]
  
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
    e.persistent(key)
  }
  
  def find(params: Filter[E, _]*)(implicit dss: DatastoreService) = {
    val q = bindParams(params:_*)
    collection.JavaConversions.asIterable(dss.prepare(q).asIterable) map entity2Object
  }
  
  def entity2Object(e: GEntity) = {
    val ctor = reflector.constructorFor(*.clazz).get
    val args = (*.mappings map {
      case(name, pm) => pm.prop.get(e, name)
    }).asInstanceOf[Seq[java.lang.Object]]
    val ee = ctor.newInstance(args:_*).asInstanceOf[E]
    ee.persistent(e.getKey)
  }
  
  def delete(key: Key)(implicit dss: DatastoreService) {
    dss.delete(key)
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
  
  /* Set of implicits yielding properties for our mapped primitives. */
  implicit object boolProp extends BooleanProp
  implicit object intProp extends IntProp
  implicit object longProp extends LongProp
  implicit object floatProp extends FloatProp
  implicit object doubleProp extends DoubleProp
  implicit object stringProp extends StringProp
  implicit object dateProp extends DateProp
  
  implicit def type2option[A](implicit prop: Prop[A]): OptionalProp[A] =
    new OptionalProp(prop)
    
  implicit def type2list[A](implicit prop: Prop[A]): ListProp[A] = new ListProp(prop)

  implicit def pm2m(pm: PropertyMapping[E, _]) = new Mapping(pm)
  
  /* Function which, given a type A, will yield an appropriate Prop instance via an implicit. */ 
  def property[A](name: String)(implicit p: Prop[A], m: Manifest[A]) = {
    new PropertyMapping[E, A](name, m.erasure)
  }
  
}
