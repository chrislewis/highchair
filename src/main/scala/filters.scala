package highchair.meta

import com.google.appengine.api.datastore.Query
import Query.{FilterOperator => FO, SortDirection => SD}

sealed trait Filter {
  def bind(q: Query): Query
}

sealed abstract class SortDirection[A](val p: PropertyMapping[A], val direction: SD) extends Filter {
  def bind(q: Query) = q.addSort(p.name, direction)
}

sealed case class Asc[A](val property: PropertyMapping[A]) extends SortDirection(property, SD.ASCENDING)
sealed case class Desc[A](val property: PropertyMapping[A]) extends SortDirection(property, SD.DESCENDING)

sealed class PropertyFilter[A <: AnyRef](val property: PropertyMapping[A]) {
  
  def single(filter: FO, value: A) = new Filter {
    def bind(q: Query) = {
      q.addFilter(property.name, filter, property.prop.translate(value))
    }
  }
  def multi(filter: FO, value: A*) = new Filter {
    def bind(q: Query) = {
      val list = new java.util.ArrayList[Any]
      value.foreach { list.add(_) }
      q.addFilter(property.name, FO.IN, list)
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
  implicit def Prop2Filter[A <: AnyRef](property: PropertyMapping[A]) = new PropertyFilter(property)
}
