package highchair.datastore.meta

import highchair.datastore.Entity
import com.google.appengine.api.datastore.{
  Query => GQuery
}
import GQuery.{
  FilterOperator => FO,
  SortDirection => SD
}

sealed trait Filter[E, A] {
  def bind(q: GQuery): GQuery
}

private[meta] trait PropertyFilter[E <: Entity[E], A] { this: PropertyMapping[E, A] =>
  
  def single(filter: FO, sym: String, value: A) = new Filter[E, A] {
    def bind(q: GQuery) =
      q.addFilter(name, filter, prop.toStoredType(value))
    /* Produce a query fragment for the string representation. */
    override def toString =
      "%s %s %s".format(PropertyFilter.this.name, sym, value)
  }
  
  def multi(filter: FO, sym: String, values: A*) = new Filter[E, A] {
    def bind(q: GQuery) = {
      val list = new java.util.ArrayList[Any]
      values foreach list.add
      q.addFilter(name, filter, list)
    }
    /* Produce a query fragment for the string representation. */
    override def toString =
      "%s %s (%s)".format(PropertyFilter.this.name, sym, values.mkString(","))
  }
  
  /* Filter operations. */
  def === (value: A)  = single(FO.EQUAL, "=", value)
  def is  (value: A)  = ===(value)
  def !== (value: A)  = single(FO.NOT_EQUAL, "!=", value)
  def not (value: A)  = !==(value)
  def >   (value: A)  = single(FO.GREATER_THAN, ">", value)
  def >=  (value: A)  = single(FO.GREATER_THAN_OR_EQUAL, ">=", value)
  def <   (value: A)  = single(FO.LESS_THAN, "<", value)
  def <=  (value: A)  = single(FO.LESS_THAN_OR_EQUAL, "<=", value)
  def in  (value: A*) = multi(FO.IN, "in", value:_*)
}

/* Special case filter to add sorts while taking advantage of a common interface. */
sealed abstract class Sort[E <: Entity[E], A](val p: PropertyMapping[E, A], val direction: SD)
  extends Filter[E, A] {
  def bind(q: GQuery) = q.addSort(p.name, direction)
  override def toString =
    "ORDER BY " + p.name + " " +
      (if (direction == SD.ASCENDING) "ASC" else "DESC")
}
case class Asc[E <: Entity[E], A](property: PropertyMapping[E, A]) extends Sort(property, SD.ASCENDING)
case class Desc[E <: Entity[E], A](property: PropertyMapping[E, A]) extends Sort(property, SD.DESCENDING)
