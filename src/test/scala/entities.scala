package highchair.tests

import highchair.meta._
import java.util.Date

class Person(
  val firstName: String,
  val middleName: Option[String],
  val lastName: String,
  val age: Int,
  val birthday: Date
)

object Person extends Kind[Person] {
  val firstName = property[String]("firstName")
  val middleName = property[Option[String]]("middleName")
  val lastName = property[String]("lastName")
  val age = property[Int]("age")
  val birthday = property[Date]("birthday")
  val * = firstName ~ middleName ~ lastName ~ age ~ birthday
}
