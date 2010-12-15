package highchair.meta

import com.google.appengine.api.datastore.{DatastoreService, Entity, Query}

/* A mapping from a Datastore type to a scala type, with methods to get from/set on an Entity. */
sealed trait Prop[A] {
  def dflt: A
  def translate(value: A): Any = value
  def get(e: Entity, name: String) = e.getProperty(name) match {
    case null => dflt
    case raw => {
      raw.asInstanceOf[A]
    }
  }
  def set(e: Entity, name: String, value: A) = {
    e.setProperty(name, value)
    e
  }
}

sealed abstract class PropertyBase[A](_dflt: => A) extends Prop[A] {
  def dflt = _dflt
}
sealed abstract class OtherProperty[A](_dflt: => A, f: Any => A) extends Prop[A] {
  def dflt = _dflt
  override def get(e: Entity, name: String) = e.getProperty(name) match {
    case null => dflt
    case raw => f(raw)
  }
}

/* Properties for a few primitive types. */
class BooleanProp extends PropertyBase(true)
class IntProp extends OtherProperty(0, _.toString.toInt)
class LongProp extends PropertyBase(0L)
class FloatProp extends PropertyBase(0f)
class DoubleProp extends PropertyBase(0d)
class StringProp extends PropertyBase("")
class DateProp extends PropertyBase(new java.util.Date)

class OptionalProp[A](val wrapped: Prop[A]) extends PropertyBase[Option[A]](None) {
  override def translate(value: Option[A]) = value.getOrElse(null)
  
  override def get(e: Entity, name: String) = e.getProperty(name) match {
    case null => None
    case _ => Some(wrapped.get(e, name))
  }
  override def set(e: Entity, name: String, value: Option[A]) = {
    e.setProperty(name, translate(value))
    e
  }
}

class PropertyMapping[A : Prop](val name: String, val clazz: Class[_]) {
  val prop = implicitly[Prop[A]]
}
object PropertyMapping {
  def unapply(p: (String, PropertyMapping[_])) = Some(p._1, p._2.prop)
}

class Mapping(val mappings: Map[String, PropertyMapping[_]]) {
  def this(pm: PropertyMapping[_]) = this(collection.immutable.ListMap(pm.name -> pm))
  
  def ~(pm: PropertyMapping[_]) = new Mapping(mappings + (pm.name ->pm))
  
  lazy val clazz = mappings.map {
    case (name, pm) => pm.clazz
  }
}

/* Base trait for a "schema" of some kind E. */
import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.Entity
abstract class Kind[E : Manifest] {
  
  /* Must be lazy! */
  lazy val reflector = new highchair.poso.Reflector[E]
  
  def * : Mapping
  
  def putProp[A : Manifest](pm: PropertyMapping[_], e: E, _e: Entity) = {
    val a = reflector.field[A](e, pm.name)
    pm.prop.asInstanceOf[Prop[A]].set(_e, pm.name, a)
  }
  
  def put(e: E)(implicit dss: DatastoreService) = {
    val entity = *.mappings.foldLeft(new Entity(reflector.simpleName)) { //TODO kind variable
      case (_e, pm) => putProp(pm._2, e, _e) 
    }
    dss.put(entity)
  }
  
  def find(params: Filter*)(implicit dss: DatastoreService) = {
    val q = bindParams(params:_*)
    val ctor = reflector.constructorFor(*.clazz).get
    
    collection.JavaConversions.asIterable(dss.prepare(q).asIterable) map { e => {
      val args = (*.mappings map { case(name, pm) => pm.prop.get(e, name) }).asInstanceOf[Seq[java.lang.Object]]
      ctor.newInstance(args:_*).asInstanceOf[E]
    }} 
  }
  
  def bindParams(params: Filter*) =
    params.foldLeft(new Query(reflector.simpleName)) { //TODO
      (q, f) => f bind q
    }
  
  //def get(key: Key): Option[E] = null
  //def find(): List[E] = null
  //def apply(key: Key) = get(key) getOrElse { error("No such entity!") }
  
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
    
  //implicit def type2list[A : Prop]: ListProp[A] = new ListProp

  implicit def pm2m(pm: PropertyMapping[_]) = new Mapping(pm)
  
  /* Function which, given a type A, will yield an appropriate Prop instance via an implicit. */ 
  def property[A](name: String)(implicit p: Prop[A], m: Manifest[A]) = {
    new PropertyMapping[A](name, m.erasure)
  }
  
}
