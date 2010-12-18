package highchair.specs

import org.specs._
import com.google.appengine.api.datastore._
import com.google.appengine.tools.development.testing._

class DataStoreSpec extends Specification {
  
  val helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig)
  implicit val ds = DatastoreServiceFactory.getDatastoreService
  
  doBeforeSpec { helper.setUp }
  doAfterSpec { helper.tearDown }
}
