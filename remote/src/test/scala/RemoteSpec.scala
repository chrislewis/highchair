package highchair.remote.tests

import org.specs._
import com.google.appengine.tools.development.testing._
import com.google.appengine.api.datastore.{
  DatastoreServiceFactory => DSF,
  Entity
}
import highchair.remote.Remote

class RemoteSpec extends Specification {
  
  val helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig)
  val remote = Remote("localhost" -> 8080, "user@foo.com" -> "foopass")
  val invalidRemote = Remote("localhost" -> 80, "user@foo.com" -> "foopass")
  
  "Remote datastore save" should {
    "succeed" in {
      val remoteSave = remote {
        DSF.getDatastoreService.put(new Entity("RemoteEntity"))
      }
      remoteSave fold (_ => "fail", _.getKind) must_== "RemoteEntity"
    }
    
    "fail" in {
      val remoteSave = invalidRemote {
        DSF.getDatastoreService.put(new Entity("RemoteEntity"))
      }
      remoteSave fold (_ => "fail", _.getKind) must_== "fail"
    }
  }
  
}
