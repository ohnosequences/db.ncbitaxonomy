libraryDependencies ++= Seq(
  "ohnosequences" %% "db-ncbitaxonomy" % "0.0.1"
) ++ testDependencies

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)
