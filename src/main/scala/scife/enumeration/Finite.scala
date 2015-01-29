package scife.enumeration

import scala.language.implicitConversions

trait Finite[+A] extends Enum[A] {

  def hasDefiniteSize = true

  /* operators */

  // concatenation
  def concat[B >: A](e: Finite[B]) =
    Concat(this, e)

  def ++[B](e: Finite[B]) = concat(e)
  def ⊕[B](e: Finite[B]) = concat(e)

  // products

//  override def product[B](e: Enum[B]): Enum[(A, B)] =
//    Product(this, e)
//
//  override def **[B](e: Enum[B]) = product(e)
//  override def ⊗[B](e: Enum[B]) = product(e)

  def product[B](e: Finite[B]): Finite[(A, B)] =
    Product(this, e)

  def **[B](e: Finite[B]) = product(e)
  def ⊗[B](e: Finite[B]) = product(e)

  def product[B](e: Infinite[B]): Infinite[(A, B)] =
    Product(this, e)

  def **[B](e: Infinite[B]) = product(e)
  def ⊗[B](e: Infinite[B]) = product(e)

  // map
  override def map[B](modifyFun: A => B): Finite[B] =
    Map(this, modifyFun)

  override def ↑[B](modifyFun: A => B) = map(modifyFun)
 
  import dependent._
    
  def ⊘[B, A2 >: A](dep: DependFinite[A2, B]): Finite[(A2, B)] = 
    dep ⊘ this 
    
  def schain[B, A2 >: A](dep: DependFinite[A2, B]): Finite[B] = 
    dep chainSingle this

}

object Finite {

}
