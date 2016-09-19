import akka.actor.Actor

import scala.collection.mutable

/**
  * Created by kuba on 19.09.16.
  */
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
    case PlayerMoveRequested(name,position) => {
      players(name).player.position = position
      notifyPlayersChanged
    }
  }

  def notifyPlayersChanged = {
    players.values.foreach(playerEndpoint => playerEndpoint.actorRef ! PlayerStatusChanged(players.values.toList.map(_.player)))
  }
}
