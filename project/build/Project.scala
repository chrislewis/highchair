import sbt._

class AppengineTestProject(info: ProjectInfo) extends AppengineProject(info) {
  val specs = "org.scala-tools.testing" %% "specs" % "1.6.5" % "test"
}
