package edu.luc.cs.laufer.cs371.minijs

import org.jline.reader.{EndOfFileException, LineReaderBuilder}
import org.jline.terminal.TerminalBuilder
import org.json4s.native.JsonMethods.{pretty, render}
import behaviors.*


object MiniJSRepl:

  def format(input: String): String =
    val result = MiniJSParser.parse(input) match
      case MiniJSParser.Success(statements, _) =>
        List(
          "The parsed statements are:",
          Statement.Block(statements).toString,
          "The corresponding JSON structure is:",
          pretty(render(toJson(statements))),
          "The unparsed statements are:",
          unparse(statements)
        ).mkString("\n")
      case failure: MiniJSParser.NoSuccess =>
        s"Parse error at line ${failure.next.pos.line}, column ${failure.next.pos.column}: ${failure.msg}"
    s"You entered: $input\n$result"

  def main(args: Array[String]): Unit =
    val terminal = TerminalBuilder.builder().system(true).build()
    try
      val reader = LineReaderBuilder.builder().terminal(terminal).build()
      try
        while true do
          val lines = List.newBuilder[String]
          var line = reader.readLine("minijs> ")
          while line.nonEmpty do
            lines += line
            line = reader.readLine(".....> ")
          println(format(lines.result().mkString("\n")))
      catch
        case _: EndOfFileException => ()
    finally
      terminal.close()

end MiniJSRepl
