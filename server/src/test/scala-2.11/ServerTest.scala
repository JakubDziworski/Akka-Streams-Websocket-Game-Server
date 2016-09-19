import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.concurrent.duration._
import akka.util.ByteString
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import org.scalatest.{FunSuite, Matchers}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import spray.json.DefaultJsonProtocol

import scala.collection.mutable
/**
  * Created by kuba on 19.09.16.
  */
class ServerTest extends FunSuite with Directives with ScalatestRouteTest with Matchers {





  test("should be able to connect") {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    val gameService = new GameService(system)
    val route = get {
      handleWebSocketMessages(gameService.flow("Jacob"))
    }

    val client = WSProbe()
    client.flow
    WS("/",client.flow) ~> route ~> check {

      isWebSocketUpgrade shouldEqual true

      client.expectMessage("""[{"name":"Jacob","position":{"x":0,"y":0}}]""")

      client.sendMessage("up")
      client.sendMessage("left")
      client.sendMessage("""down""")
      client.expectMessage("""[{"name":"Jacob","position":{"x":0,"y":-1}}]""")

      client.expectMessage("""[{"name":"Jacob","position":{"x":-1,"y":-1}}]""")

      client.expectMessage("""[{"name":"Jacob","position":{"x":-1,"y":0}}]""")
    }
  }

}
