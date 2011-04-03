# Highchair

Highchair is an experimental library for persisting scala objects to the Google Datastore.

## Goals

* Type-safety
* Simplicity
* Immutability

## Defining Persistent Objects

Persistent objects are defined as case classes which mixin the Entity trait. For query logic, mix the Kind trait
into a dedicated class. The companion object is a natural choice. 
Note: Highchair currently requires persistent objects to recieve an Option[Key] as the first constructor argument.

    import highchair._
    import com.google.appengine.api.datastore.Key
    
    case class Person(
      val key: Option[Key],
      val firstName: String,
      val middleName: Option[String],
      val lastName: String,
      val age: Int
    ) extends Entity[Person]
    
    object Person extends Kind[Person] {
      val firstName   = property[String]("firstName")
      val middleName  = property[Option[String]]("middleName")
      val lastName    = property[String]("lastName")
      val age         = property[Int]("age")
      val * = firstName ~ middleName ~ lastName ~ age
    }
    
## Working With Persistent Data

The Kind trait defines the core persistence logic for entities. Operations against the datastore are carried out
using the [low-level API](http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/package-summary.html).
For maximum flexibility, a Kind recieves a connection to the datastore as an implicit argument.
    
    import javax.servlet._
    import javax.servlet.http._
    
    import com.google.appengine.api.datastore.DatastoreServiceFactory
    
    class HighchairDemoServlet extends HttpServlet {
      
      /* An implicit in scope of this servlet. */
      implicit val dss = DatastoreServiceFactory.getDatastoreService
    
      override def doGet(req: HttpServletRequest, res: HttpServletResponse) {
        /* A transient entity. */
        val transient_chris = Person(None, "Chris", Some("Aaron"), "Lewis", 29)
        
        /* 
         * Put a transient entity and receive a persistent one.
         * Note that the transient is still transient.
         */
        val persistent_chris = Person.put(transient_chris)
        
        /* Update and put the persistent entity. */
        val older_chris = Person.put(persistent_chris.copy(age = 30))
        
        res.getWriter.print(older_chris.age)
      }
      
    }


See the [specs](http://github.com/chrislewis/highchair/tree/master/datastore/src/test/scala) for more examples.

## Install

Highchair requires Scala 2.8 and is cross-built for versions 2.8.0 and 2.8.1. Highchair artifacts are published using
the excellent built tool [sbt](http://code.google.com/p/simple-build-tool/).

### sbt

    val h_datastore = "net.thegodcode" %% "highchair-datastore" % "0.0.2"

### maven, ivy

Because sbt uses the maven module format, artifacts published via sbt are usable by maven and ivy. If you are
unfamiliar with sbt, note the following:

  - You must be explicit about the artifact versions. Because Highchair is cross-built its artifacts are suffixed
    with _scala_version, where scala_version is the version of scala against which the artifact was compiled.
    Therefore, in tools such as maven and ivy, the artifact id would be `highchair-datastore_2.8.0`.
  - You must add `http://scala-tools.org/repo-releases` as a repository.
