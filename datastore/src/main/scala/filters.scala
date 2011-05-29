package highchair.datastore.meta

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

private[meta] trait PropertyFilter[E <: highchair.datastore.Entity[E], A] { this: PropertyMapping[E, A] =>
  
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
sealed abstract class Sort[E <: highchair.datastore.Entity[E], A](val p: PropertyMapping[E, A], val direction: SD)
  extends Filter[E, A] {
  def bind(q: GQuery) = q.addSort(p.name, direction)
}

case class Asc[E <: highchair.datastore.Entity[E], A](val property: PropertyMapping[E, A]) extends Sort(property, SD.ASCENDING)
case class Desc[E <: highchair.datastore.Entity[E], A](val property: PropertyMapping[E, A]) extends Sort(property, SD.DESCENDING)

@deprecated("use datastore.Query", "0.0.4")
case class Query[E <: highchair.datastore.Entity[E]](val filters: List[Filter[E, _]], val sorts: List[Sort[E, _]]) {
  def &&(f: Filter[E, _]) = Query(f :: filters, sorts)
  def sort(s: Sort[E, _]) = Query(filters, s :: sorts)
}
