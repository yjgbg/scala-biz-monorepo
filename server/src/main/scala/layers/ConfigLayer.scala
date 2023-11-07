package com.github.yjgbg.server
package layers

import zio.ZLayer
import zio.config.ReadError

import java.io.InputStreamReader

object ConfigLayer:
  case class Server(address: String, port: Int)

  case class Config(server: Server,`type`:String)

  def live(args:Seq[String]): ZLayer[Any, ReadError[String], Config] =
    zio.ZLayer.fromZIO:
      import com.github.yjgbg.util.fp.|>
      "application.yml"
        |> {ClassLoader.getSystemResourceAsStream}
        |> {InputStreamReader(_)}
        |> {zio.config.yaml.YamlConfigSource.fromYamlReader(_)}
        |> {zio.config.ConfigSource.fromSystemEnv(Some('_'),Some(',')) <> _}
        |> {zio.config.ConfigSource.fromSystemProps(Some('.'),Some(',')) <> _}
        |> {zio.config.ConfigSource.fromCommandLineArgs(args.toList,Some('.'),Some(',')) <> _}
        |> {zio.config.magnolia.descriptor[Config] from _}
        |> {zio.config.read}
      // yaml文件 < 环境变量 < 系统属性 < 命令行参数