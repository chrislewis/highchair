package highchair.datastore

import com.google.appengine.api.datastore.FetchOptions

case class Fetch(limit: Int = 500, skip: Int = 0) {
  def fetchOptions =
    FetchOptions.Builder withOffset(skip) limit(limit)
  def toGQL = "LIMIT %d OFFSET %d".format(limit, skip)
}
