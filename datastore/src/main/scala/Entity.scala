package highchair

import com.google.appengine.api.datastore.Key

trait Entity[E] {
  def key: Option[Key]
  def ancestorKey: Option[Key] = key flatMap {
    _ getParent match {
      case null => None
      case p => Some(p)
    }
  }
}
