name := "akka-stream-game-client"

version := "1.0"

scalaVersion := "2.11.8"

val akkaV = "2.4.10"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-core" % akkaV,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
  "org.scalafx" %% "scalafx" % "8.0.92-R10",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % akkaV
  )