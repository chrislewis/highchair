package highchair.datastore

import highchair.tests._
import org.specs._
import java.util.Date

class EntitySpec extends highchair.specs.DataStoreSpec {
  
  doBeforeSpec {
    super.doBeforeSpec()
    Person.testSet foreach Person.put
  }
  
  "People queries" should {
    "find 3 people by last name" in {
      (Person where(_.lastName is "Lewis") fetch() size) must_== 3
    }    
    "find 1 person when limited" in {
      (Person where(_.lastName is "Lewis") fetch(limit = 1) size) must_== 1
    }
    "find 1 by first and last name" in {
      (Person where(_.lastName is "Lewis")
        and (_.firstName is "Chris") fetch() size) must_== 1
    }
    "find 1 by last, first, and middle names" in {
      (Person where(_.lastName is "Lewis")
        and (_.firstName is "Chris")
        and (_.middleName is Some("Aaron")) fetch() size) must_== 1
    }
    "find 0 Lewises under 20" in {
      (Person where(_.lastName is "Lewis")
        and (_.age < 20) fetch()) must beEmpty
    }
    "find 1 Lewis over 40" in {
      val over40 = (Person where(_.lastName is "Lewis")
        and (_.age > 40) fetch())
      over40.size must_== 1
      over40.headOption.flatMap(_ middleName) must_== Some("Donald")
    }
    "sort ascending by age" in {
      val ascAges = Person where (_.age > 20) orderAsc (_.age) fetch() map(_.age)
      ascAges must_== List(29, 31, 60)
    }
    "sort descending by age" in {
      val descAges = Person where (_.age > 20) orderDesc (_.age) fetch() map(_.age)
      descAges must_== List(60, 31, 29)
    }
    "find 0 Joneses" in {
      Person where (_.lastName is "Jones") fetch() must beEmpty
    }
    "fetch all" in {
      (Person.fetch() size) must_== 3
    }
    "fetch all with limit 2" in {
      (Person.fetch(limit = 2) size) must_== 2
    }
  }
}
