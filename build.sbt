import scalariform.formatter.preferences._

name          := "barista"
organization  := "com.victorursan"
version       := "0.0.1"
scalaVersion  := "2.11.8"
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

mainClass in assembly := some("com.victorursan.Main")
assemblyJarName := "barista_snapshot.jar"

libraryDependencies ++= {
  val scalaV        = "2.11.8"
  val akkaStreamV   = "2.4.4"
  val apacheMesosV  = "0.28.0"
  val rxScalaV      = "0.26.1"
  Seq(
    "com.typesafe.akka"       %% "akka-stream"                          % akkaStreamV,
    "com.typesafe.akka"       %% "akka-http-core"                       % akkaStreamV,
    "com.typesafe.akka"       %% "akka-http-spray-json-experimental"    % akkaStreamV,
    "com.typesafe.akka"       %% "akka-http-testkit"                    % akkaStreamV,
    "org.apache.mesos"        % "mesos"                                 % apacheMesosV,
    "org.scala-lang"          % "scala-library"                         % scalaV,
    "org.scala-lang"          % "scala-reflect"                         % scalaV,
    "org.scala-lang"          % "scala-compiler"                        % scalaV,
    "io.reactivex"            % "rxscala_2.11"                          % rxScalaV
  )
}


lazy val root = project.in(file("."))
scalariformSettings
Revolver.settings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)


initialCommands := """|import akka.actor._
                      |import akka.pattern._
                      |import akka.util._
                      |import scala.concurrent._
                      |import scala.concurrent.duration._""".stripMargin

publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := { _ => false }
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) {
    Some("snapshots" at nexus + "content/repositories/snapshots")
  } else {
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  }
}
