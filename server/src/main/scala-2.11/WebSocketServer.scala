

/**
  * Created by kuba on 12.09.16.
  */
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.io.StdIn

object WebSocketServer extends App {

  implicit val actorSystem = ActorSystem("game-server-system")
  implicit val flowMaterializer = ActorMaterializer()

  val config = actorSystem.settings.config
  val host = config.getString("server.host")
  val port = config.getInt("server.port")
  val route = new GameService().route
  val binding = Http().bindAndHandle(route, host, port)

  println(s"Game Server is now online at http://$host:$port\nPress RETURN to stop...")
  StdIn.readLine()

  import actorSystem.dispatcher

  binding.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())
  println("Server is down...")

}