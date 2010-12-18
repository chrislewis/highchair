import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) {
  
  val gae_version = "1.3.7"
  val gaeSdk = "com.google.appengine" % "appengine-api-1.0-sdk" % gae_version % "compile"
  val gaeStubs = "com.google.appengine" % "appengine-api-stubs" % gae_version % "compile"
  val gaeTest = "com.google.appengine" % "appengine-testing" % gae_version % "compile"
  val gaeApiLabs = "com.google.appengine" % "appengine-api-labs" % gae_version % "compile"
  
  val specs = "org.scala-tools.testing" %% "specs" % "1.6.5" % "test"
  
  val appengineRepo = "nexus" at "http://maven-gae-plugin.googlecode.com/svn/repository/"
}
