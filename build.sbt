name := "ReplicatedKVStore"

version := "1.0"

scalaVersion := "2.11.8"

val json4sVersion = "3.3.0"

val akkaVersion = "2.4.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
  "com.typesafe.akka" %% "akka-distributed-data-experimental" % akkaVersion,
  "de.heikoseeberger" %% "akka-http-json4s" % "1.6.0",
  "org.json4s" %% "json4s-core" % json4sVersion,
  "org.json4s" %% "json4s-jackson" % json4sVersion
)