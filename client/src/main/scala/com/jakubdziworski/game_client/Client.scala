package com.jakubdziworski.game_client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{TextMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import com.jakubdziworski.game_client.domain.{Player, Position}
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol
import spray.json._

class Client(playerName: String)(implicit val actorSystem: ActorSystem, implicit val actorMaterializer: ActorMaterializer) extends DefaultJsonProtocol {

  implicit val positionFormat = jsonFormat2(Position)
  implicit val playerFormat = jsonFormat2(Player)

  val config = ConfigFactory.defaultApplication()
  private val serverAdress = config.getString("serverAdress")
  val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(s"ws://$serverAdress/?playerName=$playerName")).collect {
    case TextMessage.Strict(strMsg) => strMsg.parseJson.convertTo[List[Player]]
  }

  def run[M1,M2](input: Source[String, M1], output: Sink[List[Player],M2]) = {
    input.map(direction => TextMessage(direction))
        .viaMat(webSocketFlow)(Keep.both)
        .toMat(output)(Keep.both)
        .run()
  }
}
