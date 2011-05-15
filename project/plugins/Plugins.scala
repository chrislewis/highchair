import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val posterous = "net.databinder" % "posterous-sbt" % "0.1.7"
  val ghIssues = "me.lessis" % "sbt-gh-issues" % "0.1.0"
  
  val lessis = "less is repo" at "http://repo.lessis.me"
}
