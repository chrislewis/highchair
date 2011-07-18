package highchair.datastore.meta

import highchair.datastore.{Entity, Kind}

class AutoMapping[E <: Entity[E], A](
  val kind: Kind[E],
  val property: Property[A],
  val clazz: Class[_]) {
  
  def as(name: String) =
    new PropertyMapping[E, A](kind, name, clazz)(property)
}
