name := "barista-mesos"

version := "1.0"

mainClass := Some("com.victor.Main")

scalaVersion := "2.12.1"
updateOptions := updateOptions.value.withLatestSnapshots(false)

resolvers ++= Seq(
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "ossrh" at "https://oss.sonatype.org/content/repositories/snapshots/",
  Resolver.bintrayRepo("hseeberger", "maven")
)

compileOrder in Compile := CompileOrder.ScalaThenJava

libraryDependencies ++= {
  val AkkaVersion = "2.5-M1"
  val AkkaHttpVersion = "10.0.3"
  val Json4sVersion = "3.5.0"
  val LogbackVersion = "1.2.1"
  val AkkaJson4s = "1.12.0"
  val Mesos = "1.1.0"
  val RxJavaMesos = "0.1.2-SNAPSHOT"
  Seq(
    "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    "ch.qos.logback" % "logback-classic" % LogbackVersion,
    "org.json4s" %% "json4s-native" % Json4sVersion,
    "org.json4s" %% "json4s-ext" % Json4sVersion,
    "io.reactivex" %% "rxscala" % "0.26.5" withSources() withJavadoc(),
    "de.heikoseeberger" %% "akka-http-json4s" % AkkaJson4s,
    "com.mesosphere.mesos.rx.java" % "mesos-rxjava-client" % RxJavaMesos,
    "com.mesosphere.mesos.rx.java" % "mesos-rxjava-protobuf-client" % RxJavaMesos,
    "org.apache.mesos" % "mesos" % Mesos withSources() withJavadoc()

  )
}

    