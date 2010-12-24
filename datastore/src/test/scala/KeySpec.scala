package highchair.tests

import com.google.appengine.api.datastore.KeyFactory
import highchair.meta.FilterOps._
import org.specs._

class KeySpec extends highchair.specs.DataStoreSpec {
  
  val chris = Person("Chris", Some("Aaron"), "Lewis", 29, new java.util.Date, Nil)
  
  "an Entity" should {
    "have no initial key" in {
      chris.key must_== None
    }
    
    "have a key when saved" in {
      Person.put(chris) must_!= None
      chris.key must_!= None
      Person.delete(chris.key.get)
    }
    
    "update if it already exists" in {
      Person.put(chris)
      Person.put(chris.copy(age = 30).persistent(chris.key.get))
      Person.find {
        Person.lastName === "Lewis"
      }.size must_== 1
    }
  }
  
  "a query by Key" should {
    "find None when no results exist" in {
      Person.get(KeyFactory.createKey("heart", "1")) must_== None
    }
    
    "find Some when an entity matches by key" in {
      val key = Person.put(chris).key
      Person.get(key.get) must_== Some(chris)
    }
  }
  
}
