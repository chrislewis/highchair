# Highchair

Highchair is an experimental scala library for persisting objects to the Google Datastore.

## Goals

* Simplicity
* Type-safety
* Immutability

A quick example:

    import highchair._
    import highchair.meta.FilterOps._
    import com.google.appengine.api.datastore.Key
    
    case class Person(
      val key: Option[Key],
      val firstName: String,
      val middleName: Option[String],
      val lastName: String,
      val age: Int
    ) extends Entity[Person]
    
    object Person extends Kind[Person] {
      val firstName = property[String]("firstName")
      val middleName = property[Option[String]]("middleName")
      val lastName = property[String]("lastName")
      val age = property[Int]("age")
      val * = firstName ~ middleName ~ lastName ~ age
    }
    
    
    /* A transient entity. */
    val transient_chris = Person(None, "Chris", Some("Aaron"), "Lewis", 29)
    
    /* 
     * Put a transient entity and receive a persistent one.
     * Note that the transient is still transient.
     */
    val persistent_chris = Person.put(transient_chris)
    
    /* Update and put the persistent entity. */
    val older_chris = Person.put(persistent_chris.copy(age = 30))

See the [specs](http://github.com/chrislewis/highchair/tree/master/datastore/src/test/scala) for more examples.
