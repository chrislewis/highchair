package highchair.tests

import org.specs._

class RelationSpec extends highchair.specs.DataStoreSpec {
  
  "Users" should {
    val friendless  = User.put(User(None, "Grinch", None, Nil))
    val new_friend  = User.put(User(None, "Bryan", None, Nil))
    val has_friends = User.put(User(None, "Chris", None, new_friend.key.toList))
    
    "start with no friends" in {
      (for {
        k <- friendless.key
        u <- User.get(k)
      } yield u.friends) must_== Some(Nil)
    }
    
    "have a friend after socializing" in {
      (has_friends.key flatMap { User.get } match {
        case Some(User(k, name, None, friends)) => friends
        case _ => Nil
      }) must_== new_friend.key.toList
    }
    
  }
  
  "ContactInfo" should {
    val userKey = User.keyFor(1)
    val info = ContactInfo.put(ContactInfo(Some(ContactInfo.childOf(userKey)), "chris@thegodcode.net", "511"))
    val friendless = User.put(User(Some(userKey), "Chris", info.key, Nil))
    
    "have its key aggregated in a User" in {  
      friendless.contactInfo must_== info.key
    }
    
    "have a User key as its ancestor" in {
      info.ancestorKey must_== friendless.key
    }
  }
  
  
}
