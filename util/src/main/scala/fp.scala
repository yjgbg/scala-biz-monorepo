package com.github.yjgbg.util

object fp:
  extension[A] (a: A)
    def |>[B](closure: A => B): B = closure(a)