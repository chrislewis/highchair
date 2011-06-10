package highchair.datastore

import highchair.tests._
import org.specs._

import com.google.appengine.api.datastore.EntityNotFoundException
import java.util.concurrent.ExecutionException

class AsyncEntitySpec extends highchair.specs.DataStoreSpec {
  
  doBeforeSpec {
    super.doBeforeSpec()
    Person.testSet foreach Person.put
  }
  
  "Asynchronous gets" should {
    import Async._
    
    val rex = new RuntimeException("oops!")
    val future = Person.async.get(Person.keyFor(-1))
    
    "fail with ExecutionException on not found" in {
      future.get().fold(identity, _ => rex) must haveClass[ExecutionException]
    }
    
    "fail and wrap EntityNotFoundException on not found" in {
      future.get().fold(_.getCause(), _ => rex) must haveClass[EntityNotFoundException]
    }
    
    val erin = Person where (_.firstName is "Erin") fetchOne()
    val goodFuture = erin.flatMap(_.key).map(Person.async.get)
    
    "succeed when found" in {
      goodFuture.map(_.get().fold(Some(_), identity)) must_== erin
    }
    
    "accumulate transformations" in {
      goodFuture.map {
        _.map(_ => 1).map(2*).map(4*).get()
      } must_== Some(Right(8))
    }
    
  }
}
