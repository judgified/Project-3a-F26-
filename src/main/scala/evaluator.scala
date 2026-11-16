package edu.luc.cs.laufer.cs371.minijs

import scala.collection.mutable
import Expr.*

/** Semantic values arising during interpretation. */
enum Value derives CanEqual:
  case Num(value: Int)

object evaluator:

  import Value.*

  /** Mutable memory mapping variable names to their current values. */
  type Store = mutable.Map[String, Value]

  def newStore(): Store = mutable.Map.empty

  private def number(e: Expr)(using Store): Int = evaluate(e) match
    case Num(n) => n

  def evaluate(e: Expr)(using store: Store): Value = e match
    case Constant(c) => Num(c)
    case Variable(n) => store.getOrElse(n, throw NoSuchFieldException(n))
    case UMinus(r)   => Num(-number(r))
    case Plus(l, r)  => Num(number(l) + number(r))
    case Minus(l, r) => Num(number(l) - number(r))
    case Times(l, r) => Num(number(l) * number(r))
    case Div(l, r)   => Num(number(l) / number(r))
    case Mod(l, r)   => Num(number(l) % number(r))

end evaluator
