package highchair.datastore

import highchair.datastore.meta._
import org.specs._
import java.util.Date
import org.joda.time.DateTime
import com.google.appengine.api.datastore.Text

class CorePropertiesSpec extends Specification with PropertyImplicits {
  
  def equiv[A, B](p: Prop[A], a: A, b: B) =
    p.toStoredType(a) == b && p.fromStoredType(b) == a
  
  "Core properties" should {
    "map Boolean to and from java.lang.Boolean" in {
      equiv(boolProp, true, java.lang.Boolean.TRUE) mustBe true
      equiv(boolProp, false, java.lang.Boolean.FALSE) mustBe true
    }
    "map Int to and from java.lang.Integer" in {
      equiv(intProp, 42, new java.lang.Integer(42)) mustBe true
    }
    "map Long to and from java.lang.Long" in {
      equiv(longProp, 42L, new java.lang.Long(42L)) mustBe true
    }
    "map Float to and from java.lang.Float" in {
      equiv(floatProp, 42f, new java.lang.Float(42f)) mustBe true
    }
    "map Double to and from java.lang.Double" in {
      equiv(doubleProp, 42d, new java.lang.Double(42d)) mustBe true
    }
    "map String to and from java.lang.String" in {
      equiv(stringProp, "hi", new java.lang.String("hi")) mustBe true
    }
    "map Date to and from java.util.Date" in {
      val date = new Date()
      equiv(dateProp, date, date) mustBe true
    }
    "map Date to and from java.util.Date" in {
      val date = new Date()
      equiv(dateProp, date, date) mustBe true
    }
    "map DateTime to and from java.util.Date" in {
      val jodaDate = new DateTime()
      equiv(jodaDateTimeProp, jodaDate, jodaDate.toDate) mustBe true
    }
    "map Text to and from Text" in {
      equiv(textProp, new Text("hi"), new Text("hi")) mustBe true
    }
    "map Option[String] to and from java.lang.String" in {
      equiv(new OptionalProp(stringProp), Some("hi"), new java.lang.String("hi")) mustBe true
    }
    "map List[String] to and from java.util.List[java.lang.String]" in {
      val jl = new java.util.ArrayList[String]()
      jl.add("hi")
      equiv(new ListProp(stringProp), List("hi"), jl) mustBe true
    }
  }
}
