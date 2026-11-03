package edu.luc.cs.laufer.cs371.minijs

/** Expressions: side-effect-free computations that produce a value. */
enum Expr derives CanEqual:
  case Constant(value: Int)
  case Variable(name: String)
  case UMinus(expr: Expr)
  case Plus(left: Expr, right: Expr)
  case Minus(left: Expr, right: Expr)
  case Times(left: Expr, right: Expr)
  case Div(left: Expr, right: Expr)
  case Mod(left: Expr, right: Expr)

/** Statements: imperative constructs executed for their effect. */

enum Statement derives CanEqual:
  case ExprStatement(expr: Expr)
  case Assign(left: Expr.Variable, right: Expr)
  case Cond(guard: Expr, thenBranch: Statement.Block, elseBranch: Option[Statement.Block])
  case Loop(guard: Expr, body: Statement.Block)
  case Block(statements: List[Statement])
