package highchair.datastore.meta

import highchair.datastore.{Entity, Kind}

class AutoMapping[E <: Entity[E], A](
  val kind: Kind[E],
  val prop: Prop[A],
  val clazz: Class[_]) {
  def as(name: String) = new PropertyMapping[E, A](kind, name, clazz)(prop)
}
