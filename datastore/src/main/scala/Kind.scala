package highchair.datastore

import meta._
import com.google.appengine.api.datastore.{
  DatastoreService,
  Entity => GEntity,
  Key,
  KeyFactory,
  EntityNotFoundException
}

/* Base trait for a "schema" of some kind E. */
abstract class Kind[E <: Entity[E]](implicit m: Manifest[E])
  extends PropertyImplicits {
  
  val reflector = new poso.Reflector[E]
  private lazy val ctor = findConstructor
  
  def keyFor(id: Long) = KeyFactory.createKey(reflector.simpleName, id)
  
  def childOf(ancestor: Key): Key = new GEntity(reflector.simpleName, ancestor).getKey

  private def entityKey(e: E) = e.key
  
  def put(e: E)(implicit dss: DatastoreService) = {
    val entity = entityKey(e).map(new GEntity(_)).getOrElse(new GEntity(reflector.simpleName))
    
    val key = dss.put(identityIdx.foldLeft(entity) {
      case (ge, (_, pm)) => putProperty(pm, e, ge)
    })
    
    entity2Object(entity)
  }
  
  def delete(e: E)(implicit dss: DatastoreService) {
    entityKey(e).map(dss.delete(_))
  }
  
  def get(key: Key)(implicit dss: DatastoreService): Option[E] = 
    try {
      val ge = dss.get(key)
      Some(entity2Object(ge))
    } catch {
      case e: EntityNotFoundException => None
    }
  
  /**/
  def where[A](f: this.type => meta.Filter[E, A]) =
    Query[E, this.type](this, f(this) :: Nil, Nil)
  
  implicit def kind2Query[K <: Kind[E]](k: K) =
    Query[E, this.type](this, Nil, Nil)
  /**/
  
  private def putProperty[A : Manifest](pm: PropertyMapping[E, _], e: E, ge: GEntity) = {
    val a = reflector.field[A](e, pm.name)
    pm.prop.asInstanceOf[Property[A]].set(ge, pm.name, a)
  }
  
  private def findConstructor = 
    reflector.findConstructor { c =>
      val p_types = c.getParameterTypes.toList
      val g_types = c.getGenericParameterTypes.toList
      p_types.containsSlice(ctorTag) &&
      findKey(p_types.zip(g_types)).isDefined
    } getOrElse error("No suitable constructor could be found!")
  
  private def findKey(types: Seq[(Class[_], java.lang.reflect.Type)]) = 
    types.find {
      case(c, t) =>
        c == classOf[Option[_]] &&
        t.isInstanceOf[java.lang.reflect.ParameterizedType] &&
        t.asInstanceOf[java.lang.reflect.ParameterizedType].getActualTypeArguments.head == classOf[Key]
    }
  
  def entity2Object(e: GEntity) = {
    val args = Some(e.getKey) :: (ctorMappings map {
      case pm => pm.prop.get(e, pm.name)
    }).toList.asInstanceOf[List[java.lang.Object]]
    ctor.newInstance(args:_*).asInstanceOf[E]
  }
  
  /* Function which, given a type A, will yield an appropriate Property instance via an implicit. */
  def property[A](name: String)(implicit p: Property[A], m: Manifest[A]) =
    new AutoMapping[E, A](this, p, m.erasure)
  
  def property[A](implicit p: Property[A], m: Manifest[A]) =
    new AutoMapping[E, A](this, p, m.erasure)
  
  implicit def autoToPropertyMapping[A](am: AutoMapping[E, A]) = 
    identityIdx.get(am)
      .map(_.asInstanceOf[PropertyMapping[E, A]])
      .getOrElse(error("No mapping found!"))
  
  /* Order is significant! */
  
  private lazy val mappings = {
    val mapped = this.getClass.getDeclaredFields
      .filter(_.getType == classOf[AutoMapping[E, _]])
    mapped.foreach(_.setAccessible(true))
    mapped
  }
  
  private lazy val fieldMappings = 
    mappings.map { f =>
      f -> f.get(this).asInstanceOf[AutoMapping[E, _]]
    }
  
  private lazy val identityIdx: Map[AutoMapping[E, _], PropertyMapping[E, _]] = {
    val pm = fieldMappings map { case (f, am) => am -> am.as(f.getName) }
    Map(pm:_*)
  }
  
  private lazy val ctorTag: Array[Class[_]] =
    fieldMappings.map(_._2.clazz)
  
  private lazy val ctorMappings: Array[PropertyMapping[E, _]] = 
    fieldMappings map { case (f, am) => am.as(f.getName) }
}
