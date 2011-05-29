package highchair.datastore

import highchair.tests.Person
import org.specs._
import com.google.appengine.api.datastore.Key

class KeysOnlySpec extends highchair.specs.DataStoreSpec {
  doBeforeSpec {
    super.doBeforeSpec()
    Person.testSet foreach Person.put
  }
  
  "Keys-only query should fetch keys, only" in {
    (Person where(_.lastName is "Lewis") fetchKeys() headOption)
      .map(_.getClass.getSimpleName) must_== Some("Key")
  }
}
