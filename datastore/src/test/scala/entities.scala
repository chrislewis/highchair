package highchair.tests

import highchair._
import java.util.Date
import com.google.appengine.api.datastore.Key

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
  val friends: List[Key]
) extends Entity[User]

object User extends Kind[User] {
  val firstName = property[String]("firstName")
  val friends = property[List[Key]]("friends")
  val * = firstName ~ friends
}
