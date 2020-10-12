package quickcheck

import org.scalacheck._
import Arbitrary._
import Gen._
import Prop._


abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {

  lazy val genHeap: Gen[H] =
    for {
    n <- arbitrary[A]
  } yield insert(n, empty)


  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

  property("gen1") = forAll { (h: H) =>
    val m = if (isEmpty(h)) 0 else findMin(h)
    findMin(insert(m, h)) == m
  }

  property("gen2") = forAll { (a1: A, a2: A) =>
    val m = insert(a1, insert(a2, empty))
    findMin(m) == List(a1, a2).min
  }

  property("gen3") = forAll { (a1: A) =>
    val m = insert(a1, empty)
    deleteMin(m) == empty
  }

  property("gen4") = forAll { (h1: H) =>
    def seq(h1: H): List[A] = {
      if(isEmpty(h1)) List()
      else findMin(h1) :: seq(deleteMin(h1))
    }
    seq(h1) == seq(h1).sorted
  }
  
}
