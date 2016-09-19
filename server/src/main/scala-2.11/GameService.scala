/**
  * Created by kuba on 12.09.16.
  */

import akka.actor.{ActorSystem, Props}
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

class GameService(implicit actorSystem: ActorSystem) extends MyJsonProtocol {

  val handleMessages = handleMsg("xd")
  // or provide custom logging adapter
  implicit val adapter = Logging(actorSystem, "customLogger")
  private[this] val gameArenaActor = actorSystem.actorOf(Props(new GameArenaActor()), "gameArenaActor")

  def logAndGet[T](t:T,identifier:String):T = {
    println(s"$identifier: $t\n")
    t
  }

  def route: Route = (get & parameter("name")) { implicit playerName =>
    handleWebSocketMessages(handleMsg(playerName))
  }

  def handleMsg(userName: String): Flow[Message, Message, _] = {
    val sourceActor = Source.actorRef[GameEvent](bufferSize = 5, OverflowStrategy.dropNew).map(logAndGet(_,"sourceActor"))

    Flow.fromGraph(GraphDSL.create(sourceActor) {
      implicit builder => gameEventActorSource =>
        import GraphDSL.Implicits._

        val messagesFromWebsocketFlow = builder.add(
          Flow[Message].collect {
            case TextMessage.Strict(txt) => PlayerRequestedMove(userName, txt)
          }.map(logAndGet(_,"messagesIn")))
        val messagesToWebsocketFlow = builder.add(
          Flow[GameEvent].collect {
            case PlayerPositionsChanged(positions) => {
              import spray.json._
              TextMessage(positions.toJson.toString)
            }
          }.map(logAndGet(_,"messagesOut"))
        )
        val gameArenaActorSink = Sink.actorRef[GameEvent](gameArenaActor, PlayerLeft(userName))
        val playerJoinedMessagesFlow = port2flow(builder.materializedValue)(builder).map(actor => PlayerJoined(userName, actor))
        val merge = builder.add(Merge[GameEvent](2))

        messagesFromWebsocketFlow ~> merge ~> gameArenaActorSink
        playerJoinedMessagesFlow ~> merge
        gameEventActorSource ~> messagesToWebsocketFlow
        FlowShape(messagesFromWebsocketFlow.in, messagesToWebsocketFlow.out)
    })
  }

}
