package highchair.datastore.meta

import com.google.appengine.api.datastore.{
  Entity,
  Text,
  Key
}
import com.google.appengine.api.blobstore.BlobKey

/* A mapping from a Datastore type to a scala type, with methods to get from/set on an Entity. */
@annotation.implicitNotFound(msg = "Property instance not found for ${A}!")
trait Property[A] {
  /** The default value for this property. */
  def dflt: A
  /** Convert a Scala type to its persistable representation. */
  def toStoredType(value: A): Any = value
  /** Convert a persistable type to its Scala representation. */
  def fromStoredType(st: Any) = st.asInstanceOf[A]
  /** Get a mapped property value from a raw Entity. */
  def get(e: Entity, name: String) = e.getProperty(name) match {
    case null => dflt
    case raw => fromStoredType(raw)
  }
  /** Set a mapped property value on a raw Entity. */
  def set(e: Entity, name: String, value: A) = {
    e.setProperty(name, toStoredType(value))
    e
  }
}

abstract class BaseProperty[A](_dflt: => A) extends Property[A] {
  def dflt = _dflt
}

/* Properties for some core types. */
protected[meta] class BooleanProperty extends BaseProperty(true)
protected[meta] class IntProperty     extends BaseProperty(0) { override def fromStoredType(st: Any) = st.toString.toInt }
protected[meta] class LongProperty    extends BaseProperty(0L)
protected[meta] class FloatProperty   extends BaseProperty(0f)
protected[meta] class DoubleProperty  extends BaseProperty(0d)
protected[meta] class StringProperty  extends BaseProperty("")
protected[meta] class KeyProperty     extends BaseProperty[Key](error("No suitable default value!"))
protected[meta] class BlobKeyProperty extends BaseProperty[BlobKey](error("No suitable default value!"))

protected[meta] class DateProperty      extends BaseProperty(new java.util.Date)
protected[meta] class DateTimeProperty  extends Property[org.joda.time.DateTime] {
  def dflt = new org.joda.time.DateTime
  override def toStoredType(value: org.joda.time.DateTime): Any = value.toDate
  override def fromStoredType(st: Any) = new org.joda.time.DateTime(st.asInstanceOf[java.util.Date])
}

// Properties for appengine datastore Text
protected[meta] class TextProperty      extends BaseProperty(new Text(""))

/** Property allowing any mapped property A to be mapped to Option[A]. */
protected[meta] class OptionalProperty[A](val wrapped: Property[A]) extends BaseProperty[Option[A]](None) {
  override def toStoredType(value: Option[A]) = value.getOrElse(null)
  override def fromStoredType(st: Any) = Some(wrapped.fromStoredType(st))
}

/** Property allowing any mapped property A to be mapped to List[A]. */
protected[meta] class ListProperty[A](val wrapped: Property[A]) extends BaseProperty[List[A]](Nil) {
  override def toStoredType(value: List[A]) = value match {
    case Nil => null
    case xs => scala.collection.JavaConversions.asList(xs)
  }
  
  override def fromStoredType(st: Any) = st match {
    case jlist: java.util.List[_] => List(jlist.toArray:_*) map wrapped.fromStoredType
  }
}

/** Type-safe mapping for a property A of an entity E. */
class PropertyMapping[E <: highchair.datastore.Entity[E], A : Property](
  val kind: highchair.datastore.Kind[E],
  val name: String,
  val clazz: Class[_])
  extends PropertyFilter[E, A] {
  val prop = implicitly[Property[A]]
}
