package highchair

import com.google.appengine.api.datastore.Key

trait Entity[E] {
  def key: Option[Key]
}
