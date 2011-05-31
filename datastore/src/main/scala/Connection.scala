package highchair.datastore

object Connection {
  import com.google.appengine.api.datastore.{
    DatastoreServiceFactory => Factory
  }
  /** A default synchronous connection. */
  implicit lazy val default = Factory.getDatastoreService()
  /** A default asynchronous connection. */
  implicit lazy val defaultAsync = Factory.getAsyncDatastoreService()
}
