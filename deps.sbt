libraryDependencies ++= Seq(
  "ohnosequences" %% "aws-scala-tools" % "0.20.0",
  "ohnosequences" %% "s3"              % "0.1.0",
  "ohnosequences" %% "forests"         % "0.3.0"
) ++ testDependencies

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)
