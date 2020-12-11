name := "video-stream-analytics"

version := "1.0"

val akkaVersion = "2.6.0"

fork := true
javaCppPresetLibs ++= Seq(
  "ffmpeg" -> "3.2.1"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-contrib" % "0.11",
  //"com.typesafe.akka" %% "akka-http-core" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % "10.1.13",
  //"com.typesafe.akka" %% "akka-http-experimental" % "10.0.1",
  "org.typelevel" %% "cats" % "0.9.0",
  //mqtt
  "com.lightbend.akka" %% "akka-stream-alpakka-mqtt" % "2.0.2",
  // mapping
  "com.typesafe.play" %% "play-json" % "2.9.1",
  // logs
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback"    %  "logback-classic" % "1.1.3",
  // test
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

// Assembly settings
//mainClass in Global := Some("fr.xebia.streams.GroupedSource")

fork in run := true

javacOptions ++= Seq(
  "-Xlint:deprecation"
)
