/**
  * Created by kuba on 13.09.16.
  */

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws._
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try


sealed trait PlayerJsonProtocol extends DefaultJsonProtocol {
  implicit val positionFormat = jsonFormat2(Position)
  implicit val playerFormat = jsonFormat2(Player)
}

class WebSocketClient(playerName: String, playersChangedListener: (List[Player] => Unit)) extends PlayerJsonProtocol {
  private val messageSendingActor: ActorRef = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    val incoming: Sink[Message, Future[Done]] = Sink.foreach[Message] {
      case msg: TextMessage.Strict => {
        println(s"$playerName  got msg : $msg")
        import spray.json._
        Try(msg.text.parseJson.convertTo[List[Player]]).map(playersChangedListener)
      }
      case msg => println(s"received unsupported message $msg")
    }

    val config = system.settings.config
    val bufferSize = config.getInt("client.streamBufferSize")
    val outgoing = Source.actorRef(bufferSize, OverflowStrategy.dropNew)
    val adress = config.getString("client.host") + ":" + config.getString("client.port")

    val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(s"ws://$adress/?name=$playerName"))
    val ((messageReceivingActor,_), closed) = outgoing
      .viaMat(webSocketFlow)(Keep.both)
      .toMat(incoming)(Keep.both)
      .run()

    closed.onComplete { result => {
        println(s"Connection closed with result - $result")
        System.exit(0)
      }
    }
    messageReceivingActor
  }

  def sendMessageToServer(message: String): Unit = messageSendingActor ! TextMessage(message)
  def close() = messageSendingActor ! akka.actor.Status.Success("closing stream on demand")
}
