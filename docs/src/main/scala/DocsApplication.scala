package com.github.yjgbg
package docs

import sttp.tapir.redoc.bundle.RedocInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import util.fp.|>

object DocsApplication:
  @main def main = zio.Unsafe.unsafe: uf ?=>
    List(
      spec.Proxy.login
    )
    |> {SwaggerInterpreter(
      customiseDocsModel = _.copy(
        servers = Nil
          :+ sttp.apispec.openapi.Server("http://localhost:8888")
      )
    ).fromEndpoints[zio.Task](_, "MyApp", "1.0")}
//    |> {RedocInterpreter().fromEndpoints[zio.Task](_,"MyApp","1.0")}
    |> {ZioHttpInterpreter().toHttp(_)}
    |> {zio.http.Server.serve(_)}
    |> {_.provide(zio.http.Server.defaultWithPort(3000))}
    |> {zio.Runtime.default.unsafe.run(_)}
    |> {_.exitCode}