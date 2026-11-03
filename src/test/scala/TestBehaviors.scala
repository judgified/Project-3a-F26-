package edu.luc.cs.laufer.cs371.minijs

import org.json4s.JsonAST.{JInt, JValue}
import org.json4s.native.JsonMethods.parse as parseJson
import org.scalatest.funsuite.AnyFunSuite
import TestFixtures.*
import Expr.*
import Statement.*
import behaviors.*

class TestToJson extends AnyFunSuite:

  given CanEqual[JValue, JValue] = CanEqual.derived

  test("constants become JSON numbers") {
    assert(toJson(Constant(42)) == JInt(42))
  }

  test("variables and unary minus become single-key objects") {
    assert(toJson(Variable("x")) == parseJson("""{ "Variable": "x" }"""))
    assert(toJson(UMinus(Constant(3))) == parseJson("""{ "UMinus": 3 }"""))
  }

  test("binary operators hold their operands in an array") {
    assert(toJson(complex1) == parseJson("""
      { "Div": [
        { "Minus": [
          { "Plus": [1, 2] },
          { "Times": [3, 4] }
        ] },
        5
      ] }
    """))
  }

  test("expression statements and assignments") {
    assert(toJson(ExprStatement(Variable("x"))) == parseJson("""{ "ExprStatement": { "Variable": "x" } }"""))
    assert(toJson(simpleAssign._2) == parseJson("""
      [ { "Assign": { "left": { "Variable": "x" }, "right": 5 } } ]
    """))
  }

  test("conditional without else omits the else field") {
    assert(toJson(ifWithoutElse._2) == parseJson("""
      [ { "Cond": {
        "guard": 1,
        "then": { "Block": [ { "Assign": { "left": { "Variable": "x" }, "right": 2 } } ] }
      } } ]
    """))
  }

  test("conditional with else") {
    assert(toJson(ifWithElse._2) == parseJson("""
      [ { "Cond": {
        "guard": 1,
        "then": { "Block": [ { "Assign": { "left": { "Variable": "x" }, "right": 2 } } ] },
        "else": { "Block": [ { "Assign": { "left": { "Variable": "x" }, "right": 3 } } ] }
      } } ]
    """))
  }

  test("nested loop inside conditional") {
    assert(toJson(nested._2) == parseJson("""
      [ { "Cond": {
        "guard": { "Plus": [ { "Plus": [ { "UMinus": 3 }, 4 ] }, { "Times": [5, 6] } ] },
        "then": { "Block": [
          { "Loop": {
            "guard": 0,
            "body": { "Block": [
              { "Assign": { "left": { "Variable": "x" }, "right": 3 } },
              { "Assign": { "left": { "Variable": "y" }, "right": 5 } },
              { "Block": [ { "Assign": { "left": { "Variable": "xy" }, "right": 88 } } ] }
            ] }
          } }
        ] }
      } } ]
    """))
  }

  test("empty program and empty block") {
    assert(toJson(List.empty[Statement]) == parseJson("[]"))
    assert(toJson(Block(Nil)) == parseJson("""{ "Block": [] }"""))
  }

end TestToJson

class TestUnparser extends AnyFunSuite:

  test("expressions are infix with only the parentheses that are needed") {
    assert(unparse(complex1) == "(1 + 2 - 3 * 4) / 5")
    assert(unparse(complex2) == "(1 + 2 - -3 * 4) % 5")
    assert(unparse(complex3) == "(1 + y2 - 3 * y4) / 5")
  }

  test("right-nested operators of equal precedence keep their parentheses") {
    assert(unparse(Minus(Constant(1), Minus(Constant(2), Constant(3)))) == "1 - (2 - 3)")
    assert(unparse(Div(Constant(8), Times(Constant(4), Constant(2)))) == "8 / (4 * 2)")
    assert(unparse(Minus(Minus(Constant(1), Constant(2)), Constant(3))) == "1 - 2 - 3")
  }

  test("unary minus parenthesizes compound operands only") {
    assert(unparse(UMinus(Variable("x"))) == "-x")
    assert(unparse(UMinus(UMinus(Variable("x")))) == "--x")
    assert(unparse(UMinus(Plus(Variable("x"), Constant(1)))) == "-(x + 1)")
    assert(unparse(Times(UMinus(Constant(3)), Constant(4))) == "-3 * 4")
  }

  test("each top-level statement is on its own line") {
    assert(unparse(twoAssigns._2) == "x = 5;\ny = 7;")
    assert(unparse(exprStatement._2) == "(1 + y2 - 3 * y4) / 5;")
  }

  test("if without else") {
    assert(unparse(ifWithoutElse._2) ==
      """if (1) {
        |  x = 2;
        |}""".stripMargin)
  }

  test("if with else") {
    assert(unparse(ifWithElse._2) ==
      """if (1) {
        |  x = 2;
        |} else {
        |  x = 3;
        |}""".stripMargin)
  }

  test("while loop") {
    assert(unparse(whileLoop._2) ==
      """while (y) {
        |  r = r + x;
        |  y = y - 1;
        |}""".stripMargin)
  }

  test("nested statements are indented two spaces per level") {
    assert(unparse(nested._2) ==
      """if (-3 + 4 + 5 * 6) {
        |  while (0) {
        |    x = 3;
        |    y = 5;
        |    {
        |      xy = 88;
        |    }
        |  }
        |}""".stripMargin)
  }

  test("empty blocks and empty programs") {
    assert(unparse(List(Block(Nil))) == "{\n}")
    assert(unparse(List(Cond(Variable("x"), Block(Nil), Some(Block(Nil))))) == "if (x) {\n} else {\n}")
    assert(unparse(Nil) == "")
  }

  for (input, expected) <- validPrograms do
    test(s"round trip: ${input.replace("\n", "\\n")}") {
      val unparsed = unparse(expected)
      assert(MiniJSParser.parse(unparsed).get == expected, unparsed)
      assert(unparse(MiniJSParser.parse(unparsed).get) == unparsed)
    }

end TestUnparser

class TestMiniJSRepl extends AnyFunSuite:

  test("REPL output includes the echoed source, AST, pretty JSON, and unparsed source") {
    val output = MiniJSRepl.format("x = 5;")
    assert(output.contains("You entered: x = 5;"))
    assert(output.contains("The parsed statements are:\nBlock(List(Assign(Variable(x),Constant(5))))"))
    assert(output.contains("The corresponding JSON structure is:\n["))
    assert(output.contains("The unparsed statements are:\nx = 5;"))
  }

  test("REPL output reports malformed input") {
    val output = MiniJSRepl.format("x = ;")
    assert(output.startsWith("You entered: x = ;\nParse error at line 1, column"))
  }

  test("multiline REPL input parses as one program and formats each statement separately") {
    val output = MiniJSRepl.format("x = 5;\ny = 7;")
    assert(output.contains("You entered: x = 5;\ny = 7;"))
    assert(output.endsWith("The unparsed statements are:\nx = 5;\ny = 7;"))
  }

end TestMiniJSRepl
