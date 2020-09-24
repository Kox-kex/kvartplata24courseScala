package funsets

import org.junit._

/**
 * This class is a test suite for the methods in object FunSets.
 *
 * To run this test suite, start "sbt" then run the "test" command.
 */
class FunSetSuite {

  import FunSets._

  @Test def `contains is implemented`: Unit = {
    assert(contains(x => true, 100))
  }

  /**
   * When writing tests, one would often like to re-use certain values for multiple
   * tests. For instance, we would like to create an Int-set and have multiple test
   * about it.
   *
   * Instead of copy-pasting the code for creating the set into every test, we can
   * store it in the test class using a val:
   *
   *   val s1 = singletonSet(1)
   *
   * However, what happens if the method "singletonSet" has a bug and crashes? Then
   * the test methods are not even executed, because creating an instance of the
   * test class fails!
   *
   * Therefore, we put the shared values into a separate trait (traits are like
   * abstract classes), and create an instance inside each test method.
   *
   */

  trait TestSets {
    val s1 = singletonSet(1)
    val s2 = singletonSet(2)
    val s3 = singletonSet(3)
    val s4 = singletonSet(4)
  }

  /**
   * This test is currently disabled (by using @Ignore) because the method
   * "singletonSet" is not yet implemented and the test would fail.
   *
   * Once you finish your implementation of "singletonSet", remvoe the
   * @Ignore annotation.
   */
  //@Ignore("not ready yet")
  @Test def `singleton set one contains one`: Unit = {

    /**
     * We create a new instance of the "TestSets" trait, this gives us access
     * to the values "s1" to "s3".
     */
    new TestSets {
      /**
       * The string argument of "assert" is a message that is printed in case
       * the test fails. This helps identifying which assertion failed.
       */
      assert(contains(s1, 1), "Singleton")
      assert(contains(s2, 2), "Singleton")
      assert(contains(s3, 3), "Singleton")
    }
  }

  @Test def `union contains all elements of each set`: Unit = {
    new TestSets {
      val s = union(s1, s2)
      assert(contains(s, 1), "Union 1")
      assert(contains(s, 2), "Union 2")
      assert(!contains(s, 3), "Union 3")
    }
  }

  @Test def `when intersect (s1, s1) then true`: Unit = {
    new TestSets {
      val s = intersect(s1, s1)
      assert(contains(s, 1), "intersect 1")
      assert(!contains(s, 2), "intersect 2")
      assert(!contains(s, 3), "intersect 3")
    }
  }

  @Test def `diff difference of the two given sets`: Unit = {
    new TestSets {
      val s = diff(s1, s2)
      assert(contains(s, 1), "diff 1")
      assert(!contains(s, 2), "diff 2")
      assert(!contains(s, 3), "diff 3")

      val xs = union(s1, s2)  // {1, 2}
      val ys = union(xs, s3)  // {1, 2, 3}
      val zs = diff(ys, xs)    // {1, 2, 3} diff {1, 2} = {3}
      assert(contains(zs, 3), "diff contains 3")
      assert(!contains(zs, 1), "diff not contains 1")
      assert(!contains(zs, 2), "diff not contains 2")
    }
  }

  @Test def `filter returns the subset of s for which p holds`: Unit = {
    new TestSets {
      val xs = union(s1, s2)  // {1, 2}
      val ys = union(xs, s3)  // {1, 2, 3}
      val f = filter(ys,  x => x > 1 && x < 3)

      assert(contains(f, 2), "filter contains 2")
      assert(!contains(f, 1), "filter not contains 1")
      assert(!contains(f, 3), "filter not contains 3")
    }
  }

  @Test def `forall returns whether all bounded integers within s satisfy p`: Unit = {
    new TestSets {
      val xs = union(s1, s2)  // {1, 2}
      val ys = union(xs, s3)  // {1, 2, 3}
      val f = forall(ys,  x => x >= 1 && x < 7)

      assert(f, "filter contains 3")

      val s = union(union(s1, s2), s3)  // {1, 2, 3}
      assert(forall(s, x => x > -1 && x < 7), " all items are (x > -1) ?")
      assert(!forall(s, x => x > 2), " all items are (x > 2) ?")

      val y = union(s2, s4)
      assert(forall( y, x => ((x % 2) == 0)), " are all items even ?")
    }
  }

  @Test def `exists returns whether there exists a bounded integer within s that satisfies p`: Unit = {
    new TestSets {
      val s = union(union(s1, s2), s3)  // {1, 2, 3}
      assert(exists(s, x => x > -1), "there is at least one item greater than -1?")
      assert(!exists(s, x => x > 5), "there is at least one item greater than 5?")
      assert(exists(s, x => ((x % 2) == 0)), "there is at least one even?")
    }
  }

  @Test def `map returns a set transformed by applying f to each element of s`: Unit = {
    new TestSets {
      val s = union(union(s1, s2), s3)  // {1, 2, 3}
      val m = map(s, x => x + 1)
      assert(!contains(m, 1), "map 1")
      assert(contains(m, 2), "map 2")
      assert(contains(m, 3), "map 3")
    }
  }

    @Rule def individualTestTimeout = new org.junit.rules.Timeout(10 * 1000)
}
