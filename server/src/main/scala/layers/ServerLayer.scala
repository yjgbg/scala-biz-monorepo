package com.github.yjgbg.server
package layers

import zio.ZLayer
import zio.http.Server
object ServerLayer:
  lazy val live: ZLayer[ConfigLayer.Config, Throwable, Server] =
    ZLayer.fromFunction[ConfigLayer.Config => Server.Config] { cfg =>
      Server.Config.default
        .binding(cfg.server.address, cfg.server.port)
    } >>> Server.live
