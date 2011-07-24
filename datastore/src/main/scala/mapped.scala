package highchair.datastore.meta

import highchair.datastore.{Entity, Kind}

class AutoMapping[E <: Entity[E], A](
  val kind: Kind[E],
  val property: Property[A],
  val clazz: Class[_],
  val fieldName: Option[String] = None) {
  
  def as(name: String) =
    new PropertyMapping[E, A](kind, fieldName.getOrElse(name), clazz)(property)
}
