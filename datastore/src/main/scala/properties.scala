package highchair.datastore.meta

import com.google.appengine.api.datastore.{
  Entity,
  Text,
  Key
}
import com.google.appengine.api.blobstore.BlobKey

/* A mapping from a Datastore type to a scala type, with methods to get from/set on an Entity. */
sealed trait Prop[A] {
  def dflt: A
  
  def toStoredType(value: A): Any = value // TODO use implicits
  
  def fromStoredType(st: Any) = st.asInstanceOf[A] // TODO use implicits
  
  def get(e: Entity, name: String) = e.getProperty(name) match {
    case null => dflt
    case raw => fromStoredType(raw)
  }
  
  def set(e: Entity, name: String, value: A) = {
    e.setProperty(name, toStoredType(value))
    e
  }
}

sealed abstract class BaseProp[A](_dflt: => A) extends Prop[A] {
  def dflt = _dflt
}

/* Properties for some core types. */
protected[meta] class BooleanProp   extends BaseProp(true)
protected[meta] class IntProp       extends BaseProp(0) { override def fromStoredType(st: Any) = st.toString.toInt }
protected[meta] class LongProp      extends BaseProp(0L)
protected[meta] class FloatProp     extends BaseProp(0f)
protected[meta] class DoubleProp    extends BaseProp(0d)
protected[meta] class StringProp    extends BaseProp("")
protected[meta] class KeyProp       extends BaseProp[Key](error("No suitable default value!"))
protected[meta] class BlobKeyProp   extends BaseProp[BlobKey](error("No suitable default value!"))

protected[meta] class DateProp      extends BaseProp(new java.util.Date)
protected[meta] class DateTimeProp  extends Prop[org.joda.time.DateTime] {
  def dflt = new org.joda.time.DateTime
  override def toStoredType(value: org.joda.time.DateTime): Any = value.toDate
  override def fromStoredType(st: Any) = new org.joda.time.DateTime(st.asInstanceOf[java.util.Date])
}

// Properties for appengine datastore Text
protected[meta] class TextProp      extends BaseProp(new Text(""))

/** Property allowing any mapped property A to be mapped to Option[A]. */
protected[meta] class OptionalProp[A](val wrapped: Prop[A]) extends BaseProp[Option[A]](None) {
  override def toStoredType(value: Option[A]) = value.getOrElse(null)
  override def fromStoredType(st: Any) = Some(wrapped.fromStoredType(st))
}

/** Property allowing any mapped property A to be mapped to List[A]. */
protected[meta] class ListProp[A](val wrapped: Prop[A]) extends BaseProp[List[A]](Nil) {
  override def toStoredType(value: List[A]) = value match {
    case Nil => null
    case xs => scala.collection.JavaConversions.asList(xs)
  }
  
  override def fromStoredType(st: Any) = st match {
    case jlist: java.util.List[_] => List(jlist.toArray:_*) map wrapped.fromStoredType
  }
}

/** Type-safe mapping for a property A of an entity E. */
class PropertyMapping[E <: highchair.datastore.Entity[E], A : Prop](
  val kind: highchair.datastore.Kind[E],
  val name: String,
  val clazz: Class[_])
  extends PropertyFilter[E, A] {
  val prop = implicitly[Prop[A]]
}

/** Aggregates mapped properties for an entity E. */
class Mapping[E <: highchair.datastore.Entity[E]](val mappings: Map[String, PropertyMapping[E, _]]) {
  def this(pm: PropertyMapping[E, _]) = this(collection.immutable.ListMap(pm.name -> pm))
  
  def ~(pm: PropertyMapping[E, _]) = new Mapping(mappings + (pm.name -> pm))
  
  lazy val classes: Seq[Class[_]] = mappings.map {
    case (name, pm) => pm.clazz
  } toList
}

