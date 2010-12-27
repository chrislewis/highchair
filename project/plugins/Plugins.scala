import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val gae = "net.stbbs.yasushi" % "sbt-appengine-plugin" % "2.1"
  val posterous = "net.databinder" % "posterous-sbt" % "0.1.6"
}
