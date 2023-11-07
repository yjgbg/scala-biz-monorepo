package com.github.yjgbg.server
package layers

import zio.ZLayer

object CommandLineArgsLayer:
  case class CommandLineArgs(args:Seq[String])
  def live(args:Seq[String]): ZLayer[Any, Nothing, CommandLineArgs] = ZLayer.succeed(CommandLineArgs(args))
