package com.jakubdziworski.game_client

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.jakubdziworski.game_client.gui.{Display, GUI, KeyBoardHandler}

import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    val name = StdIn.readLine("What's your name?")
    val client = new Client(name)
    val display = new Display()
    val input = Source.actorRef[String](5,OverflowStrategy.dropNew)
    val output = display.sink
    val ((inputMat,result),outputMat) = client.run(input,output)
    val keyBoardHandler = new KeyBoardHandler(inputMat)
    new GUI(keyBoardHandler,display).main(args)
  }
}
