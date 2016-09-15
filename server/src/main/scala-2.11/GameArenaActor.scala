import akka.actor.{Actor, ActorRef}
import domain.{Player, Position}
import events.{PlayerJoined, PlayerLeft, PlayerPositionsChanged, PlayerRequestedMove}

import scala.collection.mutable

/**
  * Created by kuba on 17.09.16.
  */
class GameArenaActor extends Actor {

  val playersEndpoints = mutable.HashMap[String, ActorRef]()
  val playerPositions = mutable.ListBuffer[Player]()

  override def receive: Receive = {
    case msg@PlayerJoined(name, actor) => {
      println(msg)
      playerPositions += Player(name, PositionCalculator.findClosestAvailable(Position(0, 0), takenPositions))
      playersEndpoints += (name -> actor)
      notifyOtherPlayers()
    }
    case msg@PlayerLeft(name) => {
      println(msg)
      playerPositions.remove(playerPositions.indexOf(playerPositions.find(_.name == name).get))
      playersEndpoints.remove(name)
      notifyOtherPlayers()
    }
    case msg@PlayerRequestedMove(name, direction) => {
      println(msg)
      val (xDiff, yDiff) = direction match {
        case "up" => (0, -1)
        case "down" => (0, 1)
        case "right" => (1, 0)
        case "left" => (-1, 0)
      }
      val player = playerPositions.find(_.name == name).get
      val newPosition = player.position + Position(xDiff, yDiff)
      if (!takenPositions.contains(newPosition)) {
        playerPositions(playerPositions.indexOf(player)) = Player(player.name, newPosition)
        notifyOtherPlayers()
      }
    }
    case msg => println(msg)
  }

  def takenPositions = playerPositions.map(_.position)

  def notifyOtherPlayers() = {
    playersEndpoints.values.foreach(_ ! PlayerPositionsChanged(playerPositions.toList))
  }
}