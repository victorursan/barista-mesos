name := "barista-mesos"

version := "1.0"

mainClass := Some("com.victor.Main")

scalaVersion := "2.12.2"
updateOptions := updateOptions.value.withLatestSnapshots(false)

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")

resolvers ++= Seq(
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "ossrh" at "https://oss.sonatype.org/content/repositories/snapshots/",
  Resolver.bintrayRepo("hseeberger", "maven")
)
resourceDirectory in Compile := baseDirectory.value / "src" / "main" / "resources"
compileOrder in Compile := CompileOrder.JavaThenScala

libraryDependencies ++= {
  val AkkaVersion = "2.4.17"
  val AkkaHttpVersion = "10.0.5"
  val LogbackVersion = "1.2.3"
  val Mesos = "1.2.0"
  val Spray = "1.3.3"
  val RxScala = "0.26.5"
  val RxJavaMesos = "0.1.2-SNAPSHOT"
  val Curator = "2.12.0"
  val Specs2 = "3.8.9"

  val ConsulApi = "1.2.2"
  Seq(
    "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion withSources() withJavadoc(),
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion withSources() withJavadoc(),
    "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion withSources() withJavadoc(),
    "ch.qos.logback" % "logback-classic" % LogbackVersion withSources() withJavadoc(), //in depth logs from mesos
    "io.reactivex" %% "rxscala" % RxScala withSources() withJavadoc(),
    "io.spray" %% "spray-json" % Spray withSources() withJavadoc(),
    "com.mesosphere.mesos.rx.java" % "mesos-rxjava-client" % RxJavaMesos withSources() withJavadoc(),
    "com.mesosphere.mesos.rx.java" % "mesos-rxjava-protobuf-client" % RxJavaMesos withSources() withJavadoc(),
    "org.apache.mesos" % "mesos" % Mesos withSources() withJavadoc(),
    "org.apache.curator" % "curator-framework" % Curator,

    //consul
    "com.ecwid.consul" % "consul-api" % ConsulApi withSources() withJavadoc(),

    //tests
    "org.specs2" %% "specs2-core" % Specs2
  )
}

    