package highchair

import com.google.appengine.api.datastore.Key

trait Entity[E] { this: E =>
  def key = _key
  private var _key: Option[Key] = None
  private[highchair] def persistent(key: Key) = {
    _key = Some(key)
    this
  }
}
