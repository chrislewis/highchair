package highchair.meta

import com.google.appengine.api.datastore.{Query => GQuery}
import GQuery.{FilterOperator => FO, SortDirection => SD}

sealed trait Filter[E, A] {
  def bind(q: GQuery): GQuery
}

sealed class PropertyFilter[E, A](val property: PropertyMapping[E, A]) {
  
  def single(filter: FO, value: A) = new Filter[E, A] {
    def bind(q: GQuery) = {
      q.addFilter(property.name, filter, property.prop.toStoredType(value))
    }
  }
  
  def multi(filter: FO, value: A*) = new Filter[E, A] {
    def bind(q: GQuery) = {
      val list = new java.util.ArrayList[Any]
      value foreach list.add
      q.addFilter(property.name, filter, list)
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

case class Query[E](val filters: List[Filter[E, _]], val sorts: List[SortDirection[E, _]]) {
  def &&(f: Filter[E, _]) = Query(f :: filters, sorts)
  def sort(s: SortDirection[E, _]) = Query(filters, s :: sorts)
}

/* Special case filter to add sorts while taking advantage of a common interface. */
sealed abstract class SortDirection[E, A](val p: PropertyMapping[E, A], val direction: SD)
  extends Filter[E, A] {
  def bind(q: GQuery) = q.addSort(p.name, direction)
}

object FilterOps {
  /* Sorts. Eventually these will have a more logical home, but for now don't require additional imports. */
  case class Asc[E, A](val property: PropertyMapping[E, A]) extends SortDirection(property, SD.ASCENDING)
  case class Desc[E, A](val property: PropertyMapping[E, A]) extends SortDirection(property, SD.DESCENDING)
  
  implicit def Property2Filter[E, A](pm: PropertyMapping[E, A]) = new PropertyFilter(pm)
  implicit def Filter2Query[E](f: Filter[E, _]) = Query(List(f), Nil)
}
