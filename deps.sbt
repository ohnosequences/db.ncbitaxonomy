libraryDependencies ++= Seq(
  "ohnosequences" %% "s3"      % "0.1.0-4-gaa153b0",
  "ohnosequences" %% "forests" % "0.1.0"
) ++ testDependencies

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)
