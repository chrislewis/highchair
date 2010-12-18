package highchair.meta

import com.google.appengine.api.datastore.Query
import Query.{FilterOperator => FO, SortDirection => SD}

sealed trait Filter[E, A] {
  def bind(q: Query): Query
}

sealed abstract class SortDirection[E, A](val p: PropertyMapping[E, A], val direction: SD) extends Filter[E, A] {
  def bind(q: Query) = q.addSort(p.name, direction)
}

sealed case class Asc[E, A](val property: PropertyMapping[E, A]) extends SortDirection(property, SD.ASCENDING)
sealed case class Desc[E, A](val property: PropertyMapping[E, A]) extends SortDirection(property, SD.DESCENDING)

sealed class PropertyFilter[E, A](val property: PropertyMapping[E, A]) {
  
  def single(filter: FO, value: A) = new Filter[E, A] {
    def bind(q: Query) = {
      q.addFilter(property.name, filter, property.prop.translate(value))
    }
  }
  def multi(filter: FO, value: A*) = new Filter[E, A] {
    def bind(q: Query) = {
      val list = new java.util.ArrayList[Any]
      value.foreach { list.add(_) }
      q.addFilter(property.name, filter, list)
    }
  }
  def ===(value: A) = single(FO.EQUAL, value)
  def !==(value: A) = single(FO.NOT_EQUAL, value)
  def >(value: A) = single(FO.GREATER_THAN, value)
  def >=(value: A) = single(FO.GREATER_THAN_OR_EQUAL, value)
  def <(value: A) = single(FO.LESS_THAN, value)
  def <=(value: A) = single(FO.LESS_THAN_OR_EQUAL, value)
  def in(value: A*) = multi(FO.IN, value:_*)
}

object FilterOps {
  implicit def Prop2Filter[E, A](property: PropertyMapping[E, A]) = new PropertyFilter(property)
}
