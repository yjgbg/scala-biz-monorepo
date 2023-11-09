package com.github.yjgbg.server
package layers

import zio.ZLayer
import zio.logging.{FileLoggerConfig, LogFilter}

import java.nio.file.Paths
import java.time.format.DateTimeFormatter

object LoggerLayer:
  lazy val live: ZLayer[Any, Nothing, Unit] = zio.Runtime.removeDefaultLoggers >>> zio.logging.consoleJsonLogger(
    config = zio.logging.ConsoleLoggerConfig(
      format = {
        import zio.logging.LogFormat.*
        label("timestamp", timestamp(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) +
          label("level", level) +
          label("location", loggerName(zio.logging.LoggerNameExtractor.trace) + text(":") + traceLine) +
          label("thread", fiberId) +
          label("message", line) +
          label("cause", cause)
      },
      filter = LogFilter.acceptAll
    )
  )