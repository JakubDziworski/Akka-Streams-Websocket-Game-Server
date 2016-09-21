package com.jakubdziworski.service

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import com.jakubdziworski.actor._

/**
  * Created by kuba on 21.09.16.
  */
class GameService(implicit val actorSystem: ActorSystem,implicit val actorMaterializer: ActorMaterializer) extends Directives {

  val websocketRoute = (get & parameter("playerName")){ playerName =>
    handleWebSocketMessages(flow(playerName))
  }

  val gameAreaActor = actorSystem.actorOf(Props(new GameAreaActor()))
  val playerActorSource = Source.actorRef[GameEvent](5,OverflowStrategy.fail)
  def flow(playerName: String): Flow[Message, Message, Any] = Flow.fromGraph(GraphDSL.create(playerActorSource){ implicit builder => playerActor =>
    import GraphDSL.Implicits._

    val materialization = builder.materializedValue.map(playerActorRef => PlayerJoined(Player(playerName,Position(0,0)),playerActorRef))
    val merge = builder.add(Merge[GameEvent](2))

    val messagesToGameEventsFlow = builder.add(Flow[Message].map {
      case TextMessage.Strict(direction) => PlayerMoveRequest(playerName,direction)
    })

    val gameEventsToMessagesFlow = builder.add(Flow[GameEvent].map {
      case PlayersChanged(players) => {
        import spray.json._
        import DefaultJsonProtocol._
        implicit val positionFormat = jsonFormat2(Position)
        implicit val playerFormat = jsonFormat2(Player)
        TextMessage(players.toJson.toString)
      }
    })


    val gameAreaActorSink = Sink.actorRef[GameEvent](gameAreaActor,PlayerLeft(playerName))

    materialization ~> merge ~> gameAreaActorSink
    messagesToGameEventsFlow ~> merge

    playerActor ~> gameEventsToMessagesFlow

    FlowShape(messagesToGameEventsFlow.in,gameEventsToMessagesFlow.out)
  })
}
