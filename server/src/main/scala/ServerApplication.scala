package com.github.yjgbg
package server
import com.github.yjgbg.server.layers.{ConfigLayer, ServerLayer}
import com.github.yjgbg.spec.Proxy.StdResponseWith
import com.github.yjgbg.util.fp.|>
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.ztapir.*
import zio.ZLayer

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
    |> {zio.ZIO.serviceWithZIO[ConfigLayer.Config]{cfg =>
      zio.Console.printLine(s"server start on port ${cfg.server.address}:${cfg.server.port}")
    } *> _}
    |> {_ *> zio.Console.printLine("server finished")}
    |> {_ provide ConfigLayer.live(args)
      >+> ServerLayer.live
    }
    |> {_.catchSome {
      case e:NoStackTrace => zio.Console.printLine(s"${e.getClass}:${e.getMessage}")
      case e:Throwable => zio.Console.printLine(s"${e.getClass}:${e.getMessage}\n${e.getStackTrace.mkString("\n\t\t")}")
    }}
    |> {zio.Runtime.default.unsafe.run _}
