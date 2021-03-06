package highchair.specs

import org.specs._
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.tools.development.testing._

class DataStoreSpec extends Specification {
  
  val helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig)
  implicit val ds = DatastoreServiceFactory.getDatastoreService
  implicit val ads = DatastoreServiceFactory.getAsyncDatastoreService
  
  doBeforeSpec { helper.setUp }
  doAfterSpec { helper.tearDown }
}
