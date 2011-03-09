package highchair.tests

import highchair.meta.FilterOps._
import org.specs._

import com.google.appengine.api.datastore.Text

class EntitySpec extends highchair.specs.DataStoreSpec {
  
  val people = List(
    Person(None, "Erin", Some("Pate"), "Lewis", 31, new java.util.Date, Nil),
    Person(None, "Chris", Some("Aaron"), "Lewis", 29, new java.util.Date, Nil),
    Person(None, "Garrett", Some("Donald"), "Lewis", 60, new java.util.Date, List("Pop"))
  )

  val notes = List(
    Note(None, "A short note", new Text("This is a very short note.")),
    Note(None, "A long note", new Text("This note can be up to a meg in size!"))
  )
  
  doBeforeSpec {
    super.doBeforeSpec()
    people foreach Person.put
    notes foreach Note.put
  }
  
  "People queries" should {
    
    "find 3 Lewises" in {
      Person.find {
        Person.lastName === "Lewis"
      }.size must_== 3
    }
    
    "find 1 Lewis with middleName Aaron" in {
      Person.find {
        Person.firstName === "Chris" &&
        Person.lastName === "Lewis" &&
        Person.middleName === Some("Aaron")
      }.size must_== 1
    }
    
    "find 0 Lewises under age 20" in {
      Person.find {
        Person.lastName === "Lewis" &&
        Person.age < 20
      } must beEmpty
    }
    
    "find 1 Lewis over age 40" in {
      val pop = Person.find {
        Person.lastName === "Lewis" &&
        Person.age > 40
      }
      
      pop.size must_== 1
      pop.head.middleName must_== Some("Donald")
    }
    
    "find 0 Joneses" in {
      Person.find {
        Person.lastName === "Jones"
      } must beEmpty
    }
    
  }
 
  "Note queries" should {
    
    "Find both notes" in {
      val notes = Note.find {
        Note.title in ("A short note", "A long note")
      }
      notes.size must_== 2
    }

    "Find the long note with a Text prop" in {
      val long = Note.find {
        Note.title === "A long note"
      }.head

      long.details.getValue must_== "This note can be up to a meg in size!"
    }
  }
}
