package com.jakubdziworski.game_client.gui

import akka.actor.ActorRef

import scalafx.scene.input.{KeyCode, KeyEvent}

class KeyBoardHandler(keyboardEventsReceiver: ActorRef) {
  def handle(keyEvent: KeyEvent) = keyEvent.code match {
    case KeyCode.Up => keyboardEventsReceiver ! "down" //scalafx coordinates are reversed
    case KeyCode.Down => keyboardEventsReceiver ! "up"
    case KeyCode.Left => keyboardEventsReceiver ! "left"
    case KeyCode.Right => keyboardEventsReceiver ! "right"
  	case _ =>
  }
}
