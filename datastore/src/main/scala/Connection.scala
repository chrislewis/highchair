package highchair.datastore

object Connection {
  import com.google.appengine.api.datastore.{
    DatastoreServiceFactory => Factory
  }
  
  implicit lazy val default = Factory.getDatastoreService()
  
  implicit lazy val defaultAsync = Factory.getAsyncDatastoreService()
}
