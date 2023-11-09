package com.github.yjgbg.server
package layers

import java.time.Duration

object ConfigLayer:
  case class Config(server: Server,name:String)
  import zio.http.netty.{NettyConfig,ChannelType}
  case class Server(address: String = "0.0.0.0", port: Int = 8080,netty:NettyConfig = NettyConfig(
    leakDetectionLevel = NettyConfig.LeakDetectionLevel.SIMPLE,
    channelType = ChannelType.AUTO,
    nThreads = 0,
    shutdownQuietPeriodDuration = Duration.ofSeconds(2),
    shutdownTimeoutDuration = Duration.ofSeconds(15)
  ))
  def live(args:Seq[String]): zio.ZLayer[Any, zio.config.ReadError[String], Config] =
    import com.github.yjgbg.util.fp.|>
    zio.config.typesafe.TypesafeConfigSource.fromResourcePath
      |> {zio.config.ConfigSource.fromSystemProps(Some('.'), Some(',')) <> _}
      |> {zio.config.ConfigSource.fromCommandLineArgs(args.toList, Some('.'), Some(',')) <> _}
      |> {zio.config.magnolia.descriptor[Config] from _}
      |> {zio.config.read}
      |> {zio.ZLayer(_)}
    // yaml文件 < 系统属性 < 命令行参数