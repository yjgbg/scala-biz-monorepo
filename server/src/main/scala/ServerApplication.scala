package com.github.yjgbg
package server
import com.github.yjgbg.server.layers.{ConfigLayer, LoggerLayer, ServerLayer}
import com.github.yjgbg.spec.Proxy.StdResponseWith
import com.github.yjgbg.util.fp.{|>, ||>}
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.ztapir.*

import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import scala.util.control.NoStackTrace
object ServerApplication:
  @main def main(args:String*): Unit = zio.Unsafe.unsafe: uf ?=>
    (Nil
      :+ spec.Proxy.login.zServerLogic { in =>
        zio.ZIO.logInfo(s"received request:$in")
        *> zio.ZIO.succeed(StdResponseWith(in.username))
      }
    )
    |> {ZioHttpInterpreter().toHttp _}
    |> {_ @@ zio.http.Middleware.cors}
    |> {_ @@ zio.http.Middleware.timeout(zio.Duration(2,TimeUnit.SECONDS))}
    |> {_ @@ zio.http.Middleware.serveDirectory(zio.http.Path("code"),Paths.get("./").toFile)}
    |> {zio.http.Server.serve _}
    |> {_ race (zio.ZIO.logInfo("press enter to stop") *> zio.Console.readLine(""))}
    |> {zio.ZIO.serviceWithZIO[ConfigLayer.Config]{cfg =>
      zio.ZIO.logInfo(s"${cfg.name} server start on ${cfg.server.address}:${cfg.server.port}")
    } *> _}
    |> {_ *> zio.ZIO.logInfo("waiting server stopping")}
    |> {_ provide ConfigLayer.live(args) >+> ServerLayer.live >+> LoggerLayer.live}
    |> {_ catchAll {
      case e:NoStackTrace => zio.ZIO.logInfo(s"${e.getClass}:${e.getMessage}")
      case e:Throwable => zio.ZIO.logInfo(s"${e.getClass}:${e.getMessage}\n${e.getStackTrace.mkString("\n\t\t")}")
    }}
    |> {zio.Runtime.default.unsafe.run _}
    |> {_.getOrThrowFiberFailure()}
