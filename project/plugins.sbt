resolvers ++= Seq(
  "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com",
  "repo.jenkins-ci.org" at "https://repo.jenkins-ci.org/public",
  "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/",
  Resolver.jcenterRepo
)

addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.15")

addSbtPlugin("ohnosequences" % "nice-sbt-settings" % "0.9.0")
// test coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
// codacy
addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.3.11")
