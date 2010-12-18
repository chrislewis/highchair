package highchair.tests

import org.specs._
import com.google.appengine.api.datastore._
import com.google.appengine.tools.development.testing._

class EntitySpec extends Specification {
  
  val helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig)
  
  doBeforeSpec { helper.setUp }
  doAfterSpec { helper.tearDown }
  
  implicit val ds = DatastoreServiceFactory.getDatastoreService
  val p = new Person("Chris", Some("Aaron"), "Lewis", 29, new java.util.Date, List("bill", "andy"))
  
  "a person" should {
    "have a mapped firstName" in {
      Person.firstName.name must_== "firstName"
      Person.put(p)
    }

    "be findable" in {
      import highchair.meta.FilterOps._
      val people = Person.find (
        Person.firstName === "Chris",
        Person.middleName === Some("Aaron"),
        Person.lastName === "Lewis",
        Person.age <= 29
      )
      people.size must_== 1
      people.head.aliases must_== List("bill", "andy")
    }
  }
  
}
