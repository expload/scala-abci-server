
// Git Versions
enablePlugins(GitVersioning)
git.formattedShaVersion := git.gitHeadCommit.value map { sha => sha.take(8) }
git.gitTagToVersionNumber := { tag: String =>
  if (tag.length > 0) Some(tag)
  else None
}


skip in publish := true

val commonSettings = Seq(
  organization := "com.expload",
  crossScalaVersions := Seq("2.12.4"),
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-Xlint",
    "-Xfatal-warnings",
    "-Yno-adapted-args",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused-import"
  ),
  skip in publish := true,
  bintrayOmitLicense := true,
  // licenses += ("Apache-2.0", url("http://www.opensource.org/licenses/apache2.0.php")),
  bintrayOrganization := Some("expload"),
  bintrayRepository := "oss",
  bintrayVcsUrl := Some("https://github.com/expload/scala-abci-server")
)

lazy val server = (project in file("abci") / "server").
  settings( commonSettings: _* ).
  settings(
    normalizedName := "scala-abci-server",
    name := "scala-abci-server",
    description := "ABCI Server",
    skip in publish := false
  ).
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
    version := "0.1.1"
  ).
  settings( commonSettings: _* ).
  enablePlugins(JavaAppPackaging).
  dependsOn( server )
