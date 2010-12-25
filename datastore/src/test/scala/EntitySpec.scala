package highchair.tests

import highchair.meta.FilterOps._
import org.specs._

class EntitySpec extends highchair.specs.DataStoreSpec {
  
  val people = List(
    Person(None, "Erin", Some("Pate"), "Lewis", 31, new java.util.Date, Nil),
    Person(None, "Chris", Some("Aaron"), "Lewis", 29, new java.util.Date, Nil),
    Person(None, "Garrett", Some("Donald"), "Lewis", 60, new java.util.Date, List("Pop"))
  )
  
  doBeforeSpec {
    super.doBeforeSpec()
    people foreach Person.put
  }
  
  "People queries" should {
    
    "find 3 Lewises" in {
      Person.find {
        Person.lastName === "Lewis"
      }.size must_== 3
    }
    
    "find 1 Lewis with middleName Aaron" in {
      Person.find {
        Person.lastName === "Lewis"
        Person.middleName === Some("Aaron")
      }.size must_== 1
    }
    
    "find 0 Lewises under age 20" in {
      Person.find(
        Person.lastName === "Lewis",
        Person.age < 20
      ) must beEmpty
    }
    
    "find 1 Lewis over age 40" in {
      val pop = Person.find(
        Person.lastName === "Lewis",
        Person.age > 40
      )
      
      pop.size must_== 1
      pop.head.middleName must_== Some("Donald")
    }
    
    "find 0 Joneses" in {
      Person.find {
        Person.lastName === "Jones"
      } must beEmpty
    }
    
  }
  
}
