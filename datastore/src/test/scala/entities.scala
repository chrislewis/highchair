package highchair.tests

import highchair._
import java.util.Date
import com.google.appengine.api.datastore.{Key, Text}

case class Person(
  val key: Option[Key],
  val firstName: String,
  val middleName: Option[String],
  val lastName: String,
  val age: Int,
  val birthday: Date,
  val aliases: List[String]
) extends Entity[Person]

object Person extends Kind[Person] {
  val firstName = property[String]("firstName")
  val middleName = property[Option[String]]("middleName")
  val lastName = property[String]("lastName")
  val age = property[Int]("age")
  val birthday = property[Date]("birthday")
  val aliases = property[List[String]]("aliases")
  val * = firstName ~ middleName ~ lastName ~ age ~ birthday ~ aliases
}


case class User(
  val key: Option[Key],
  val firstName: String,
  val contactInfo: Option[Key],
  val friends: List[Key]
) extends Entity[User]

object User extends Kind[User] {
  val firstName = property[String]("firstName")
  val contactInfo = property[Option[Key]]("contactInfo")
  val friends = property[List[Key]]("friends")
  val * = firstName ~ contactInfo ~ friends
}


case class ContactInfo(
  val key: Option[Key],
  val email: String,
  val mobile: String
) extends Entity[ContactInfo]

object ContactInfo extends Kind[ContactInfo] {
  val email = property[String]("email")
  val mobile = property[String]("mobile")
  val * = email ~ mobile
}


case class Note(
  val key: Option[Key],
  val title: String,
  val details: Text
) extends Entity[Note]

object Note extends Kind[Note] {
  val title = property[String]("title")
  val details = property[Text]("details")
  val * = title ~ details 
}
