libraryDependencies ++= Seq(
  "ohnosequences" %% "s3"      % "0.2.1",
  "ohnosequences" %% "forests" % "0.2.0"
) ++ testDependencies

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)
