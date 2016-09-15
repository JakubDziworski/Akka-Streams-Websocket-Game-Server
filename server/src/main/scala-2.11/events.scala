import akka.actor.ActorRef
import domain.Player

/**
  * Created by kuba on 17.09.16.
  */
package object events {
  sealed trait GameEvent

  case class PlayerJoined(name: String, actor: ActorRef) extends GameEvent

  case class PlayerLeft(name: String) extends GameEvent

  case class PlayerRequestedMove(name: String, direction: String) extends GameEvent

  case class PlayerPositionsChanged(playerPositions: List[Player]) extends GameEvent
}
