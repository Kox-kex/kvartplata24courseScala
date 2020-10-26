import java.util.concurrent._
import scala.util.DynamicVariable

import org.scalameter._

package object scalashop extends BoxBlurKernelInterface {

  /** The value of every pixel is represented as a 32 bit integer. */
  type RGBA = Int

  /** Returns the red component. */
  def red(c: RGBA): Int = (0xff000000 & c) >>> 24

  /** Returns the green component. */
  def green(c: RGBA): Int = (0x00ff0000 & c) >>> 16

  /** Returns the blue component. */
  def blue(c: RGBA): Int = (0x0000ff00 & c) >>> 8

  /** Returns the alpha component. */
  def alpha(c: RGBA): Int = (0x000000ff & c) >>> 0

  /** Used to create an RGBA value from separate components. */
  def rgba(r: Int, g: Int, b: Int, a: Int): RGBA = {
    (r << 24) | (g << 16) | (b << 8) | (a << 0)
  }

  /** Restricts the integer into the specified range. */
  def clamp(v: Int, min: Int, max: Int): Int = {
    if (v < min) min
    else if (v > max) max
    else v
  }

  /** Image is a two-dimensional matrix of pixel values. */
  class Img(val width: Int, val height: Int, private val data: Array[RGBA]) {
    def this(w: Int, h: Int) = this(w, h, new Array(w * h))
    def apply(x: Int, y: Int): RGBA = data(y * width + x)
    def update(x: Int, y: Int, c: RGBA): Unit = data(y * width + x) = c
  }

  /** Computes the blurred RGBA value of a single pixel of the input image. */
  def boxBlurKernel(src: Img, x: Int, y: Int, radius: Int): RGBA = {

    // TODO implement using while loops
    if (radius == 0) return src(x, y)

    getAvgSurroundPixelValue(src, x, y, radius)
  }




  def getOptPixelValue(src: Img, x: Int, y: Int): Option[RGBA] = {
    val clampedX = clamp(x, 0, src.width - 1)
    val clampedY = clamp(y, 0, src.height - 1)

    if (clampedX == x && clampedY == y) Some(src(x, y))
    else None
  }

  def getAvgSurroundPixelValue(src: Img,
                               targetX: Int,
                               targetY: Int,
                               radius: Int): RGBA = {

    var r = 0
    var g = 0
    var b = 0
    var a = 0
    var count = 0

    val left = targetX - radius
    val right = targetX + radius
    val top = targetY - radius
    val bottom = targetY + radius

    var x = left
    while (x <= right) {
      var y = top

      while(y <= bottom) {

        getOptPixelValue(src, x, y) match {
          case Some(v) =>
            count += 1
            r += red(v)
            g += green(v)
            b += blue(v)
            a += alpha(v)
          case None => /** do nothing */
        }

        y += 1
      }

      x += 1
    }

    if (count == 0) rgba(r, g, b, a)
    else rgba(r / count, g / count, b / count, a / count)
  }




  val forkJoinPool = new ForkJoinPool

  abstract class TaskScheduler {
    def schedule[T](body: => T): ForkJoinTask[T]
    def parallel[A, B](taskA: => A, taskB: => B): (A, B) = {
      val right = task {
        taskB
      }
      val left = taskA
      (left, right.join())
    }
  }

  class DefaultTaskScheduler extends TaskScheduler {
    def schedule[T](body: => T): ForkJoinTask[T] = {
      val t = new RecursiveTask[T] {
        def compute = body
      }
      Thread.currentThread match {
        case wt: ForkJoinWorkerThread =>
          t.fork()
        case _ =>
          forkJoinPool.execute(t)
      }
      t
    }
  }

  val scheduler =
    new DynamicVariable[TaskScheduler](new DefaultTaskScheduler)

  def task[T](body: => T): ForkJoinTask[T] = {
    scheduler.value.schedule(body)
  }

  def parallel[A, B](taskA: => A, taskB: => B): (A, B) = {
    scheduler.value.parallel(taskA, taskB)
  }


  def parallel[A, B, C, D](taskA: => A, taskB: => B, taskC: => C, taskD: => D): (A, B, C, D) = {
    val ta = task { taskA }
    val tb = task { taskB }
    val tc = task { taskC }
    val td = taskD
    (ta.join(), tb.join(), tc.join(), td)
  }

  // Workaround Dotty's handling of the existential type KeyValue
  implicit def keyValueCoerce[T](kv: (Key[T], T)): KeyValue = {
    kv.asInstanceOf[KeyValue]
  }
}
