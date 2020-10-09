"GjkjHJHHkljk".toLowerCase

List('d' -> 9):::List[(Char, Int)]()

List[(Char, Int)]() ::: List(('a',1)) ::: List(('a',2))


val numbers = List("1", "2", "3", "4", "5")
numbers.foldLeft("0") { (m: String, n: String) =>
  println("m: " + m + " n: " + n); m + n }