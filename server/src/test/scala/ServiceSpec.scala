import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import com.jakubdziworski.service
import org.scalatest.{FunSuite, Matchers}

/**
  * Created by kuba on 20.09.16.
  */
class ServiceSpec extends FunSuite with Matchers with ScalatestRouteTest {

  test("should create GameService") {
    new service.GameService()
  }

  test("should be able to connect to the GameService websocket") {
    assertWebsocket("John") { wsClient =>
        isWebSocketUpgrade shouldEqual true
      }
  }

  test("should register player") {
    assertWebsocket("John"){ wsClient =>
      wsClient.expectMessage("[{\"name\":\"John\",\"position\":{\"x\":0,\"y\":0}}]")
    }
  }

  test("should register multiple players") {
    val gameService = new service.GameService()
    val johnClient = WSProbe()
    val andrewClient = WSProbe()

    WS(s"/?playerName=John", johnClient.flow) ~> gameService.websocketRoute ~> check {
      johnClient.expectMessage("[{\"name\":\"John\",\"position\":{\"x\":0,\"y\":0}}]")
    }
    WS(s"/?playerName=Andrew", andrewClient.flow) ~> gameService.websocketRoute ~> check {
      andrewClient.expectMessage("[{\"name\":\"John\",\"position\":{\"x\":0,\"y\":0}},{\"name\":\"Andrew\",\"position\":{\"x\":0,\"y\":0}}]")
    }
  }

  test("should register player and move it up") {
    assertWebsocket("John") { wsClient =>
      wsClient.expectMessage("[{\"name\":\"John\",\"position\":{\"x\":0,\"y\":0}}]")
      wsClient.sendMessage("up")
      wsClient.expectMessage("[{\"name\":\"John\",\"position\":{\"x\":0,\"y\":1}}]")
    }
  }

  test("should register player and move around") {
    assertWebsocket("John") { wsClient =>
      wsClient.expectMessage("[{\"name\":\"John\",\"position\":{\"x\":0,\"y\":0}}]")
      wsClient.sendMessage("up")
      wsClient.expectMessage("[{\"name\":\"John\",\"position\":{\"x\":0,\"y\":1}}]")
      wsClient.sendMessage("left")
      wsClient.expectMessage("[{\"name\":\"John\",\"position\":{\"x\":-1,\"y\":1}}]")
      wsClient.sendMessage("down")
      wsClient.expectMessage("[{\"name\":\"John\",\"position\":{\"x\":-1,\"y\":0}}]")
      wsClient.sendMessage("right")
      wsClient.expectMessage("[{\"name\":\"John\",\"position\":{\"x\":0,\"y\":0}}]")
    }
  }

  def assertWebsocket(playerName: String)(assertions:(WSProbe) => Unit) : Unit = {
    val gameService = new service.GameService()
    val wsClient = WSProbe()
    WS(s"/?playerName=$playerName", wsClient.flow) ~> gameService.websocketRoute ~> check(assertions(wsClient))
  }
}

