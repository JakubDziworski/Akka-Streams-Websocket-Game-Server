package com.jakubdziworski

/**
  * Created by kuba on 21.09.16.
  */

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.jakubdziworski.service.GameService
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

object Server {

  object Config {
    val config = ConfigFactory.defaultApplication()
    val port = config.getInt("server.port")
    val host = config.getString("server.host")
  }

  def main(args: Array[String]) {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val gameService = new GameService()
    val bindingFuture = Http().bindAndHandle(gameService.websocketRoute, Config.host,Config.port)
    println(s"Server online at ${Config.host}:${Config.port}\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
