libraryDependencies ++= Seq(
  "ohnosequences" %% "aws-scala-tools" % "0.20.0"
) ++ testDependencies

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)
