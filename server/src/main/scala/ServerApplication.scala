package com.github.yjgbg
package server
import com.github.yjgbg.server.layers.{ConfigLayer, ServerLayer}
import com.github.yjgbg.spec.Proxy.StdResponseWith
import com.github.yjgbg.util.fp.|>
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.ztapir.*

import java.time.format.DateTimeFormatter
import scala.util.control.NoStackTrace
object ServerApplication:
  @main def main(args:String*) = zio.Unsafe.unsafe: uf ?=>
    (Nil
      :+ spec.Proxy.login.zServerLogic { in =>
        zio.ZIO.succeed(StdResponseWith(200, "OK", in.username))
      }
    )
    |> {ZioHttpInterpreter().toHttp _}
    |> {zio.http.Server.serve _}
    |> {_ race (zio.ZIO.logInfo("press enter to stop") *> zio.Console.readLine(""))}
    |> {zio.ZIO.serviceWithZIO[ConfigLayer.Config]{cfg =>
      zio.ZIO.logInfo(s"server start on port ${cfg.server.address}:${cfg.server.port}")
    } *> _}
    |> {_ *> zio.ZIO.logInfo("server stopped")}
    |> {_ provide ConfigLayer.live(args) >+> ServerLayer.live}
    |> {_ provide zio.Runtime.removeDefaultLoggers >>> zio.logging.consoleJsonLogger(
      config = zio.logging.ConsoleLoggerConfig.default
        .copy(
          format = {
            import zio.logging.LogFormat.*
            label("timestamp", timestamp(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) +
              label("level", level) +
              label("location",enclosingClass + text(":") + traceLine) +
              label("thread", fiberId) +
              label("message", line) +
              label("cause", cause)
          }
        )
    )}
    |> {_ catchAll {
      case e:NoStackTrace => zio.ZIO.logInfo(s"${e.getClass}:${e.getMessage}")
      case e:Throwable => zio.ZIO.logInfo(s"${e.getClass}:${e.getMessage}\n${e.getStackTrace.mkString("\n\t\t")}")
    }}
    |> {zio.Runtime.default.unsafe.run _}
