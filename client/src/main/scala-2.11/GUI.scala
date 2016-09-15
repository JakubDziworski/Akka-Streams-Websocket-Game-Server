import Direction.Direction

import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.scene.Scene
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.scene.layout.{AnchorPane, StackPane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle
import scalafx.scene.text.Text

class GUI(movePlayerListener: (Direction) => Unit) extends JFXApp {
  private val PlayerRadius = 100
  private val Dimensions = 6
  private val ScreenSize = PlayerRadius * Dimensions
  private val panel = new AnchorPane {
    minWidth = ScreenSize
    minHeight = ScreenSize
  }

  stage = new JFXApp.PrimaryStage {
    title.value = "Hello Stage"
    scene = new Scene {
      content = panel
      onKeyPressed = (ev: KeyEvent) => ev.code match {
        case KeyCode.Up => movePlayerListener(Direction.Up)
        case KeyCode.Down => movePlayerListener(Direction.Down)
        case KeyCode.Left => movePlayerListener(Direction.Left)
        case KeyCode.Right => movePlayerListener(Direction.Right)
        case _ =>
      }
    }
  }

  def displayPlayers(playerPositions: Seq[Player]): Unit = {
    val playersShapes = playerPositions.map(player => {
      new StackPane {
        minWidth = ScreenSize
        minHeight = ScreenSize
        layoutX = player.position.x * PlayerRadius
        layoutY = player.position.y * PlayerRadius
        prefHeight = PlayerRadius
        prefWidth = PlayerRadius
        val circlePlayer = new Circle {
          radius = PlayerRadius * 0.5
          fill = getColorForPlayer(player.name)
        }
        val textOnCircle = new Text {
          text = player.name
        }
        children = Seq(circlePlayer, textOnCircle)

        def getColorForPlayer(name: String) = {
          val r = 55 + math.abs(("r" + name).hashCode) % 200
          val g = 55  + math.abs(("g"+ name).hashCode) % 200
          val b = 55  + math.abs(("b" + name).hashCode) % 200
          Color.rgb(r, g, b)
        }
      }
    })
    Platform.runLater({
      panel.children = playersShapes
      panel.requestLayout()
    })
  }
}

object Direction extends Enumeration {
  type Direction = Value
  val Up = Value("up")
  val Down = Value("down")
  val Right = Value("right")
  val Left = Value("left")
}
