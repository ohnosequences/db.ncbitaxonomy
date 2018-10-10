libraryDependencies ++= Seq(
  "ohnosequences" %% "s3"      % "0.1.0-11-ge66487e-SNAPSHOT",
  "ohnosequences" %% "forests" % "0.1.0-2-gdbfdd11-SNAPSHOT"
) ++ testDependencies

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)
