package edu.luc.cs.laufer.cs371.minijs

import org.jline.reader.{EndOfFileException, LineReader, LineReaderBuilder, UserInterruptException}
import org.jline.terminal.TerminalBuilder
import org.json4s.native.JsonMethods.{pretty, render}
import behaviors.*
import evaluator.*


object MiniJSRepl:

  /** Renders the store as `Memory: Map(x -> Num(3), ...)`, sorted by name so the order does not depend on hashing. */
  def memory(store: Store): String =
    store.toList.sortBy(_._1).map((name, value) => s"$name -> $value").mkString("Memory: Map(", ", ", ")")

  def format(input: String): String =
    format(input, newStore())

  /** Output for one input, ending with the memory after evaluation so that it precedes the next prompt. */
  def format(input: String, store: Store): String =
    val result = MiniJSParser.parse(input) match
      case MiniJSParser.Success(statements, _) =>
        List(
          "The parsed statements are:",
          Statement.Block(statements).toString,
          "The corresponding JSON structure is:",
          pretty(render(toJson(statements))),
          "The unparsed statements are:",
          unparse(statements),
          s"It evaluates to ${interpret(statements)(using store)}"
        ).mkString("\n")
      case failure: MiniJSParser.NoSuccess =>
        s"Parse error at line ${failure.next.pos.line}, column ${failure.next.pos.column}: ${failure.msg}"
    s"You entered: $input\n$result\n${memory(store)}"

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
        println(memory(store))
        while true do
          val input = readProgram(reader)
          if !input.isBlank then println(format(input, store))
      catch
        case _: EndOfFileException => ()
    finally
      terminal.close()

end MiniJSRepl
