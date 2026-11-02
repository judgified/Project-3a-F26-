package edu.luc.cs.laufer.cs371.minijs

object TestFixtures:

  import Expr.*
  import Statement.*

  val complex1 =
    Div(
      Minus(
        Plus(
          Constant(1),
          Constant(2)
        ),
        Times(
          Constant(3),
          Constant(4)
        )
      ),
      Constant(5)
    )

  val complex1string = "((1 + 2) - (3 * 4)) / 5"

  val complex1string2 = "  ((1 + 2) - (3 * 4)) / 5  "

  val complex2 =
    Mod(
      Minus(
        Plus(
          Constant(1),
          Constant(2)
        ),
        Times(
          UMinus(
            Constant(3)
          ),
          Constant(4)
        )
      ),
      Constant(5)
    )

  val complex2string = "((1 + 2) - (-3 * 4)) % 5"

  val complex3 =
    Div(
      Minus(
        Plus(
          Constant(1),
          Variable("y2")
        ),
        Times(
          Constant(3),
          Variable("y4")
        )
      ),
      Constant(5)
    )

  val simpleAssign = "x = 5;" -> List(
    Assign(Variable("x"), Constant(5))
  )

  val twoAssigns = "x = 5 ; y = 7;" -> List(
    Assign(Variable("x"), Constant(5)),
    Assign(Variable("y"), Constant(7))
  )

  val exprStatement = "((1 + y2) - (3 * y4)) / 5;" -> List(
    ExprStatement(complex3)
  )

  val complexAssign = "x = ((1 + y2) - (3 * y4)) / 5;" -> List(
    Assign(Variable("x"), complex3)
  )

  val ifWithoutElse = "if (1) { x = 2; }" -> List(
    Cond(
      Constant(1),
      Block(List(Assign(Variable("x"), Constant(2)))),
      None
    )
  )

  val ifWithElse = "if (1) { x = 2; } else { x = 3; }" -> List(
    Cond(
      Constant(1),
      Block(List(Assign(Variable("x"), Constant(2)))),
      Some(Block(List(Assign(Variable("x"), Constant(3)))))
    )
  )

  val accumulate = List(
    Assign(Variable("r"), Plus(Variable("r"), Variable("x"))),
    Assign(Variable("y"), Plus(Variable("y"), Constant(1)))
  )

  val decrement = List(
    Assign(Variable("r"), Plus(Variable("r"), Variable("x"))),
    Assign(Variable("y"), Minus(Variable("y"), Constant(1)))
  )

  val bareBlock = "{ r = r + x; y = y + 1 ; }" -> List(
    Block(accumulate)
  )

  val ifBlock = "if (4) { r = r + x; y = y + 1; }" -> List(
    Cond(Constant(4), Block(accumulate), None)
  )

  val whileLoop = "while (y) { r = r + x; y = y - 1; }" -> List(
    Loop(Variable("y"), Block(decrement))
  )

  val whileLoopTightSpacing = "while (y) { r = r + x ; y = y - 1 ;}" -> List(
    Loop(Variable("y"), Block(decrement))
  )

  val leftAssociative = "x = 3 + 4 - 5;" -> List(
    Assign(Variable("x"), Minus(Plus(Constant(3), Constant(4)), Constant(5)))
  )

  val loopThenAssign = "while (y) { r = r + x ; y = y - 1 ;}\nx = 3 + 4 - 5;" -> List(
    Loop(Variable("y"), Block(decrement)),
    Assign(Variable("x"), Minus(Plus(Constant(3), Constant(4)), Constant(5)))
  )

  val nested = "if(-3+4+5*6){while(0){x=3;y=5;{xy=88;}}}" -> List(
    Cond(
      Plus(
        Plus(UMinus(Constant(3)), Constant(4)),
        Times(Constant(5), Constant(6))
      ),
      Block(List(
        Loop(
          Constant(0),
          Block(List(
            Assign(Variable("x"), Constant(3)),
            Assign(Variable("y"), Constant(5)),
            Block(List(Assign(Variable("xy"), Constant(88))))
          ))
        )
      )),
      None
    )
  )

  val validPrograms = List(
    simpleAssign,
    twoAssigns,
    exprStatement,
    complexAssign,
    ifWithoutElse,
    ifWithElse,
    bareBlock,
    ifBlock,
    whileLoop,
    whileLoopTightSpacing,
    leftAssociative,
    loopThenAssign,
    nested
  )

  val invalidPrograms = List(
    "x = 5",
    "x = 5 ; y = 7",
    "((1 + y2) - (3 * y4)) / 5",
    "((1 + y2) - (3 * y4) / 5;",
    "x = ((1 + y2) - (3 * y4))) / 5;",
    "x = ;",
    "x == 5;",
    "5 = x;",
    "x + 1 = 5;",
    "1x = 5;",
    "_x = 5;",
    "x = 5;;",
    "x = 3 +;",
    "x = * 3;",
    "if (1) x = 2;",
    "if 1 { x = 2; }",
    "if (1) { x = 2; } else x = 3;",
    "if (1) { x = 2; } else",
    "if () { x = 2; }",
    "if (1) { x = 2 }",
    "else { x = 3; }",
    "{ r = r + x; y = y + 1 ;",
    "r = r + x; y = y + 1 ; }",
    "if (4) { r = r + x; y = y + 1;",
    "while (y) r = r + x;",
    "while y { r = r + x; }",
    "while (y) { r = r + x; y = y - 1; ",
    "while (y;) { r = r + x; }",
    "if = 3;",
    "while = 3;",
    "else = 3;",
    "x = if;",
    "while;",
    "x = 99999999999;"
  )

end TestFixtures
