libraryDependencies ++= Seq(
  "ohnosequences" %% "s3"      % "0.1.0-11-ge66487e",
  "ohnosequences" %% "forests" % "0.1.0"
) ++ testDependencies

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)
