package highchair.tests

import com.google.appengine.api.datastore.KeyFactory
import highchair.meta.FilterOps._
import org.specs._

class KeySpec extends highchair.specs.DataStoreSpec {
  
  val chris = Person(None, "Chris", Some("Aaron"), "Lewis", 29, new java.util.Date, Nil)
  
  "an Entity" should {
    "have no initial key" in {
      chris.key must_== None
    }
    
    "have a key when saved" in {
      val saved = Person.put(chris)
      saved.key must_!= None
      Person.delete(saved.key.get)
    }
    
    "update if it already exists" in {
      val saved = Person.put(chris)
      Person.put(saved.copy(age = 30))
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
      val saved = Person.put(chris)
      Person.get(saved.key.get) must_== Some(saved)
    }
  }
  
}
