package highchair.datastore

import org.specs2.mutable._
import com.google.appengine.api.datastore.Key

class MapNaming extends Specification {
  
  case class Point(
    key:  Option[Key],
    x:    Int,
    y:    Int
  ) extends Entity[Point]
  
  object Point extends Kind[Point] {
    val x = property[Int]("ex")
    val y = property[Int]
  }
  
  "A mapped property" should {
    "be nameabled when it doesn't have a preset" in {
      Point.y.as("why").name must_== "why"
      Point.y.as("y").name must_== "y"
    }
    "not be nameabled when it has a preset" in {
      Point.x.as("x").name must_== "ex"
    }
  }
  
}
