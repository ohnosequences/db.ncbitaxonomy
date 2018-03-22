// generic conf; don't change this file
// pull particular versions from buildconf
scalaVersion := "2.12.4"

dependencyOverrides += "org.scala-lang" % "scala-library" % scalaVersion.value

// compiler
////////////////////////////////////////////////////////////////////////////////
scalacOptions ++= Seq(
  "-Xsource:2.13",
  "-Xlint",
  "-Xfatal-warnings",
  "-Xlog-reflective-calls",
  // "-Ywarn-unused",
  "-Ywarn-adapted-args",
  "-opt-warnings:_",
  "-unchecked",
  // "-Xstrict-inference",
  "-Ywarn-unused-import",
  "-Yno-adapted-args",
  "-Ydelambdafy:method"
)

addCompilerPlugin("ohnosequences" %% "contexts" % "0.5.0")
////////////////////////////////////////////////////////////////////////////////

// scalafmt
////////////////////////////////////////////////////////////////////////////////
scalafmtVersion := "1.4.0"
scalafmtOnCompile := true
////////////////////////////////////////////////////////////////////////////////

// wartemover
////////////////////////////////////////////////////////////////////////////////
wartremoverErrors in (Compile, compile) := Seq()

wartremoverWarnings in (Compile, compile) := Warts.allBut(
  Wart.Equals,
  Wart.FinalVal,
  Wart.ImplicitConversion,
  Wart.Nothing // needed because of the contexts compiler plugin
)
////////////////////////////////////////////////////////////////////////////////

// Test configuration
////////////////////////////////////////////////////////////////////////////////
// shows time for each test:
testOptions in Test += Tests.Argument("-oD")
// disables parallel exec
parallelExecution in Test := false
////////////////////////////////////////////////////////////////////////////////

// publishing
////////////////////////////////////////////////////////////////////////////////
bucketSuffix := "era7.com"
////////////////////////////////////////////////////////////////////////////////

// overriding!

// sbt-build-info stuff; basically used to generate build time value
lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "ohnosequences.db.ncbitaxonomy"
  )

buildInfoOptions += BuildInfoOption.BuildTime
