import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.concurrent.duration._
import akka.util.ByteString
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import org.scalatest.FunSuite
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import spray.json.DefaultJsonProtocol

import scala.collection.mutable
/**
  * Created by kuba on 19.09.16.
  */
class ServerTest extends FunSuite with Directives with ScalatestRouteTest {

  trait GameEvent
  case class PlayerJoined(string: String,actorRef: ActorRef) extends GameEvent
  case class PlayerLeft(playerName: String) extends GameEvent
  case class PlayerMoveRequested(string:String) extends GameEvent
  case class PlayerStatusChanged(players:Seq[Player]) extends GameEvent
  case class Position(x:Int,y:Int)
  case class Player(name: String,position: Position)
  case class PlayerEndpoint(player: Player, actorRef: ActorRef)


  class GameAreaActor extends Actor {

    val players = mutable.HashMap[String, PlayerEndpoint]()

    override def receive: Receive = {
      case PlayerJoined(name,actor) => {
        val player = Player(name, Position(0, 0))
        players += (name -> PlayerEndpoint(player,actor))
        notifyPlayersChanged
      }
      case PlayerLeft(name) => {
        players -= name
        notifyPlayersChanged
      }
      case PlayerMoveRequested(direction) => {
        notifyPlayersChanged
      }
    }

    def notifyPlayersChanged = {
      players.values.foreach(playerEndpoint => playerEndpoint.actorRef ! PlayerStatusChanged(players.values.toList.map(_.player)))
    }
  }

  test("should be able to connect") {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val gameRoomActor = system.actorOf(Props(new GameAreaActor))
    val actorSource = Source.actorRef(5,OverflowStrategy.dropNew)

    def flow(playerName: String) : Flow[Message, Message, _] = Flow.fromGraph(GraphDSL.create(actorSource) { implicit  builder=> actorSrc => {
      import GraphDSL.Implicits._
      val messageMapper = builder.add(Flow[Message].collect{
        case TextMessage.Strict(txt) => PlayerMoveRequested(txt)
      })

      val sink = Sink.actorRef[GameEvent](gameRoomActor,PlayerLeft(playerName))
      val materialized = builder.materializedValue.map(x => PlayerJoined(playerName,x))
      val merge = builder.add(Merge[GameEvent](2))
      val gameEvetToMessageConverter = builder.add(Flow[GameEvent].collect {
        case PlayerStatusChanged(players) => {
          import spray.json._
          import DefaultJsonProtocol._
          implicit val positionFormat = DefaultJsonProtocol.jsonFormat2(Position.apply)
          implicit val playerFormat = DefaultJsonProtocol.jsonFormat2(Player.apply)
          TextMessage(players.toJson.toString)
        }
      })
      materialized ~> merge ~> sink
      messageMapper ~> merge

      actorSrc ~> gameEvetToMessageConverter
      FlowShape(messageMapper.in,gameEvetToMessageConverter.out)
    }})

    val route = get {
      handleWebSocketMessages(flow("Jacob"))
    }

    val client = WSProbe()
    client.flow
    WS("/",client.flow) ~> route ~> check {
      client.expectMessage("""[{"name":"Jacob","position":{"x":0,"y":0}}]""")
      client.sendMessage("up")
      client.expectMessage("""[{"name":"Jacob","position":{"x":0,"y":1}}]""")
    }
  }

}
