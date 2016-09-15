/**
  * Created by kuba on 17.09.16.
  */
package object domain {
  case class Player(name: String, position: Position)
  case class Position(x: Int, y: Int) {
    def +(position: Position): Position = {
      Position(position.x + x, position.y + y)
    }
  }
}
