import java.util.UUID

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
      val oldPlayer = players(name).player
      val oldPlayerActor = players(name).actorRef
      val newPosition = oldPlayer.position + position
      val newPlayer = PlayerEndpoint(Player(oldPlayer.name,newPosition),oldPlayerActor)
      players(name) = newPlayer
      notifyPlayersChanged
    }
  }

  def notifyPlayersChanged = {
    val message = PlayerStatusChanged(players.values.map(_.player).toList)
    players.values.foreach(playerEndpoint => {
      playerEndpoint.actorRef ! message
    })
  }
}
