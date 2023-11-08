package com.github.yjgbg.server
package layers
object ConfigLayer:
  case class Server(address: String, port: Int)

  case class Config(server: Server,`type`:String)
  def live(args:Seq[String]): zio.ZLayer[Any, zio.config.ReadError[String], Config] =
    import com.github.yjgbg.util.fp.|>
    "application.yml"
      |> {ClassLoader.getSystemResourceAsStream}
      |> {java.io.InputStreamReader(_)}
      |> {zio.config.yaml.YamlConfigSource.fromYamlReader(_)}
      |> {zio.config.ConfigSource.fromSystemProps(Some('.'), Some(',')) <> _}
      |> {zio.config.ConfigSource.fromCommandLineArgs(args.toList, Some('.'), Some(',')) <> _}
      |> {zio.config.magnolia.descriptor[Config] from _}
      |> {zio.config.read}
      |> {zio.ZLayer(_)}
    // yaml文件 < 系统属性 < 命令行参数