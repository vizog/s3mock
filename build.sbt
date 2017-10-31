import com.typesafe.sbt.packager.docker._

name := "s3mock"

version := "0.2.4-ts"

organization := "io.findify"

scalaVersion := "2.12.2"

crossScalaVersions := Seq("2.11.11", "2.12.2")

val akkaVersion = "2.5.2"

licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

homepage := Some(url("https://github.com/findify/s3mock"))

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.0.7",
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "com.github.pathikrit" %% "better-files" % "2.17.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.149",
  "org.scalatest" %% "scalatest" % "3.0.3" % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
  "org.iq80.leveldb" % "leveldb" % "0.9",
  "com.lightbend.akka" %% "akka-stream-alpakka-s3" % "0.9" % "test"
)

parallelExecution in Test := false

publishMavenStyle := true

publishTo := Some("Bintray API Realm" at s"https://api.bintray.com/content/vizog/maven/s3mock/${version.value}")

pomExtra := (
    <scm>
      <url>git@github.com:findify/s3mock.git</url>
      <connection>scm:git:git@github.com:findify/s3mock.git</connection>
    </scm>
    <developers>
      <developer>
        <id>romangrebennikov</id>
        <name>Roman Grebennikov</name>
        <url>http://www.dfdx.me</url>
      </developer>
    </developers>)

enablePlugins(JavaAppPackaging)

maintainer in Docker := "Document pipeline team"
packageSummary in Docker := "S3moock"
packageDescription := "Mock Service For S3"
dockerRepository := Some("vizog")
dockerUpdateLatest := true
dockerCommands += Cmd("USER", "root")
dockerCommands ++= Seq(
  ExecCmd("RUN", "apt-get", "update"),
  ExecCmd("RUN", "apt-get", "install", "-y", "telnet", "vim", "net-tools")
)
dockerExposedPorts := Seq(8001)
