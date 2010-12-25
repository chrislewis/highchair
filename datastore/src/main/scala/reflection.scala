package highchair.poso

import java.lang.reflect.{Constructor, Field, Type}

class Reflector[A](implicit m: Manifest[A]) {
  
  val clazz = m.erasure
  val constructors = clazz.getDeclaredConstructors
  
  /* Map name -> Field */
  val fields = Map(
    clazz.getDeclaredFields.map { f =>
      (f.getName, { f.setAccessible(true); f })
    }:_*
  )
    
  def simpleName = clazz.getSimpleName
  
  def does(params: Iterable[Class[_]], ctor: Iterable[Class[_]]) = 
    params == ctor
  
  def constructorFor(params: Iterable[Class[_]]) =
    constructors.find { c => does(c.getParameterTypes, params) }
  
  def field[B](a: A, field: String) = fields(field).get(a).asInstanceOf[B]
  
  /* Find a suitable constructor using a test given the argument list. */
  def findConstructor(test: Constructor[_] => Boolean): Option[Constructor[_]] =
    constructors.find(c => test(c))
  
}
