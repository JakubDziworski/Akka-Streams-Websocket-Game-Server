/**
  * Created by kuba on 12.09.16.
  */

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{Attributes, FlowShape, OverflowStrategy}
import domain.{Player, Position}
import events._
import spray.json.DefaultJsonProtocol


trait MyJsonProtocol extends DefaultJsonProtocol {
  implicit val positionFormat = jsonFormat2(Position.apply)
  implicit val playerFormat = jsonFormat2(Player.apply)
}

class GameService(implicit val actorSystem:ActorSystem) extends MyJsonProtocol {

  private[this] val gameArenaActor = actorSystem.actorOf(Props(new GameArenaActor()))

  def route: Route = (get & parameter("name")) { playerName =>
    handleWebSocketMessages(handleMsg(playerName))
  }

  def handleMsg(userName: String): Flow[Message, Message, _] = Flow.fromGraph(GraphDSL.create(Source.actorRef[GameEvent](bufferSize = 5, OverflowStrategy.dropNew)) {
    implicit builder => messageBackToClientSource =>
      import GraphDSL.Implicits._

      val messagesFromWebsocketFlow = builder.add(
        Flow[Message].collect {
          case TextMessage.Strict(txt) => PlayerRequestedMove(userName, txt)
        })
      val messagesToWebsocketFlow = builder.add(
        Flow[GameEvent].collect {
          case PlayerPositionsChanged(positions) => {
            import spray.json._
            TextMessage(positions.toJson.toString)
          }
        }
      )
      val gameArenaActorSink = Sink.actorRef[GameEvent](gameArenaActor, PlayerLeft(userName))
      val playerJoinedMessagesFlow = builder.materializedValue.map(actor => PlayerJoined(userName, actor))
      val merge = builder.add(Merge[GameEvent](2))

      messagesFromWebsocketFlow ~> merge.in(0)
      playerJoinedMessagesFlow ~> merge.in(1)
      merge ~> gameArenaActorSink
      messageBackToClientSource ~> messagesToWebsocketFlow
      FlowShape(messagesFromWebsocketFlow.in, messagesToWebsocketFlow.out)
  })

  val handleMessages = handleMsg("xd")
  handleMessages.log("before-map")
    .withAttributes(Attributes.logLevels(onElement = Logging.WarningLevel))

  // or provide custom logging adapter
  implicit val adapter = Logging(actorSystem, "customLogger")
  handleMessages.log("custom")

}
