package highchair.remote.tests

import org.specs._
import com.google.appengine.tools.development.testing._
import com.google.appengine.api.datastore.{
  DatastoreServiceFactory => DSF,
  Entity
}
import highchair.remote.Remote

class RemoteSpec extends Specification {
  
  val remote = Remote("localhost" -> 8080, "user@foo.com" -> "foopass")
  val invalidRemote = Remote("localhost" -> 80, "user@foo.com" -> "foopass")
  
  val helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig)
  val doPersist = () => DSF.getDatastoreService.put(new Entity("RemoteEntity"))
  
  "A valid Remote should" should {
    "persist to a remote datastore" in {
      remote {
        doPersist()
      } fold (_ => "fail", _.getKind) must_== "RemoteEntity"
    }
    
    "scope remote operations" in {
      remote {
        doPersist()
      } fold (_ => "fail", _.getKind) must_== "RemoteEntity"
      
      doPersist() must throwA[NullPointerException]
    }
  }
  
  "An invalid Remote should " in {
    "fail to persist to a remote datastore" in {
      invalidRemote {
        doPersist()
      } fold (_ => "fail", _.getKind) must_== "fail"
    }
  }
  
}
