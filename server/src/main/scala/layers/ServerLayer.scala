package com.github.yjgbg.server
package layers

import zio.ZLayer
import zio.http.Server

object ServerLayer:
  lazy val live: ZLayer[ConfigLayer.Config, Throwable, Server] = zio.ZLayer.environment[ConfigLayer.Config].flatMap { ze =>
    zio.http.Server.defaultWith(_.binding(ze.get.server.address, ze.get.server.port))
  }
