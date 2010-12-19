package highchair.meta

import com.google.appengine.api.datastore.Entity

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
    e.setProperty(name, translate(value))
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

class ListProp[A](val wrapped: Prop[A]) extends PropertyBase[List[A]](Nil) {
  import java.util.Collections
  override def translate(value: List[A]) = value match {
    case Nil => Collections.emptyList
    case xs => scala.collection.JavaConversions.asList(xs)
  }
  
  override def get(e: Entity, name: String) = e.getProperty(name) match {
    case null => Nil
    case _ => {
      val jlist = e.getProperty(name).asInstanceOf[java.util.List[A]]
      List(jlist.toArray:_*).map(_.asInstanceOf[A]) // FIXME we need another translate here
    }
  }
}

class PropertyMapping[E, A : Prop](val name: String, val clazz: Class[_]) {
  val prop = implicitly[Prop[A]]
}

class Mapping[E](val mappings: Map[String, PropertyMapping[E, _]]) {
  def this(pm: PropertyMapping[E, _]) = this(collection.immutable.ListMap(pm.name -> pm))
  
  def ~(pm: PropertyMapping[E, _]) = new Mapping(mappings + (pm.name ->pm))
  
  lazy val clazz = mappings.map {
    case (name, pm) => pm.clazz
  }
}

