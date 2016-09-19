import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorSystem, Props}

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
/**
  * Created by kuba on 19.09.16.
  */
class ServerTest extends FunSuite with Directives with ScalatestRouteTest {

  trait GameEvent
  case class PlayerJoined(string: String) extends GameEvent
  case class PlayerLeft(playerName: String) extends GameEvent
  case class PlayerMoveRequested(string:String) extends GameEvent

  class GameAreaActor extends Actor {
    override def receive: Receive = {
      case PlayerJoined(name) => TextMessage(s"Welcome $name")
      case PlayerMoveRequested(direction) => TextMessage(s"so you want to move $direction, huh?")
    }
  }

  test("should be able to connect") {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val gameRoomActor = system.actorOf(Props(new GameAreaActor))

    def flow(playerName: String) : Flow[Message, Message, _] = Flow.fromGraph(GraphDSL.create() { implicit  builder => {
      import GraphDSL.Implicits._
      val messageMapper = builder.add(Flow[Message].collect{
        case TextMessage.Strict(txt) => PlayerMoveRequested(txt)
      })

      val sink = Sink.actorRef[GameEvent](gameRoomActor,PlayerLeft(playerName))
      val materialized = builder.materializedValue.map(x => PlayerJoined(playerName))
      val merge = builder.add(Merge[GameEvent](2))
      materialized ~> merge ~> sink
      messageMapper ~> merge
      FlowShape(messageMapper.in,anotherFlow.out)
    }})

    val route = get {
      handleWebSocketMessages(flow("Jacob"))
    }

    val client = WSProbe()
    client.flow
    WS("/",client.flow) ~> route ~> check {
      client.expectMessage("Welcome Jacob")
      client.sendMessage("up")
      client.expectMessage("so you want to move up, huh?")
    }
  }

}
