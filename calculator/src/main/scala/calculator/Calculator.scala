package calculator

sealed abstract class Expr
final case class Literal(v: Double) extends Expr
final case class Ref(name: String) extends Expr
final case class Plus(a: Expr, b: Expr) extends Expr
final case class Minus(a: Expr, b: Expr) extends Expr
final case class Times(a: Expr, b: Expr) extends Expr
final case class Divide(a: Expr, b: Expr) extends Expr

object Calculator extends CalculatorInterface {
  def computeValues(
      namedExpressions: Map[String, Signal[Expr]]): Map[String, Signal[Double]] = {
    namedExpressions.map { x =>
      (x._1, Signal(eval(getReferenceExpr(x._1, namedExpressions), namedExpressions)) )
      /*match {
        case e: Literal => (x._1, Signal(e.v))
        case e: Ref => (x._1, Signal(eval(e, namedExpressions)))
        case e: Plus => (x._1, Signal(eval(e, namedExpressions)))
        case e: Minus => (x._1, Signal(eval(e, namedExpressions)))
        case e: Times => (x._1, Signal(eval(e, namedExpressions)))
        case e: Divide => (x._1, Signal(eval(e, namedExpressions)))
      }*/
    }
  }

  def eval(expr: Expr, references: Map[String, Signal[Expr]]): Double = expr match {
    case e: Literal => e.v
    case e: Ref => eval(getReferenceExpr(e.name, references), references)
    case e: Plus => eval(e.a, references) + eval(e.b, references)
    case e: Minus => eval(e.a, references) - eval(e.b, references)
    case e: Times => eval(e.a, references) * eval(e.b, references)
    case e: Divide => eval(e.a, references) / eval(e.b, references)
  }

  /** Get the Expr for a referenced variables.
   *  If the variable is not known, returns a literal NaN.
   */
  private def getReferenceExpr(name: String,
      references: Map[String, Signal[Expr]]) = {
    references.get(name).fold[Expr] {
      Literal(Double.NaN)
    } { exprSignal =>
      exprSignal()
    }
  }
}
