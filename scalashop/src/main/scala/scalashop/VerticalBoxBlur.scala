package scalashop

import org.scalameter._

object VerticalBoxBlurRunner {

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 5,
    Key.exec.maxWarmupRuns -> 10,
    Key.exec.benchRuns -> 10,
    Key.verbose -> true
  ) withWarmer(new Warmer.Default)

  def main(args: Array[String]): Unit = {
    val radius = 3
    val width = 1920
    val height = 1080
    val src = new Img(width, height)
    val dst = new Img(width, height)
    val seqtime = standardConfig measure {
      VerticalBoxBlur.blur(src, dst, 0, width, radius)
    }
    println(s"sequential blur time: $seqtime")

    val numTasks = 32
    val partime = standardConfig measure {
      VerticalBoxBlur.parBlur(src, dst, numTasks, radius)
    }
    println(s"fork/join blur time: $partime")
    println(s"speedup: ${seqtime.value / partime.value}")

    /*val par = standardConfig measure {
      parallel(VerticalBoxBlur.blur(src, dst, 0, width/2, radius),
                VerticalBoxBlur.blur(src, dst, width/2, width, radius))
    }
    println(s"fork/join blur time: $par")*/
  }


}

/** A simple, trivially parallelizable computation. */
object VerticalBoxBlur extends VerticalBoxBlurInterface {

  /** Blurs the columns of the source image `src` into the destination image
   *  `dst`, starting with `from` and ending with `end` (non-inclusive).
   *
   *  Within each column, `blur` traverses the pixels by going from top to
   *  bottom.
   */
  def blur(src: Img, dst: Img, from: Int, end: Int, radius: Int): Unit = {
    // TODO implement this method using the `boxBlurKernel` method
    for {
      x <- from until end
      y <- 0 until src.height
    } yield {
      dst.update(x, y, boxBlurKernel(src, x, y, radius))
    }
  }

  /** Blurs the columns of the source image in parallel using `numTasks` tasks.
    *
    *  Parallelization is done by stripping the source image `src` into
    *  `numTasks` separate strips, where each strip is composed of some number of
    *  columns.
    */
  /*def parBlur(src: Img, dst: Img, numTasks: Int, radius: Int): Unit = {
    // TODO implement using the `task` construct and the `blur` method
    val width = src.width

    val remainder = width / numTasks
    val range = 0 to width by (width / numTasks)
    val strips =
      if (remainder == 0) range.zip(range.tail)
      else range.zip(range.tail) ++ List((width - remainder, width))

    strips.foreach { x =>
      val c = task {
        blur(src, dst, x._1, x._2, radius)
        //dst
        print("++++++++++++++++++++++++++++++++++++++++++++++++++")
      }
      c.join()
    }

  }*/

  def parBlur(src: Img, dst: Img, numTasks: Int, radius: Int): Unit = {
    val width = src.width
    val taskCount = clamp(numTasks, 1, width)
    val mod = width % taskCount

    val range = (0 to width by (width / taskCount))
    val strips =
      if (mod == 0) range.zip(range.tail)
      else range.zip(range.tail) ++ List((width - mod, width))

    val tasks = strips.map {
      case (startX, exclusiveEndX) =>
        task(
          blur(src, dst, startX, exclusiveEndX, radius)
        )
    }

    tasks.foreach { _.join() }
  }

}
