import sbt._

class Highchair(info: ProjectInfo) extends ParentProject(info) with posterous.Publish {
  
  val gae_version = "1.3.7"
  val specs_version = "1.6.5"
  
  /* Minimal GAE artifacts (from AppEngine repo) for local datastore. */
  trait GAEDatastoreDeps {
    val gaeSdk = "com.google.appengine" % "appengine-api-1.0-sdk" % gae_version % "provided"
    val gaeStubs = "com.google.appengine" % "appengine-api-stubs" % gae_version % "provided"
    val gaeTest = "com.google.appengine" % "appengine-testing" % gae_version % "provided"
    val gaeApiLabs = "com.google.appengine" % "appengine-api-labs" % gae_version % "provided"
  }
  
  class HighchairModule(info: ProjectInfo) extends DefaultProject(info) with GAEDatastoreDeps
  
  val specsDep = "org.scala-tools.testing" %% "specs" % specs_version
  
  lazy val spec = project("spec", "Highchair Spec", new HighchairModule(_) {
    lazy val specs = specsDep
  })
  lazy val datastore = project("datastore", "Highchair Datastore", new HighchairModule(_) {
    lazy val specs = specsDep % "test"
  }, spec)
  
  /* Additional repos. */
  val gae_repo = "AppEngine" at "http://maven-gae-plugin.googlecode.com/svn/repository/"
  
}
