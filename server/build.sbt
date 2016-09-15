name := "akka-stream-game-server"

version := "1.0"

scalaVersion := "2.11.8"

val akkaV = "2.4.10"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence" % akkaV,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaV,
  "com.typesafe.akka" %% "akka-http-core" % akkaV,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

