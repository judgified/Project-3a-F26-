package edu.luc.cs.laufer.cs371.minijs

import org.jline.reader.{EndOfFileException, LineReader, LineReaderBuilder, UserInterruptException}
import org.jline.terminal.TerminalBuilder
import org.json4s.native.JsonMethods.{pretty, render}
import scala.util.{Failure, Success} //used for the tests
import behaviors.*
import evaluator.*


object MiniJSRepl:

  def errorMessage(input: String, message: String): String =
    s"Error in input '$input': $message"

  def format(input: String): String =
    format(input, newStore())

  def format(input: String, store: Store): String =
    val result = MiniJSParser.parse(input) match
      case MiniJSParser.Success(statements, _) =>
        val details = List(
          "The parsed statements are:",
          Statement.Block(statements).toString,
          "The corresponding JSON structure is:",
          pretty(render(toJson(statements))),
          "The unparsed statements are:",
          unparse(statements)
        ).mkString("\n")
        val evaluation = interpret(statements)(using store) match
          case Success(value) => s"The result is:\n$value"
          case Failure(error) => errorMessage(input, error.getMessage)
        s"$details\n$evaluation"
      case failure: MiniJSParser.NoSuccess =>
        s"Parse error at line ${failure.next.pos.line}, column ${failure.next.pos.column}: ${failure.msg}"
    s"You entered: $input\n$result"

  /** Reads lines until a blank one. Ctrl-C discards everything entered so far and yields an empty program. */
  def readProgram(reader: LineReader): String =
    val lines = List.newBuilder[String]
    try
      var line = reader.readLine("minijs> ")
      while !line.isBlank do
        lines += line
        line = reader.readLine(".....> ")
      lines.result().mkString("\n")
    catch
      case _: UserInterruptException => ""

  def main(args: Array[String]): Unit =
    val terminal = TerminalBuilder.builder().system(true).build()
    try
      val reader = LineReaderBuilder.builder().terminal(terminal).build()
      try
        val store = newStore()
        while true do
          val input = readProgram(reader)
          if !input.isBlank then println(format(input, store))
      catch
        case _: EndOfFileException => ()
    finally
      terminal.close()

end MiniJSRepl
