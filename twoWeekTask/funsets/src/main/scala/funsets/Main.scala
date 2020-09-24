package funsets

object Main extends App {
  import FunSets._
  println(contains(singletonSet(1), 1))



  //val ff = Set(2, 5, 9).forall()

  println(contains(intersect(singletonSet(5), singletonSet(5)), 5))

  print(forall(singletonSet(10000), x => x >= -1000 && x <= 1000))
}
