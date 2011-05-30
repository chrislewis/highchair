import sbt._

class Highchair(info: ProjectInfo) extends ParentProject(info)
  with posterous.Publish
  with gh.Issues {
  
  val gae_version = "1.4.3"
  val dispatch_version = "0.8.1"
  
  /* Minimal GAE artifacts (from AppEngine repo) for local datastore. */
  trait GAEDatastoreDeps {
    val gaeSdk      = "com.google.appengine" % "appengine-api-1.0-sdk" % gae_version % "provided"
    val gaeStubs    = "com.google.appengine" % "appengine-api-stubs" % gae_version % "provided"
    val gaeTest     = "com.google.appengine" % "appengine-testing" % gae_version % "provided"
    val gaeApiLabs  = "com.google.appengine" % "appengine-api-labs" % gae_version % "provided"
  }
  
  class HighchairModule(info: ProjectInfo) extends DefaultProject(info) with GAEDatastoreDeps {
    /* Source attachements. */
    override def packageSrcJar = defaultJarPath("-sources.jar")
    lazy val sourceArtifact = Artifact.sources(artifactID)
    override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageSrc)
  }
  
  def specsDep =
    "org.scala-tools.testing" %% "specs" % { 
      if (buildScalaVersion startsWith "2.8.0")
        "1.6.5"
      else if (buildScalaVersion startsWith "2.8.1")
        "1.6.7.2"
      else
        "1.6.8"
    }
  
  lazy val datastore = project("datastore", "Highchair Datastore", new HighchairModule(_) {
    override def deliverProjectDependencies = 
      super.deliverProjectDependencies.toList - spec.projectID ++ Seq(spec.projectID % "test")
    val joda = "joda-time" % "joda-time" % "1.6.2"
  }, spec)
  lazy val spec = project("spec", "Highchair Spec", new HighchairModule(_) {
    lazy val specs = specsDep
  }, util)
  lazy val remote = project("remote", "Highchair Remote", new HighchairModule(_) {
    lazy val specs = specsDep
  })
  lazy val util = project("util", "Highchair Util", new DefaultProject(_) {
    lazy val specs = specsDep
    lazy val dispatch = "net.databinder" %% "dispatch-http" % dispatch_version
  })
  
  /* Additional repos. */
  val gae_repo = "AppEngine" at "http://maven-gae-plugin.googlecode.com/svn/repository/"
  /* Without this, publishing fails to resolve interdependent modules of the project. */
  val publish_repo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
  
  override def managedStyle = ManagedStyle.Maven
  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
  Credentials(Path.userHome / ".ivy2" / ".credentials", log)
  
  /* Github creds (gh-issues). */
  def ghCredentials = gh.LocalGhCreds(log)
  def ghRepository = ("chrislewis", "highchair")
}
