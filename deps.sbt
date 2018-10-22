libraryDependencies ++= Seq(
  "ohnosequences" %% "s3"      % "0.2.0",
  "ohnosequences" %% "forests" % "0.1.0-29-g34be97d"
) ++ testDependencies

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)
