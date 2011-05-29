package highchair.datastore

import Query._
import highchair.datastore.meta.FilterOps._
import highchair.tests._

import org.specs._

import java.util.Date

class EntitySpec extends highchair.specs.DataStoreSpec {
  
  implicit val personKind = Person
  
  val q1 = Person where(_.lastName is "Lewis")
  val q2 = q1 and (_.firstName is "Chris")
  
  val people = List(
    Person(None, "Erin", Some("Pate"), "Lewis", 31, new Date, Nil),
    Person(None, "Chris", Some("Aaron"), "Lewis", 29, new Date, Nil),
    Person(None, "Garrett", Some("Donald"), "Lewis", 60, new Date, List("Pop"))
  )
  
  doBeforeSpec {
    super.doBeforeSpec()
    people foreach Person.put
  }
  
  "People queries" should {
    "find 3 people by last name" in {
      { Person where(_.lastName is "Lewis") fetch() size } must_== 3
    }    
    "find 1 person when limited" in {
      { Person where(_.lastName is "Lewis") fetch(limit = 1) size } must_== 1
    }
    "find 1 by first and last name" in {
      { Person where(_.lastName is "Lewis") and (_.firstName is "Chris") fetch() size } must_== 1
    }
    
  }
}
