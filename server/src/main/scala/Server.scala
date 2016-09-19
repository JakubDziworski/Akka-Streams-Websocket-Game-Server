import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn

/**
  * Created by kuba on 19.09.16.
  */
object Server extends App with Directives {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val gameService = new GameService(system)

  val route = (get & parameter("name")) { playerName =>
    handleWebSocketMessages(gameService.flow(playerName))
  }

  val bindingFuture = Http().bindAndHandle(route,"localhost",8080)

  StdIn.readLine("server running. press return to stop server")

  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
