package edu.luc.cs.laufer.cs371.minijs

import org.scalatest.funsuite.AnyFunSuite
import TestFixtures.*
import Expr.*
import Statement.*

class TestMiniJSParser extends AnyFunSuite:

  private def parseExpr(input: String) = MiniJSParser.parseAll(MiniJSParser.expression, input)

  test("expression fixture complex1 parses") {
    assert(parseExpr(complex1string).get == complex1)
  }

  test("expression fixture complex1 parses with surrounding whitespace") {
    assert(parseExpr(complex1string2).get == complex1)
  }

  test("expression fixture complex2 parses") {
    assert(parseExpr(complex2string).get == complex2)
  }

  test("additive operators are left-associative") {
    assert(parseExpr("1 - 2 + 3 - 4").get ==
      Minus(Plus(Minus(Constant(1), Constant(2)), Constant(3)), Constant(4)))
  }

  test("multiplicative operators are left-associative") {
    assert(parseExpr("8 / 4 * 2 % 3").get ==
      Mod(Times(Div(Constant(8), Constant(4)), Constant(2)), Constant(3)))
  }

  test("multiplicative operators bind tighter than additive ones") {
    assert(parseExpr("1 + 2 * 3 - 4 / 5").get ==
      Minus(Plus(Constant(1), Times(Constant(2), Constant(3))), Div(Constant(4), Constant(5))))
  }

  test("unary minus is a node, not a negative literal") {
    assert(parseExpr("-3").get == UMinus(Constant(3)))
    assert(parseExpr("--x").get == UMinus(UMinus(Variable("x"))))
  }

  test("unary plus is dropped") {
    assert(parseExpr("+3").get == Constant(3))
    assert(parseExpr("-+x").get == UMinus(Variable("x")))
  }

  test("identifiers may contain digits and underscores after the first letter") {
    assert(parseExpr("a1_b2").get == Variable("a1_b2"))
  }

  test("identifiers may start with a keyword") {
    assert(MiniJSParser.parse("iffy = 1; whiley = 2; elsewhere;").get == List(
      Assign(Variable("iffy"), Constant(1)),
      Assign(Variable("whiley"), Constant(2)),
      ExprStatement(Variable("elsewhere"))
    ))
  }

  test("empty input is an empty program") {
    assert(MiniJSParser.parse("").get == Nil)
    assert(MiniJSParser.parse("  \n\t ").get == Nil)
  }

  test("empty blocks are allowed") {
    assert(MiniJSParser.parse("{}").get == List(Block(Nil)))
    assert(MiniJSParser.parse("if (x) {} else {}").get ==
      List(Cond(Variable("x"), Block(Nil), Some(Block(Nil)))))
    assert(MiniJSParser.parse("while (x) {}").get == List(Loop(Variable("x"), Block(Nil))))
  }

  for (input, expected) <- validPrograms do
    test(s"accepts: ${input.replace("\n", "\\n")}") {
      val result = MiniJSParser.parse(input)
      assert(result.successful, result.toString)
      assert(result.get == expected)
    }

  for input <- invalidPrograms do
    test(s"rejects: $input") {
      val result = MiniJSParser.parse(input)
      assert(!result.successful, s"unexpectedly parsed as ${result.getOrElse(Nil)}")
    }

end TestMiniJSParser
