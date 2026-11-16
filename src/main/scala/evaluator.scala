package edu.luc.cs.laufer.cs371.minijs

import scala.collection.mutable
import scala.util.Try
import Expr.*
import Statement.*

/** Semantic values arising during interpretation. */
enum Value derives CanEqual:
  case Num(value: Int)

object evaluator:

  import Value.*

  /** Mutable memory mapping variable names to their current values. */
  type Store = mutable.Map[String, Value]

  def newStore(): Store = mutable.Map.empty

  /** The value of assignments, loops, and empty blocks, which have no meaningful result. */
  val Void: Value = Num(0)

  private def number(e: Expr)(using Store): Int = evaluate(e) match
    case Num(n) => n

  /** As in JavaScript, zero is false and every other number is true. */
  private def isTrue(e: Expr)(using Store): Boolean = number(e) != 0

  def evaluate(e: Expr)(using store: Store): Value = e match
    case Constant(c) => Num(c)
    case Variable(n) => store.getOrElse(n, throw NoSuchFieldException(n))
    case UMinus(r)   => Num(-number(r))
    case Plus(l, r)  => Num(number(l) + number(r))
    case Minus(l, r) => Num(number(l) - number(r))
    case Times(l, r) => Num(number(l) * number(r))
    case Div(l, r)   => Num(number(l) / number(r))
    case Mod(l, r)   => Num(number(l) % number(r))

  def evaluate(s: Statement)(using store: Store): Value = s match
    case ExprStatement(e)    => evaluate(e)
    case Assign(Variable(n), r) =>
      store(n) = evaluate(r)
      Void
    case Cond(g, t, e)       => if isTrue(g) then evaluate(t) else e.fold(Void)(b => evaluate(b))
    case Loop(g, b)          =>
      while isTrue(g) do evaluate(b): Unit
      Void
    case Block(ss)           => evaluate(ss)

  /** A sequence of statements evaluates to the value of its last statement, or `Void` if empty. */
  def evaluate(program: List[Statement])(using Store): Value =
    program.foldLeft(Void)((_, s) => evaluate(s))

  /** Top-level entry point: any runtime failure becomes a `Failure` instead of escaping. */
  def interpret(program: List[Statement])(using Store): Try[Value] = Try(evaluate(program))

end evaluator
