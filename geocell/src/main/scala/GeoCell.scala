package highchair.geocell

import Resolution._

case class Box(val northEast: (Double, Double), val southWest: (Double, Double))

/**
 * A library for pure-functional geocell calculations, initially
 * ported from javageomodel http://code.google.com/p/javageomodel/.
 *
 * @see http://code.google.com/apis/maps/articles/geospatial.html
 */
object GeoCell {
  
  type Point = (Double, Double)
  
  val GRID_SIZE = 4
  val ALPHABET = "0123456789abcdef"
  val RADIUS = 6378135 /* meters */
    

  // Returns the (x, y) of the geocell character in the 4x4 alphabet grid.
  /* Calculate the coordinates of a character   */
  def subdivXY(c: Char): Point = ALPHABET indexOf c match {
    case _c => (
      (_c & 4) >> 1 | (_c & 1) >> 0,
      (_c & 8) >> 2 | (_c & 2) >> 1
    )
  }
  
  // Returns the geocell character in the 4x4 alphabet grid at pos. (x, y).
  def subdivChar(pos: (Int, Int)) =
    ALPHABET(
      (pos._2 & 2) << 2 |
      (pos._1 & 2) << 1 |
      (pos._2 & 1) << 1 |
      (pos._1 & 1) << 0)
  
  /** Computes the rectangular boundaries (bounding box) of the given geocell. */
  def computeBox(cell: String) = {
    def _computeBox(box: Box, cell: List[Char]): Box = (box, cell) match {
      case (_, Nil) => box
      case (Box((n, e), (s, w)), head :: tail) => {
        val latSpan = (n - s) / GRID_SIZE
        val lonSpan = (e - w) / GRID_SIZE
        val (x, y) = subdivXY(head)
        _computeBox(Box(
          (s + latSpan * (y + 1), w + lonSpan * (x + 1)),
          (s + latSpan * y, w + lonSpan * x)), tail)
      }
    }
    
    _computeBox(Box((90, 180), (-90, -180)), cell toList)
  }
  
  /** Calculate the geocell for a point at the given resolution. */
  def ?(point: Point, resolution: Resolution = Resolution.Max): String = {
    def _compute(n: Double, s: Double, e: Double, w: Double, cell: String = "", depth: Option[Resolution] = Some(Resolution.Min)): String = {
      val x = math.min((GRID_SIZE * (point._2 - w) / (e - w)),
        GRID_SIZE - 1).toInt;
      val y = math.min((GRID_SIZE * (point._1 - s) / (n - s)),
        GRID_SIZE - 1).toInt;
      val subcellLonSpan = (e - w) / GRID_SIZE
      val subcellLatSpan = (n - s) / GRID_SIZE
      
      val nextCell = cell + subdivChar((x, y))
      
      depth match {
        case Some(r) if r == resolution => nextCell
        case _ => _compute(
          (s + (subcellLatSpan * y)) + subcellLatSpan,
          s + (subcellLatSpan * y),
          (w + (subcellLonSpan * x)) + subcellLonSpan,
          w + (subcellLonSpan * x),
          nextCell,
          depth flatMap (_ next)
        )
      }
    }
    _compute(90, -90, 180, -180)
  }
  
  /** Calculate the geocells in which the point resides at every resolution. */
  def ??(point: Point) = 
    (Resolution.Min to Resolution.Max) map(?(point, _))

  /** Calculate the distance between two points in meters. */
	def distance(p1: Point, p2: Point) = (p1, p2) match {
	  case (Rad(lat1, lon1), Rad(lat2, lon2)) =>
      RADIUS * math.acos(math.sin(lat1) * math.sin(lat2)
              + math.cos(lat1) * math.cos(lat2)
              * math.cos(lon2 - lon1))
	}
	
	/** Extractor for a Point to a pair of radian values. */
	object Rad {
	  def unapply(p: Point) = p match {
	    case (lat, lon) =>
	      Some((math toRadians lat, math toRadians lon))
	  }
	}
	
}
