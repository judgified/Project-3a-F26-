package edu.luc.cs.laufer.cs371.minijs

import org.json4s.JsonAST.{JArray, JInt, JObject, JString, JValue}
import Expr.*
import Statement.*

object behaviors:

  // ---------- JSON converter ----------

  private def node(name: String, value: JValue): JValue = JObject(name -> value)

  private def binary(name: String, left: Expr, right: Expr): JValue =
    node(name, JArray(List(toJson(left), toJson(right))))

  def toJson(e: Expr): JValue = e match
    case Constant(c) => JInt(BigInt(c))
    case Variable(n) => node("Variable", JString(n))
    case UMinus(r)   => node("UMinus", toJson(r))
    case Plus(l, r)  => binary("Plus", l, r)
    case Minus(l, r) => binary("Minus", l, r)
    case Times(l, r) => binary("Times", l, r)
    case Div(l, r)   => binary("Div", l, r)
    case Mod(l, r)   => binary("Mod", l, r)

  def toJson(s: Statement): JValue = s match
    case ExprStatement(e) => node("ExprStatement", toJson(e))
    case Assign(l, r)     => node("Assign", JObject("left" -> toJson(l), "right" -> toJson(r)))
    case Cond(g, t, e)    =>
      val branches = List("guard" -> toJson(g), "then" -> toJson(t)) ++ e.map(b => "else" -> toJson(b))
      node("Cond", JObject(branches))
    case Loop(g, b)       => node("Loop", JObject("guard" -> toJson(g), "body" -> toJson(b)))
    case Block(ss)        => node("Block", JArray(ss.map(s => toJson(s))))

  def toJson(program: List[Statement]): JValue = JArray(program.map(s => toJson(s)))

  // ---------- Unparser ----------

  /** Binding strength of each expression form; higher binds tighter. */
  private def precedence(e: Expr): Int = e match
    case Plus(_, _) | Minus(_, _)            => 1
    case Times(_, _) | Div(_, _) | Mod(_, _) => 2
    case UMinus(_)                           => 3
    case Constant(_) | Variable(_)           => 4

  private def operand(e: Expr, minPrecedence: Int): String =
    if precedence(e) < minPrecedence then s"(${unparse(e)})" else unparse(e)

  /** All binary operators are left-associative, so only a right operand at the same level needs parentheses. */
  private def infix(left: Expr, op: String, right: Expr, level: Int): String =
    s"${operand(left, level)} $op ${operand(right, level + 1)}"

  def unparse(e: Expr): String = e match
    case Constant(c) => c.toString
    case Variable(n) => n
    case UMinus(r)   => "-" + operand(r, precedence(e))
    case Plus(l, r)  => infix(l, "+", r, 1)
    case Minus(l, r) => infix(l, "-", r, 1)
    case Times(l, r) => infix(l, "*", r, 2)
    case Div(l, r)   => infix(l, "/", r, 2)
    case Mod(l, r)   => infix(l, "%", r, 2)

  private def indent(depth: Int): String = "  " * depth

  /** Renders a block whose opening brace continues the current line. */
  private def unparseBlock(b: Block, depth: Int): String =
    ("{" :: b.statements.map(s => unparse(s, depth + 1)) ::: List(indent(depth) + "}")).mkString("\n")

  def unparse(s: Statement, depth: Int): String = indent(depth) + (s match
    case ExprStatement(e)    => unparse(e) + ";"
    case Assign(l, r)        => s"${unparse(l)} = ${unparse(r)};"
    case Cond(g, t, None)    => s"if (${unparse(g)}) ${unparseBlock(t, depth)}"
    case Cond(g, t, Some(e)) => s"if (${unparse(g)}) ${unparseBlock(t, depth)} else ${unparseBlock(e, depth)}"
    case Loop(g, b)          => s"while (${unparse(g)}) ${unparseBlock(b, depth)}"
    case b: Block            => unparseBlock(b, depth)
  )

  def unparse(program: List[Statement]): String = program.map(s => unparse(s, 0)).mkString("\n")

end behaviors
