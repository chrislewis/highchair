package highchair.geocell

import org.specs._

class ResolutionSpec extends Specification {
  "a Resolution range" should {
    "include One through Five" in {
      One to Five must_== List(One, Two, Three, Four, Five)
    }
    
    "include only Three" in {
      Three to Three must_== List(Three)
    }
    
    "be empty when the upperbound is a lower resolution" in {
      Five to Four must beEmpty
    }
  }
}
