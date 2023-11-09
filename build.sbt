
ThisBuild / version := "0.1.0"
ThisBuild / organization := "com.github.yjgbg"
ThisBuild / scalaVersion := "3.3.1"
ThisBuild / assemblyMergeStrategy := {
  case "deriving.conf" => MergeStrategy.concat
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.concat
  case x => (ThisBuild / assemblyMergeStrategy).value(x)
}

lazy val util = (project in file("util"))
  .settings(
    name := "util",
    idePackagePrefix :=  Some(organization.value + ".util")
  )
lazy val spec = (project in file("spec"))
  .settings(
    name := "spec",
    idePackagePrefix := Some(organization.value + ".spec"),
    libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.8.5",
    libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.8.5"
  )
lazy val docs = (project in file("docs"))
  .dependsOn(spec,util)
  .enablePlugins(AssemblyPlugin)
  .settings(
    name := "docs",
    idePackagePrefix := Some(organization.value + ".docs"),
    libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.8.5",
    libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % "1.8.5",
    libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-redoc-bundle" % "1.8.5"
  )
lazy val server = (project in file("server"))
  .dependsOn(spec,util)
  .enablePlugins(AssemblyPlugin)
  .settings(
    name := "server",
    idePackagePrefix := Some(organization.value + ".server"),
    libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % "1.8.5",
    libraryDependencies += "dev.zio" %% "zio-config-typesafe" % "3.0.7",
    libraryDependencies += "dev.zio" %% "zio-config-magnolia" % "3.0.7",
    libraryDependencies += "dev.zio" %% "zio-logging" % "2.1.15",
  )