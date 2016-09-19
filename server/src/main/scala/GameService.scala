import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{FlowShape, OverflowStrategy}
import spray.json.DefaultJsonProtocol._
import spray.json.{DefaultJsonProtocol, _}
/**
  * Created by kuba on 19.09.16.
  */

trait GameEvent
case class PlayerJoined(string: String,actorRef: ActorRef) extends GameEvent
case class PlayerLeft(playerName: String) extends GameEvent
case class PlayerMoveRequested(name:String,position: Position) extends GameEvent
case class PlayerStatusChanged(players:Seq[Player]) extends GameEvent
case class EmptyEvent() extends GameEvent
case class Position(x:Int,y:Int) {
  def +(other:Position) : Position = {
    Position(x+other.x,y+other.y)
  }
}
object Position {
  implicit def fromString(string:String) : Position = string match {
    case "up" => Position(0,-1)
    case "down" => Position(0,1)
    case "left" => Position(-1,0)
    case "right" => Position(1,0)
    case _ => throw new RuntimeException(s"unable to parse position $string")
  }
}
case class Player(name: String, position: Position)
case class PlayerEndpoint(player: Player, actorRef: ActorRef)

class GameService(system: ActorSystem) {

  val gameRoomActor = system.actorOf(Props(new GameAreaActor))
  val actorSource = Source.actorRef(5,OverflowStrategy.dropNew)

  def flow(playerName: String) : Flow[Message, Message, _] = {
    implicit val positionFormat = DefaultJsonProtocol.jsonFormat2(Position.apply)
    implicit val playerFormat = DefaultJsonProtocol.jsonFormat2(Player.apply)
    val messageToGameEventFlow = Flow[Message].collect {
      case TextMessage.Strict(txt) => PlayerMoveRequested(playerName, txt)
    }

    val gameEventToMessageFlow = Flow[GameEvent].collect {
      case PlayerStatusChanged(players) => {
        TextMessage(players.toJson.toString)
      }
    }

    val graph = GraphDSL.create(actorSource) { implicit builder => actorSrc => {
      import GraphDSL.Implicits._
      val messageToEvent = builder.add(messageToGameEventFlow)
      val eventToMessage = builder.add(gameEventToMessageFlow)
      val sink = Sink.actorRef[GameEvent](gameRoomActor, PlayerLeft(playerName))
      val materializationResultActor = builder.materializedValue.map(actor => PlayerJoined(playerName, actor))
      val merge = builder.add(Merge[GameEvent](2))

      messageToEvent ~>             merge ~> sink
      materializationResultActor ~> merge
      actorSrc ~> eventToMessage
      FlowShape(messageToEvent.in, eventToMessage.out)
    }}

    Flow.fromGraph(graph)
  }
}
