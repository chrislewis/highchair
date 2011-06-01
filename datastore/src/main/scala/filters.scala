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
  
  def single(filter: FO, value: A) = new Filter[E, A] {
    def bind(q: GQuery) = {
      q.addFilter(name, filter, prop.toStoredType(value))
    }
  }
  
  def multi(filter: FO, value: A*) = new Filter[E, A] {
    def bind(q: GQuery) = {
      val list = new java.util.ArrayList[Any]
      value foreach list.add
      q.addFilter(name, filter, list)
    }
  }
  
  /* Filter operations. */
  def === (value: A)  = single(FO.EQUAL, value)
  def is  (value: A)  = ===(value)
  def !== (value: A)  = single(FO.NOT_EQUAL, value)
  def not (value: A)  = !==(value)
  def >   (value: A)  = single(FO.GREATER_THAN, value)
  def >=  (value: A)  = single(FO.GREATER_THAN_OR_EQUAL, value)
  def <   (value: A)  = single(FO.LESS_THAN, value)
  def <=  (value: A)  = single(FO.LESS_THAN_OR_EQUAL, value)
  def in  (value: A*) = multi(FO.IN, value:_*)
}

/* Special case filter to add sorts while taking advantage of a common interface. */
sealed abstract class Sort[E <: Entity[E], A](val p: PropertyMapping[E, A], val direction: SD)
  extends Filter[E, A] {
  def bind(q: GQuery) = q.addSort(p.name, direction)
}
case class Asc[E <: Entity[E], A](property: PropertyMapping[E, A]) extends Sort(property, SD.ASCENDING)
case class Desc[E <: Entity[E], A](property: PropertyMapping[E, A]) extends Sort(property, SD.DESCENDING)
