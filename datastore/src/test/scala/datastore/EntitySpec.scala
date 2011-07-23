package highchair.datastore

import highchair.tests._
import org.specs2.mutable._

class EntitySpec extends Specification {
  "Person kind" should {
    "build a query for people by last name" in {
      (Person where(_.lastName is "Lewis") toString) must_==
      "SELECT * FROM Person WHERE lastName = Lewis"
    }
    
    "build a query for one person by last name" in {
      (Person where(_.lastName is "Lewis") limit 1 toString) must_==
      "SELECT * FROM Person WHERE lastName = Lewis LIMIT 1 OFFSET 0"
    }
    
    "build a query for people by last name skipping the first 10" in {
      (Person where(_.lastName is "Lewis") offset 10 toString) must_==
      "SELECT * FROM Person WHERE lastName = Lewis LIMIT 500 OFFSET 10"
    }
    
    "build a query for people by first and last name" in {
      (Person where(_.lastName is "Lewis")
        and (_.firstName is "Chris") toString) must_==
      "SELECT * FROM Person WHERE lastName = Lewis AND firstName = Chris"
    }
    
    "build a query for people by last, first, and middle names" in {
      (Person where(_.lastName is "Lewis")
        and (_.firstName is "Chris")
        and (_.middleName is Some("Aaron")) toString) must_==
        "SELECT * FROM Person WHERE lastName = Lewis AND firstName = Chris AND middleName = Some(Aaron)"
    }
    
    "build a query for people under 20 by last name" in {
      (Person where(_.lastName is "Lewis")
        and (_.age < 20) toString) must_==
        "SELECT * FROM Person WHERE lastName = Lewis AND age < 20"
    }
    
    "build a query for people over 40 by last name" in {
      (Person where(_.lastName is "Lewis")
        and (_.age > 40) toString) must_==
        "SELECT * FROM Person WHERE lastName = Lewis AND age > 40"
    }
    
    "build a query for people under 20 in ascending order by age" in {
      (Person where (_.age > 20) orderAsc (_.age) toString) must_==
      "SELECT * FROM Person WHERE age > 20 ORDER BY age ASC"
    }
    
    "build a query for people under 20 in descending order by age" in {
      (Person where (_.age > 20) orderDesc (_.age) toString) must_==
      "SELECT * FROM Person WHERE age > 20 ORDER BY age DESC"
    }
  }
  
}
