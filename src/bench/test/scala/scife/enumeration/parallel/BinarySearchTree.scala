package scife
package enumeration
package parallel

import dependent._
import memoization._
import scife.{ enumeration => e }
import e.parallel._

import scife.util._
import scife.util.logging._

import java.util.concurrent._

import structures._
import BSTrees._

import benchmarks._

import org.scalatest._
import org.scalameter.api._

import scala.language.existentials

class BinarySearchTreeBenchmark(numberOfThreads: Int)
  extends StructuresBenchmark[Depend[(Int, Range), Tree]] {

  type EnumType = Depend[(Int, Range), Tree]

  def measureCode(tdEnum: EnumType) = {
    { (size: Int) =>
      this.tdEnum = tdEnum
      this.size = size

      //initExecutor
//      var beg = System.currentTimeMillis()
      exec.invokeAll(runners)
      //exec.shutdownNow();
      //exec.awaitTermination(2, TimeUnit.MINUTES);
//      println(System.currentTimeMillis() - beg)
//      exec.shutdown()
//      System.out.flush
    }
  }

  var size: Int = _
  var tdEnum: EnumType = _

//  @transient
  var exec: ExecutorService = _

  def initExecutor =
    if (exec == null) exec = Executors.newFixedThreadPool(numberOfThreads)

  @transient
  lazy val runners: java.util.Collection[Callable[Object]] =
//_
//  def initRunners = runners =
{
    val al = new java.util.ArrayList[Callable[Object]]()
    var i = 0
    while (i < numberOfThreads) {
      al add Executors.callable(new Runnable {
        def run = {
          try {
            var myInd = i
            val enum = tdEnum.getEnum((size, 1 to size))
  
            while (myInd < enum.size) {
              enum(myInd)
              myInd += numberOfThreads
            }
            
          } catch {
            case t: Throwable =>
              println(s"Thrown $t:${t.getStackTrace.mkString("\n")} at $i")
          }
        }
      })
      i+=1
    }
    
    al
  }
  
  var freeMemory: Int = _
  import scala.scife.enumeration.util._

  override def setUp(size: Int, tdEnum: EnumType, memScope: e.memoization.MemoizationScope) {
//    import Memory._
//
//    getFreeMemory
//    println(s"Freeing up $freeMemory")
//    tryToFreeUpSpaceG(freeMemory)    
//    getFreeMemory
    runners != null
    initExecutor
//    Thread.sleep(runners.size)
//    System.gc;
//    System.gc;
//    Thread.sleep(1000)
//    System.gc;
//    System.gc;
//    Thread.sleep(1000)
  }

  def warmUp(tdEnum: EnumType, maxSize: Int) {
    
    for (size <- 1 to maxSize) {
      val enum = tdEnum.getEnum((size, 1 to size))
      for (i <- 0 until enum.size) enum(i)
    }

//    freeMemory = Memory.getFreeMemory

//    initExecutor
//    this.tdEnum = tdEnum
//    this.size = 5
//
//    initExecutor
//    exec.invokeAll(runners)
//    exec.shutdown()

//    initExecutor
//    exec.awaitTermination(10, TimeUnit.SECONDS)
//    import Memory._
//
//    getFreeMemory
//    println(s"Freeing up $freeMemory")
//    tryToFreeUpSpaceG(freeMemory)    
//    getFreeMemory
  }
  
  override def tearDown(i: Int, tdEnum: EnumType, memScope: e.memoization.MemoizationScope): Unit = {
//    exec.shutdown()
      exec.shutdown();
      exec.awaitTermination(2, TimeUnit.MINUTES);
      exec = null
  }

  val enumeratorFunction =
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
          e.memoization.Chain[(Int, Int), (Tree, Tree), Node](rootLeftSizePairs, leftRightPairs,
            (p1: (Int, Int), p2: (Tree, Tree)) => {
              val ((leftSize, currRoot), (leftTree, rightTree)) = (p1, p2)

              Node(leftTree, currRoot, rightTree)
            })

        allNodes
      }
    }

  def constructEnumerator(implicit ms: e.memoization.MemoizationScope) = {
    val enum = Depend.memoizedConcurrentNoScope(enumeratorFunction)
    ms.add(enum)
    enum
  }

}
