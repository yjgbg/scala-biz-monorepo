package com.github.yjgbg
package server
import com.github.yjgbg.server.layers.{ConfigLayer, LoggerLayer, ServerLayer}
import com.github.yjgbg.spec.Proxy.{StdResponse, StdResponseWith}
import com.github.yjgbg.util.fp.{|>,||>}
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.ztapir.*

import java.time.format.DateTimeFormatter
import scala.util.control.NoStackTrace
object ServerApplication:
  @main def main(args:String*): Unit = zio.Unsafe.unsafe: uf ?=>
    new Thread(() => {
      while (true) {
        println(s"Thread.activeCount = ${Thread.activeCount()}")
        val group = Thread.currentThread().getThreadGroup
        val all = new Array[Thread](group.activeCount())
        group.enumerate(all)
        all.foreach(it => println(it.getName))
        Thread.sleep(20000)
      }
    })
    ||> {it => it.setDaemon(true)}
    |> {_.start()}
    (Nil
      :+ spec.Proxy.login.zServerLogic { in =>
        zio.ZIO.logInfo(s"received request:$in")
        *> zio.ZIO.succeed(StdResponseWith(in.username))
      }
    )
    |> {ZioHttpInterpreter().toHttp _}
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
