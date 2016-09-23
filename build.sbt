name := "reactiveStreams"

version := "1.0"

scalaVersion := "2.11.8"

//resolvers
resolvers += Resolver.jcenterRepo

// add conf/ folder
unmanagedResourceDirectories in Compile += baseDirectory.value / "conf"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture")

libraryDependencies ++= Seq(
  "com.iheart" %% "ficus" % "1.2.3",
  "com.sksamuel.elastic4s" %% "elastic4s-core" % "2.3.1",
  "com.sksamuel.elastic4s" %% "elastic4s-streams" % "2.3.1",
  "com.typesafe.akka" %% "akka-stream" % "2.4.10",
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "org.postgresql" % "postgresql" % "9.4.1208",
  "com.github.tminglei" %% "slick-pg" % "0.14.3",
  "com.github.tminglei" %% "slick-pg_date2" % "0.14.3"
)