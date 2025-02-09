val reactJS        = "16.13.1"
val scalaJsReact   = "1.7.7"
val reactResizable = "1.11.1"
val scalaJSDom     = "1.1.0"

addCommandAlias("restartWDS",
                "; ~demo/fastOptJS::stopWebpackDevServer; demo/fastOptJS::startWebpackDevServer"
)

Global / onChangedBuildSource := ReloadOnSourceChanges

Global / resolvers += Resolver.sonatypeRepo("public")

// sbt-release-early
inThisBuild(
  List(
    homepage := Some(url("https://github.com/cquiroz/scalajs-react-resizable")),
    licenses := Seq("BSD 3-Clause License" -> url("https://opensource.org/licenses/BSD-3-Clause")),
    developers := List(
      Developer("cquiroz",
                "Carlos Quiroz",
                "carlos.m.quiroz@gmail.com",
                url("https://github.com/cquiroz")
      )
    ),
    scmInfo := Some(
      ScmInfo(url("https://github.com/cquiroz/scalajs-react-resizable"),
              "scm:git:git@github.com:cquiroz/scalajs-react-resizable"
      )
    )
  )
)

val root =
  project
    .in(file("."))
    .settings(commonSettings: _*)
    .aggregate(facade, demo)
    .settings(
      name := "root",
      // No, SBT, we don't want any artifacts for root.
      // No, not even an empty jar.
      publish := {},
      publishLocal := {},
      publishArtifact := false,
      Keys.`package` := file("")
    )

lazy val demo =
  project
    .in(file("demo"))
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(commonSettings: _*)
    .settings(
      webpack / version := "4.44.1",
      startWebpackDevServer / version := "3.3.1",
      webpackCliVersion / version := "3.3.1",
      fastOptJS / webpackConfigFile := Some(
        baseDirectory.value / "src" / "webpack" / "webpack-dev.config.js"
      ),
      fullOptJS / webpackConfigFile := Some(
        baseDirectory.value / "src" / "webpack" / "webpack-prod.config.js"
      ),
      webpackMonitoredDirectories += (Compile / resourceDirectory).value,
      webpackResources := (baseDirectory.value / "src" / "webpack") * "*.js",
      webpackMonitoredFiles / includeFilter := "*",
      useYarn := true,
      fastOptJS / webpackBundlingMode := BundlingMode.LibraryOnly(),
      fullOptJS / webpackBundlingMode := BundlingMode.Application,
      Compile / fastOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) },
      test := {},
      webpackDevServerPort := 9090,
      Compile / npmDevDependencies ++= Seq(
        "css-loader"                         -> "1.0.0",
        "less"                               -> "3.8.1",
        "less-loader"                        -> "4.1.0",
        "mini-css-extract-plugin"            -> "0.11.0",
        "html-webpack-plugin"                -> "3.2.0",
        "url-loader"                         -> "4.1.0",
        "style-loader"                       -> "0.23.0",
        "postcss-loader"                     -> "3.0.0",
        "cssnano"                            -> "4.1.0",
        "optimize-css-assets-webpack-plugin" -> "5.0.1",
        "webpack-merge"                      -> "4.1.4",
        "webpack-dev-server-status-bar"      -> "1.1.0",
        "autoprefixer"                       -> "9.1.5"
      ),
      Compile / npmDependencies ++= Seq(
        "react"           -> reactJS,
        "react-dom"       -> reactJS,
        "react-resizable" -> reactResizable
      ),
      libraryDependencies ++= Seq(
        "io.github.cquiroz.react" %%% "react-sizeme" % "0.6.4"
      ),
      // don't publish the demo
      publish := {},
      publishLocal := {},
      publishArtifact := false,
      Keys.`package` := file("")
    )
    .dependsOn(facade)

lazy val facade =
  project
    .in(file("facade"))
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(commonSettings: _*)
    .settings(
      name := "react-resizable",
      webpack / version := "4.30.0",
      startWebpackDevServer / version := "3.3.1",
      webpackCliVersion / version := "3.3.1",
      // Requires the DOM for tests
      Test / requireJsDomEnv := true,
      // Compile tests to JS using fast-optimisation
      // scalaJSStage in Test            := FastOptStage,
      Compile / npmDependencies ++= Seq(
        "react"           -> reactJS,
        "react-dom"       -> reactJS,
        "react-resizable" -> reactResizable
      ),
      libraryDependencies ++= Seq(
        "com.github.japgolly.scalajs-react" %%% "core"            % scalaJsReact,
        "com.github.japgolly.scalajs-react" %%% "extra"           % scalaJsReact,
        "com.github.japgolly.scalajs-react" %%% "test"            % scalaJsReact % Test,
        "org.scala-js"                      %%% "scalajs-dom"     % scalaJSDom,
        "io.github.cquiroz.react"           %%% "common"          % "0.11.3",
        "io.github.cquiroz.react"           %%% "react-draggable" % "0.11.3",
        "com.lihaoyi"                       %%% "utest"           % "0.7.10"      % Test,
        "org.typelevel"                     %%% "cats-core"       % "2.6.1"      % Test
      ),
      // webpackConfigFile in Test := Some(
      //   baseDirectory.value / "src" / "webpack" / "test.webpack.config.js"
      // ),
      testFrameworks += new TestFramework("utest.runner.Framework")
    )

lazy val commonSettings = Seq(
  scalaVersion := "2.13.6",
  organization := "io.github.cquiroz.react",
  sonatypeProfileName := "io.github.cquiroz",
  description := "scala.js facade for react-resizable ",
  Test / publishArtifact := false,
  scalacOptions ~= (_.filterNot(
    Set(
      // By necessity facades will have unused params
      "-Wdead-code",
      "-Wunused:params",
      "-Ywarn-dead-code",
      "-Ywarn-unused:params"
    )
  ))
)
