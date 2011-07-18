package highchair.datastore.meta

trait PropertyImplicits {
  /* Set of implicits yielding properties for our mapped primitives. */
  implicit object booleanProperty   extends BooleanProperty
  implicit object intProperty       extends IntProperty
  implicit object longProperty      extends LongProperty
  implicit object floatProperty     extends FloatProperty
  implicit object doubleProperty    extends DoubleProperty
  implicit object stringProperty    extends StringProperty
  implicit object dateProperty      extends DateProperty
  implicit object dateTimeProperty  extends DateTimeProperty
  implicit object keyProperty       extends KeyProperty
  implicit object blobKeyProperty   extends BlobKeyProperty
  implicit object textProperty      extends TextProperty

  implicit def type2option[A](implicit property: Property[A]): OptionalProperty[A] =
    new OptionalProperty(property)
    
  implicit def type2list[A](implicit property: Property[A]): ListProperty[A] =
    new ListProperty(property)
}
