package com.jakubdziworski.game_client.gui

import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.Includes._
import scalafx.scene.input.KeyEvent

class GUI(keyBoardHandler: KeyBoardHandler, display: Display) extends JFXApp {
  stage = new JFXApp.PrimaryStage {
    title.value = "client"
    scene = new Scene {
      content = display.panel
      onKeyPressed = (ev:KeyEvent) => keyBoardHandler.handle(ev)
    }
  }
}
