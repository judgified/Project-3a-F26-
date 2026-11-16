package edu.luc.cs.laufer.cs371.minijs

import scala.util.Try
import org.scalatest.funsuite.AnyFunSuite
import TestFixtures.*
import Value.*
import evaluator.*

class TestEvaluator extends AnyFunSuite:

  /** Parses and interprets `input` in a fresh store seeded with `initial`, returning the result and final memory. */
  private def run(input: String, initial: (String, Int)*): (Try[Value], Map[String, Value]) =
    given store: Store = newStore()
    initial.foreach((name, value) => store(name) = Num(value))
    (interpret(MiniJSParser.parse(input).get), store.toMap)

  private def memory(entries: (String, Int)*): Map[String, Value] =
    entries.map((name, value) => name -> Num(value)).toMap

  // ---------- Expressions and variables ----------

  test("expression fixtures evaluate with integer arithmetic") {
    given Store = newStore()
    assert(evaluate(complex1) == Num(-1))
    assert(evaluate(complex2) == Num(0))
  }

  test("unary minus negates its operand") {
    val (result, _) = run("-x; ", "x" -> 5)
    assert(result.get == Num(-5))
    assert(run("--x;", "x" -> 5)._1.get == Num(5))
  }

  test("a variable evaluates to its current value in memory") {
    val (result, mem) = run("x + 1;", "x" -> 41)
    assert(result.get == Num(42))
    assert(mem == memory("x" -> 41))
  }

  test("an undefined variable fails with NoSuchFieldException and leaves memory unchanged") {
    val (result, mem) = run("z;", "y" -> 0, "x" -> 2, "r" -> 6)
    val failure = intercept[NoSuchFieldException](result.get)
    assert(failure.getMessage == "z")
    assert(mem == memory("y" -> 0, "x" -> 2, "r" -> 6))
  }

  test("division and remainder by zero fail with ArithmeticException") {
    intercept[ArithmeticException](run("1 / 0;")._1.get)
    intercept[ArithmeticException](run("5 % (x - x);", "x" -> 3)._1.get)
  }

  // ---------- Assignment ----------

  test("assignment defines a previously unknown variable and evaluates to void") {
    val (result, mem) = run("x = 3;")
    assert(result.get == Void)
    assert(mem == memory("x" -> 3))
  }

  test("assignment overwrites an existing variable using its old value") {
    val (result, mem) = run("x = x + 1;", "x" -> 9)
    assert(result.get == Void)
    assert(mem == memory("x" -> 10))
  }

  test("an assignment whose right side fails does not define the variable") {
    val (result, mem) = run("x = z + 1;")
    intercept[NoSuchFieldException](result.get)
    assert(mem.isEmpty)
  }

  test("variables assigned inside a block remain visible after it") {
    val (result, mem) = run("{ y = 1; } y + 1;")
    assert(result.get == Num(2))
    assert(mem == memory("y" -> 1))
  }

  // ---------- Conditional ----------

  test("a nonzero guard selects the then branch, whose value is the result") {
    assert(run("if (1) { 5; } else { 6; }")._1.get == Num(5))
    assert(run("if (-1) { 5; } else { 6; }")._1.get == Num(5))
  }

  test("a zero guard selects the else branch") {
    val (result, mem) = run("if (x - 3) { y = 1; } else { y = 2; y; }", "x" -> 3)
    assert(result.get == Num(2))
    assert(mem == memory("x" -> 3, "y" -> 2))
  }

  test("a zero guard without else evaluates to void and changes nothing") {
    val (result, mem) = run("if (0) { x = 1; }")
    assert(result.get == Void)
    assert(mem.isEmpty)
  }

  test("the branch not taken is not evaluated") {
    val (result, mem) = run("if (1) { x = 1; } else { z; }")
    assert(result.isSuccess)
    assert(mem == memory("x" -> 1))
  }

  test("a conditional with an undefined guard variable fails") {
    intercept[NoSuchFieldException](run("if (z) { x = 1; }")._1.get)
  }

  // ---------- Loop ----------

  test("loop example from the assignment evaluates to void and updates memory") {
    val (result, mem) = run("x = 2; y = 3; r = 0; while (y) { r = r + x ; y = y - 1; }")
    assert(result.get == Void)
    assert(mem == memory("y" -> 0, "x" -> 2, "r" -> 6))
  }

  test("a loop whose guard is initially zero never runs its body") {
    val (result, mem) = run("while (0) { z; }")
    assert(result.get == Void)
    assert(mem.isEmpty)
  }

  test("nested loops") {
    val (result, mem) = run("x = 0; i = 3; while (i) { j = 2; while (j) { x = x + 1; j = j - 1; } i = i - 1; }")
    assert(result.get == Void)
    assert(mem == memory("x" -> 6, "i" -> 0, "j" -> 0))
  }

  test("long-running loops do not overflow the stack") {
    val (result, mem) = run("i = 1000000; while (i) { i = i - 1; }")
    assert(result.get == Void)
    assert(mem == memory("i" -> 0))
  }

  test("a loop with an undefined guard variable fails") {
    intercept[NoSuchFieldException](run("while (z) { }")._1.get)
  }

  test("a failure inside a loop keeps the updates made before it") {
    val (result, mem) = run("i = 3; while (i) { i = i - 1; if (i - 1) { } else { z; } }")
    intercept[NoSuchFieldException](result.get)
    assert(mem == memory("i" -> 1))
  }

  // ---------- Block and program ----------

  test("block example from the assignment evaluates to its last statement") {
    val (result, mem) = run("{ x = 7; x; }")
    assert(result.get == Num(7))
    assert(mem == memory("x" -> 7))
  }

  test("a block ending in an assignment evaluates to void") {
    val (result, mem) = run("{ 4; x = 1; }")
    assert(result.get == Void)
    assert(mem == memory("x" -> 1))
  }

  test("empty blocks and empty programs evaluate to void") {
    assert(run("{ }")._1.get == Void)
    assert(run("")._1.get == Void)
  }

  test("nested blocks evaluate to the innermost last value") {
    assert(run("{ 1; { 2; { 3; } } }")._1.get == Num(3))
  }

  test("a program evaluates to the value of its last statement") {
    assert(run("x = 2; y = x * 3; y - 1;")._1.get == Num(5))
  }

  test("a failing statement stops the program but keeps earlier assignments") {
    val (result, mem) = run("x = 1; z; y = 2;")
    intercept[NoSuchFieldException](result.get)
    assert(mem == memory("x" -> 1))
  }

  test("memory persists across programs interpreted with the same store") {
    given store: Store = newStore()
    assert(interpret(MiniJSParser.parse("x = 3;").get).get == Void)
    assert(interpret(MiniJSParser.parse("x + 1;").get).get == Num(4))
    assert(store.toMap == memory("x" -> 3))
  }

end TestEvaluator
