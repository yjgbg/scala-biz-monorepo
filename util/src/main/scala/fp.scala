package com.github.yjgbg.util

import scala.annotation.targetName

object fp:
  extension[A] (a: A)
    @targetName("ap")
    def |>[B](closure: A => B): B = closure(a)