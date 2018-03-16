val commonSettings = Seq(
  organization := "io.mytc",
  crossScalaVersions := Seq("2.12.4"),
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-Xlint",
    "-Xfatal-warnings",
    "-Yno-adapted-args",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused-import"
  )
)

lazy val server = (project in file("abci") / "server").
  settings(
    normalizedName := "scala-abci-server",
    version := "0.9.0"
  ).
  settings( commonSettings: _* ).
  settings( PB.targets in Compile := Seq(scalapb.gen() -> (sourceManaged in Compile).value) ).
  settings(
    libraryDependencies ++= Seq (
      "com.typesafe.akka"     %% "akka-actor"       % "2.5.8",
      "com.typesafe.akka"     %% "akka-stream"      % "2.5.8",
      "com.thesamet.scalapb"  %% "scalapb-runtime"  % scalapb.compiler.Version.scalapbVersion % "protobuf",

      "com.github.jnr"        % "jnr-unixsocket"    % "0.18",

      "org.scalatest"     %% "scalatest"            % "3.0.5"   % Test,
      "com.typesafe.akka" %% "akka-testkit"         % "2.5.11"  % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % "2.5.11"  % Test
    )
  )

lazy val dummyServer = (project in file("examples") / "abci" / "server" / "dummy" ).
  settings(
    normalizedName := "scala-abci-dummy-server",
    version := "0.1"
  ).
  settings( commonSettings: _* ).
  enablePlugins(JavaAppPackaging).
  dependsOn( server )
