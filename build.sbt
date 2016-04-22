import scalariform.formatter.preferences._

name          := "barista"
organization  := "com.victorursan"
version       := "0.0.1"
scalaVersion  := "2.11.8"
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

resolvers += "Mesosphere Repo" at "http://downloads.mesosphere.io/maven"

mainClass in assembly := some("com.victorursan.Main")
assemblyJarName := "barista_snapshot.jar"

libraryDependencies ++= {
  val scalaV           = "2.11.8"
  val akkaStreamV      = "2.4.3"
  val apacheMesosV     = "0.28.0"
  val scalaMockV       = "3.2.2"
  val scalazScalaTestV = "0.2.3"
  val slf4jLoggerV     = "1.7.21"
  Seq(

    "com.typesafe.akka"       %% "akka-stream"                          % akkaStreamV,
    "com.typesafe.akka"       %% "akka-http-core"                       % akkaStreamV,
    "com.typesafe.akka"       %% "akka-http-spray-json-experimental"    % akkaStreamV,
    "com.typesafe.akka"       %% "akka-http-testkit"                    % akkaStreamV,
    "org.apache.mesos"        % "mesos"                                 % apacheMesosV,
    "mesosphere"              %% "mesos-utils"                          % apacheMesosV,
    "org.scala-lang"          % "scala-library"                         % scalaV,
    "org.scala-lang"          % "scala-reflect"                         % scalaV,
    "org.scala-lang"          % "scala-compiler"                        % scalaV,
    "org.slf4j"               % "slf4j-api"                             % slf4jLoggerV
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
