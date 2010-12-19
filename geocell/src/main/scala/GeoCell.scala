package highchair.geocell

/**
 * A library for pure-functional geocell calculations, initially
 * ported from javageomodel http://code.google.com/p/javageomodel/.
 *
 * @see http://code.google.com/apis/maps/articles/geospatial.html
 */
object GeoCell {
  
  type Point = (Double, Double)
  
  val MAX_RESOLUTION = 13
  val GRID_SIZE = 4
  val ALPHABET = "0123456789abcdef"
  
  // Returns the geocell character in the 4x4 alphabet grid at pos. (x, y).
  def subdivChar(pos: (Int, Int)) =
    ALPHABET(
      (pos._2 & 2) << 2 |
      (pos._1 & 2) << 1 |
      (pos._2 & 1) << 1 |
      (pos._1 & 1) << 0)
  
  /** Calculate the geocell for a point at the given resolution. */
  def compute(point: Point, resolution: Int = MAX_RESOLUTION): String = {
    def _compute(n: Double, s: Double, e: Double, w: Double, cell: String = "", depth: Int = 0): String = {
      if(depth >= resolution)
        cell
      else {
        val x = Math.min((GRID_SIZE * (point._2 - w) / (e - w)),
          GRID_SIZE - 1).toInt;
        val y = Math.min((GRID_SIZE * (point._1 - s) / (n - s)),
          GRID_SIZE - 1).toInt;
        val subcellLonSpan = (e - w) / GRID_SIZE
        val subcellLatSpan = (n - s) / GRID_SIZE
        
        _compute(
          (s + (subcellLatSpan * y)) + subcellLatSpan,
          s + (subcellLatSpan * y),
          (w + (subcellLonSpan * x)) + subcellLonSpan,
          w + (subcellLonSpan * x),
          cell + subdivChar((x, y)),
          depth + 1
        )
      }
    }
    _compute(90d, -90d, 180d, -180d)
  }
}
