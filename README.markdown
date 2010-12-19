
    import highchair._
    
    case class Person(
      val firstName: String,
      val middleName: Option[String],
      val lastName: String,
      val age: Int
    )
    
    object Person extends Kind[Person] {
      val firstName = property[String]("firstName")
      val middleName = property[Option[String]]("middleName")
      val lastName = property[String]("lastName")
      val age = property[Int]("age")
      val * = firstName ~ middleName ~ lastName ~ age
    }

