version := "0.1"
name := "scala-abci-server"
scalaVersion := "2.12.4"
organization := "io.mytime"

enablePlugins(JavaAppPackaging)
PB.targets in Compile := Seq(scalapb.gen() -> (sourceManaged in Compile).value)

libraryDependencies ++= Seq (

  "com.typesafe.akka"     %% "akka-actor"       % "2.5.8",
  "com.typesafe.akka"     %% "akka-stream"      % "2.5.8",

  "com.thesamet.scalapb"  %% "scalapb-runtime"  % scalapb.compiler.Version.scalapbVersion % "protobuf",

  "org.scalatest"     %% "scalatest"            % "3.0.5"   % Test,
  "com.typesafe.akka" %% "akka-testkit"         % "2.5.11"  % Test,
  "com.typesafe.akka" %% "akka-stream-testkit"  % "2.5.11"  % Test

)
