package funsets

/**
 * 2. Purely Functional Sets.
 */
trait FunSets extends FunSetsInterface {
  /**
   * We represent a set by its characteristic function, i.e.
   * its `contains` predicate.
   */
  override type FunSet = Int => Boolean

  //type Set = Int => Boolean

  /**
   * Indicates whether a set contains a given element.
   */
  def contains(s: FunSet, elem: Int): Boolean = s(elem)

  /**
   * Returns the set of the one given element.
   */
  def singletonSet(elem: Int): FunSet = e => e == elem


  /**
   * Returns the union of the two given sets,
   * the sets of all elements that are in either `s` or `t`.
   */
  def union(s: FunSet, t: FunSet): FunSet = (e: Int) => s(e) || t(e)

  /**
   * Returns the intersection of the two given sets,
   * the set of all elements that are both in `s` and `t`.
   */
  def intersect(s: FunSet, t: FunSet): FunSet = (e: Int) => s(e) && t(e)

  /**
   * Returns the difference of the two given sets,
   * the set of all elements of `s` that are not in `t`.
   */
  def diff(s: FunSet, t: FunSet): FunSet = (e: Int) => s(e) && !(t(e))

  /**
   * Returns the subset of `s` for which `p` holds.
   */
  def filter(s: FunSet, p: Int => Boolean): FunSet = intersect(s, p)


  /**
   * The bounds for `forall` and `exists` are +/- 1000.
   */
  val bound = 1000

  /**
   * Returns whether all bounded integers within `s` satisfy `p`.
   */
  def forall(s: FunSet, p: Int => Boolean): Boolean = {
    def iter(a: Int): Boolean = {
      if (a > bound) true  //(a > bound) contains(s, a) && p(a)
      else if (contains(s, a) && !p(a)) false
      else iter(a + 1)
    }
    iter(-bound)
  }

  /**
   * Returns whether there exists a bounded integer within `s`
   * that satisfies `p`.
   */
  def exists(s: FunSet, p: Int => Boolean): Boolean = !forall(s, x => !p(x))

  /*def exists(s: FunSet, p: Int => Boolean): Boolean = {
    def iter(a: Int): Boolean = {
      if (contains(s, a) && p(a)) true  //(a > bound)
      else if (contains(s, a) && !p(a)) false
      else iter(a + 1)
    }
    iter(-bound)
  }*/

  /**
   * Returns a set transformed by applying `f` to each element of `s`.
   */
  def map(s: FunSet, f: Int => Int): FunSet = x => exists(s, y => f(y) == x)

  /**
   * Displays the contents of a set
   */
  def toString(s: FunSet): String = {
    val xs = for (i <- -bound to bound if contains(s, i)) yield i
    xs.mkString("{", ",", "}")
  }

  /**
   * Prints the contents of a set on the console.
   */
  def printSet(s: FunSet): Unit = {
    println(toString(s))
  }
}

object FunSets extends FunSets