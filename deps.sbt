libraryDependencies ++= Seq(
  "ohnosequences" %% "aws-scala-tools" % "0.20.0",
  "ohnosequences" %% "forests"         % "0.0.0-129-g5ffb485"
) ++ testDependencies

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)
