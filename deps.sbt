libraryDependencies ++= Seq(
<<<<<<< HEAD
  "ohnosequences" %% "aws-scala-tools" % "0.20.0"
) ++ testDependencies

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
=======
  ) ++ testDependencies

val testDependencies = Seq(
  "org.scalatest" %% "scalatest"       % "3.0.4" % Test,
  "ohnosequences" %% "db-ncbitaxonomy" % "0.1.0"
>>>>>>> tmp/master
)
