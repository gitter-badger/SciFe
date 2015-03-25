package scife
package enumeration
package benchmarks
package nomemoization

import dependent._

import scife.{ enumeration => e }

import scife.util._
import logging._

import structures._
import BSTrees._

import org.scalatest._
import org.scalameter.api._

import scala.language.existentials

class BinarySearchTreeBenchmark
  extends StructuresBenchmark[Depend[(Int, Range), Tree]]
  {

  type EnumType = Depend[(Int, Range), Tree]

  def measureCode(tdEnum: EnumType) = {
    { (size: Int) =>
      val enum = tdEnum.getEnum((size, 1 to size))
      for (i <- 0 until enum.size) enum(i)
    }
  }

  def warmUp(inEnum: EnumType, maxSize: Int) {
    for (size <- 1 to maxSize) {
      val enum = inEnum.getEnum((size, 1 to size))
      for (i <- 0 until enum.size) enum(i)
    }
  }

  override def constructEnumerator(implicit ms: memoization.MemoizationScope) = {
    Depend.fin(
      (self: Depend[(Int, Range), Tree], pair: (Int, Range)) => {
        val (size, range) = pair

        if (size <= 0) e.Singleton(Leaf)
        else if (size == 1)
          e.WrapArray(range map { v => Node(Leaf, v, Leaf) })
        else {
          val roots = e.Enum(range)
          val leftSizes = e.Enum(0 until size)

          val rootLeftSizePairs = e.Product(leftSizes, roots)

          val leftTrees: Depend[(Int, Int), Tree] = InMap(self, { (par: (Int, Int)) =>
            val (leftSize, median) = par
            (leftSize, range.start to (median - 1))
          })

          val rightTrees: Depend[(Int, Int), Tree] =
            InMap(self, { (par: (Int, Int)) =>
              val (leftSize, median) = par
              (size - leftSize - 1, (median + 1) to range.end)
            })

          val leftRightPairs: Depend[(Int, Int), (Tree, Tree)] =
            Product(leftTrees, rightTrees)

          val allNodes =
            Chain[(Int, Int), (Tree, Tree), Node](rootLeftSizePairs, leftRightPairs,
              (p1: (Int, Int), p2: (Tree, Tree)) => {
                val ((leftSize, currRoot), (leftTree, rightTree)) = (p1, p2)

                Node(leftTree, currRoot, rightTree)
              })

          allNodes
        }
      })
  }

}
