package com.github.yjgbg.spec

object Proxy:
  import sttp.tapir.*
  import sttp.tapir.json.circe.jsonBody
  import io.circe.generic.auto.{*,given}
  import sttp.tapir.generic.auto.{*,given}
  case class Login(username: String, password: String)
  case class StdResponseWith[A](code: Int, message: String, data: A)
  case class StdResponse(code: Int, message: String)
  def login = sttp.tapir.endpoint
    .tag("用户")
    .description("用户登录")
    .post
    .in("api" / "login")
    .in(jsonBody[Login])
    .out(jsonBody[StdResponseWith[String]])
    .errorOut(jsonBody[StdResponseWith[String]])
