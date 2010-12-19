import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) {
  
  val gae_version = "1.3.7"
  val gaeSdk = "com.google.appengine" % "appengine-api-1.0-sdk" % gae_version % "provided"
  val gaeStubs = "com.google.appengine" % "appengine-api-stubs" % gae_version % "provided"
  val gaeTest = "com.google.appengine" % "appengine-testing" % gae_version % "provided"
  val gaeApiLabs = "com.google.appengine" % "appengine-api-labs" % gae_version % "provided"
  
  val specs = "org.scala-tools.testing" %% "specs" % "1.6.5" % "test"
  
  val appengineRepo = "AppEngine" at "http://maven-gae-plugin.googlecode.com/svn/repository/"
}
