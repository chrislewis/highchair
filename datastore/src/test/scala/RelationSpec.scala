package highchair.tests

import highchair.meta.FilterOps._
import org.specs._

class RelationSpec extends highchair.specs.DataStoreSpec {
  
  val no_friends = User(None, "Chris", Nil)
  
  "Users" should {
    
    val friendless = User.put(no_friends)
    
    "start with no friends" in {
      User.get(friendless.key.get).get.friends must beEmpty
    }
    
    "have a friend after socializing" in {
      val new_friend = User.put(User(None, "Bryan", Nil))
      val has_friends = User.put(friendless.copy(friends = List(new_friend.key.get)))
      User.get(has_friends.key.get).get.friends must_== List(new_friend.key.get)
    }
    
  }
  
}
