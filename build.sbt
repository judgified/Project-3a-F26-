name := "minijs-scala"

version := "0.1"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "2.4.0",
  "org.json4s"             %% "json4s-native"            % "4.1.0-M5",
  "org.jline"              %  "jline"                   % "4.4.8",
  "org.scalatest"          %% "scalatest"                % "3.2.19" % Test
)

enablePlugins(JavaAppPackaging)
