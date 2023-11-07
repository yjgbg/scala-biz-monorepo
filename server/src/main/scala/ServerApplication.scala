package com.github.yjgbg
package server
import com.github.yjgbg.server.layers.{CommandLineArgsLayer, ConfigLayer, ServerLayer}
import com.github.yjgbg.spec.Proxy.StdResponseWith
import com.github.yjgbg.util.fp.|>
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.ztapir.*
import zio.ZLayer
import zio.config.ReadError
object ServerApplication:
  @main def main(args:String*) = zio.Unsafe.unsafe: uf ?=>
    (Nil
      :+ spec.Proxy.login.zServerLogic { in =>
        zio.ZIO.succeed(StdResponseWith(200, "OK", in.username))
      }
    )
    |> {ZioHttpInterpreter().toHttp(_)}
    |> {zio.http.Server.serve(_)}
    |> {_ <* zio.Console.printLine("server starting")}
    |> {_ *> zio.Console.printLine("server started")}
    |> {_.provide(CommandLineArgsLayer.live(args) >+> ConfigLayer.live >+> ServerLayer.live)}
    |> {_.catchSome {
      case e:ReadError[_] => zio.Console.printLine(s"config error on ${e.getMessage}")
      case e:Throwable => zio.Console.printLine(e.getStackTrace.mkString("\n"))
    }}
    |> {zio.Runtime.default.unsafe.run(_)}
    |> {_.exitCode}
