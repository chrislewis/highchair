package highchair

import meta._
import com.google.appengine.api.datastore.{DatastoreService, Entity, Key, Query}

/* Base trait for a "schema" of some kind E. */
abstract class Kind[E : Manifest] {
  
  /* Must be lazy! */
  lazy val reflector = new highchair.poso.Reflector[E]
  
  def * : Mapping[E]
  
  def putProp[A : Manifest](pm: PropertyMapping[E, _], e: E, _e: Entity) = {
    val a = reflector.field[A](e, pm.name)
    pm.prop.asInstanceOf[Prop[A]].set(_e, pm.name, a)
  }
  
  def put(e: E)(implicit dss: DatastoreService) = {
    val entity = *.mappings.foldLeft(new Entity(reflector.simpleName)) {
      case (_e, pm) => putProp(pm._2, e, _e) 
    }
    dss.put(entity)
  }
  
  def find(params: Filter[E, _]*)(implicit dss: DatastoreService) = {
    val q = bindParams(params:_*)
    val ctor = reflector.constructorFor(*.clazz).get
    
    collection.JavaConversions.asIterable(dss.prepare(q).asIterable) map { e => {
      val args = (*.mappings map { case(name, pm) => pm.prop.get(e, name) }).asInstanceOf[Seq[java.lang.Object]]
      ctor.newInstance(args:_*).asInstanceOf[E]
    }} 
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
