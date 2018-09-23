enablePlugins(JavaAppPackaging)
enablePlugins(AshScriptPlugin)//does not use bash, so we can use alpine images
//docker settings:
dockerBaseImage := "openjdk:8-jdk-alpine"
dockerRepository := sys.props.get("docker_registry")

releaseVersionBump := sbtrelease.Version.Bump.Bugfix

mainClass in Compile := Some("hiketra.MicroserviceServer")

lazy val akkaHttpVersion = "10.0.11"
lazy val akkaVersion    = "2.5.8"

lazy val root = (project in file(".")).
  settings(updateOptions := updateOptions.value.withGigahorse(false)).
  settings(
    inThisBuild(List(
      organization    := "com.bankifi",
      scalaVersion    := "2.12.4"
    )),
    name := "integration-erp-sage-ms",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion,
      "org.scalatest"     %% "scalatest"            % "3.0.1",
      "com.typesafe.akka" %% "akka-stream" % "2.5.14"

    )
  )


val circeVersion = "0.9.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

lazy val publishDocker: ReleaseStep = { st: State =>
  val extracted = Project.extract(st)
  val ref = extracted.get(thisProjectRef)
  extracted.runAggregated(publish in Docker in ref, st)
}
import ReleaseTransformations._
import org.apache.tools.ant.taskdefs.Java

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  //  publishArtifacts,
  publishDocker,
  setNextVersion,
  commitNextVersion,
  pushChanges)
