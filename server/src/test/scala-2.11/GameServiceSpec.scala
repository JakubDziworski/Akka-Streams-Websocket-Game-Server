import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
  * Created by andrew on 17.09.16.
  */
class GameServiceSpec extends FunSuite {


  implicit val system = ActorSystem("test")
  implicit val materializer = ActorMaterializer()

  def getFlow = {
    new GameService().handleMsg("andrew")
  }

  test("should register") {
    val input = Nil

    val expectedOutput = List (
      "[{\"name\":\"andrew\",\"position\":{\"x\":0,\"y\":0}}]"
    ).map(TextMessage(_))

    assertFlow(getFlow,input,expectedOutput)
  }

  test("should register and move player up") {
    val input = List("up").map(TextMessage(_))

    val expectedOutput = List (
      "[{\"name\":\"andrew\",\"position\":{\"x\":0,\"y\":0}}]",
      "[{\"name\":\"andrew\",\"position\":{\"x\":0,\"y\":-1}}]"
    ).map(TextMessage(_))

    assertFlow(getFlow,input,expectedOutput)
  }

  test("should register and move player up and left") {
    val input = List("up","left").map(TextMessage(_))

    val expectedOutput = List (
      "[{\"name\":\"andrew\",\"position\":{\"x\":0,\"y\":0}}]",
      "[{\"name\":\"andrew\",\"position\":{\"x\":0,\"y\":-1}}]",
      "[{\"name\":\"andrew\",\"position\":{\"x\":-1,\"y\":-1}}]"
    ).map(TextMessage(_))

    assertFlow(getFlow,input,expectedOutput)
  }

  def assertFlow(flow:Flow[Message,Message,_], input:Seq[Message], expectedOutput:Seq[Message]) = {
    val (pub, sub) = TestSource.probe[Message]
      .via(flow)
      .toMat(TestSink.probe[Message])(Keep.both)
      .run()
    sub.request(expectedOutput.size)
    input.foreach(pub.sendNext)
    expectedOutput.foreach(sub.expectNext)
  }

}
