package com.github.yjgbg.server
package layers

import zio.ZLayer
import zio.config.ReadError

import java.nio.file.Paths

object ConfigLayer:
  case class Server(address: String, port: Int)

  case class Config(server: Server,`type`:String)

  lazy val live: ZLayer[CommandLineArgsLayer.CommandLineArgs, ReadError[String], Config] =
    zio.ZLayer.environment[CommandLineArgsLayer.CommandLineArgs].flatMap:ze =>
      import com.github.yjgbg.util.fp.|>
//      import zio.config.yaml.{*,given}
      "./config.yml"
      |> {Paths.get(_)}
      |> {zio.config.yaml.YamlConfigSource.fromYamlPath}
      |> {zio.config.ConfigSource.fromSystemEnv() <> _}
      |> {zio.config.ConfigSource.fromCommandLineArgs(ze.get.args.toList) <> _}
      |> {zio.config.magnolia.descriptor[Config] from _}
      |> {zio.config.read}
      |> {ZLayer.fromZIO(_)}