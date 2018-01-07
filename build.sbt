name := "barista-mesos"

version := "1.0.0"

mainClass := Some("com.victorursan.MainService")

scalaVersion := "2.12.4"
updateOptions := updateOptions.value.withLatestSnapshots(false)

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")

resolvers ++= Seq(
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "ossrh" at "https://oss.sonatype.org/content/repositories/snapshots/",
  Resolver.bintrayRepo("hseeberger", "maven")
)
resourceDirectory in Compile := baseDirectory.value / "src" / "main" / "re% Sources"
compileOrder in Compile := CompileOrder.JavaThenScala

libraryDependencies ++= {
  val AkkaVersion = "2.4.17"
  val AkkaHttpVersion = "10.0.5"
  val LogbackVersion = "1.2.3"
  val Mesos = "1.4.1"
  val Spray = "1.3.4"
  val RxScala = "0.26.5"
  val RxJavaMesos = "0.2.0"
  val Curator = "2.12.0"
  val Specs2 = "4.0.2"

  val ConsulApi = "1.3.0"

  Seq(
    "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion % Sources,
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion % Sources,
    "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion % Sources,
    "ch.qos.logback" % "logback-classic" % LogbackVersion % Sources, //in depth logs from mesos
    "io.reactivex" %% "rxscala" % RxScala % Sources,
    "io.spray" %% "spray-json" % Spray % Sources,
    "com.mesosphere.mesos.rx.java" % "mesos-rxjava-client" % RxJavaMesos % Sources,
    "com.mesosphere.mesos.rx.java" % "mesos-rxjava-protobuf-client" % RxJavaMesos % Sources,
    "org.apache.mesos" % "mesos" % Mesos % Sources,

    //curator
    "org.apache.curator" % "curator-framework" % Curator % Sources,
    "org.apache.curator" % "curator-recipes" % Curator % Sources,

    //consul
    "com.ecwid.consul" % "consul-api" % ConsulApi % Sources,

    //tests
    "org.specs2" %% "specs2-core" % Specs2 % Test
  )
}
