package highchair.geocell

import org.specs._

class GeoCellSpec extends Specification {
  
  val jacksonville = (30.332184, -81.655651)
  val st_augustine = (29.896466, -81.313248)
  val firenze =      (37.0625,   -95.677068)
  
  "Saint Augustine" should {
    "be in cell 989c8471bbd5d at max resolution" in {
      GeoCell ? st_augustine must_== "989c8471bbd5d"
    }
    
    "be in cell 9 at resolution One" in {
      GeoCell ? (st_augustine, One) must_== "9"
    }
  }
  
  "Jacksonville" should {
    "be in cell 989c8471bbd5d at max resolution" in {
      GeoCell ? jacksonville must_== "989b5c5f82e47"
    }
    
    "be in cell 9 at resolution One" in {
      GeoCell ? (jacksonville, One) must_== "9"
    }
    
    "be about 58649 meters from Saint Augustine" in {
      GeoCell distance (jacksonville, st_augustine) must beCloseTo(58649d, 5d) /* Give or take 5 meters. */
    }
  }
  

  
  "Firenze" should {
    "essere nella cella 8f65df3c409b3 alla massima risoluzione" in {
      GeoCell ? firenze must_== "8f65df3c409b3"
    }
    
    "essere nella cella 8 alla risoluzione One" in {
      GeoCell ? (firenze, One) must_== "8"
    }
  }
  
}

