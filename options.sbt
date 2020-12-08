scalaVersion := "2.12.11"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-Xfatal-warnings",
  "-feature",
  "-language:_"
)

val buildSettings = Defaults.coreDefaultSettings ++ Seq(
  javaOptions += "ulimit -c unlimited"
)