import scala.io.StdIn

/**
  * Created by kuba on 12.09.16.
  */

case class Player(name: String, position: Position)
case class Position(x: Int, y: Int)

object Game extends App {

  println("What's your name?")
  val playerName = StdIn.readLine()
  val webSocketClient: WebSocketClient = new WebSocketClient(playerName, playersPositionChangedListener)
  val gui = new GUI(direction => webSocketClient.sendMessageToServer(direction.toString))

  def playersPositionChangedListener = (players: List[Player]) => {
    gui.displayPlayers(players)
  }

  gui.main(args)
}
