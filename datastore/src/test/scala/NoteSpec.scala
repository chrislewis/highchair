package highchair.tests

import highchair.datastore.meta._
import org.specs._
import org.joda.time.DateTime
import com.google.appengine.api.datastore.Text

class NoteSpec extends highchair.specs.DataStoreSpec {
  
  val notes = List(
    Note(None, "A short note", new Text("This is a very short note."), new DateTime()),
    Note(None, "A long note", new Text("This note can be up to a meg in size!"), new DateTime())
  )
  
  doBeforeSpec {
    super.doBeforeSpec()
    notes foreach Note.put
  }
 
  "Note queries" should {
    "Find both notes" in {
      (Note where(_.title in ("A short note", "A long note")) fetch() size) must_== 2
    }
    "Find the long note with a Text prop" in {
      val txt = Note where(_.title === "A long note") fetchOne() map (_.details.getValue)
      txt must_== Some("This note can be up to a meg in size!")
    }
  }
}
