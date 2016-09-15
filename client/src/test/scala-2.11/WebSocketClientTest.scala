import org.scalatest.concurrent.Waiters
import org.scalatest._

import scala.collection.mutable
import org.scalatest.time.SpanSugar._


/**
  * Created by kuba on 17.09.16.
  */
class WebSocketClientTest extends FunSuite with Waiters with Matchers {

  val Timeout = timeout(5 seconds)

  test("should login player") {
    val playerName = "kuba"
    val sentMessages = Nil
    val expectedResponse = mutable.Stack(
      List(Player(playerName, Position(0, 0)))
    )
    assertServerResponseAndClose(playerName,sentMessages,expectedResponse)
  }

  test("should login and move player down and right") {
    val sentMessages = List("down","right")
    val expectedResponse = mutable.Stack(
      List(Player("andrew",Position(0,0))),
      List(Player("andrew",Position(0,1))),
      List(Player("andrew",Position(1,1)))
    )
    assertServerResponseAndClose("andrew",sentMessages,expectedResponse)
  }

  test("should login and move player up left down right") {
    val sentMessages = List("up","left","down","right")
    val expectedResponse = mutable.Stack(
      List(Player("andrew",Position(0,0))),
      List(Player("andrew",Position(0,-1))),
      List(Player("andrew",Position(-1,-1))),
      List(Player("andrew",Position(-1,0))),
      List(Player("andrew",Position(0,0)))
    )
    assertServerResponseAndClose("andrew",sentMessages,expectedResponse)
  }

  test("should login two player") {

    val andrewJoinedExpectedMessage = List(Player("andrew", Position(0, 0)))
    val johnJoinedExpectedMessage = List(Player("andrew", Position(0, 0)),Player("john", Position(0, 1)))

    val andrewJoinedWaiter = new Waiter
    val JohnJoinedWaiter1 = new Waiter
    var andrewMessageReceiveCount = 0
    val andrewClient = new WebSocketClient("andrew", players => {
      andrewMessageReceiveCount match {
        case 0 =>
          andrewJoinedWaiter(players shouldEqual andrewJoinedExpectedMessage)
          andrewJoinedWaiter.dismiss()
        case 1 =>
          JohnJoinedWaiter1(players shouldEqual johnJoinedExpectedMessage)
          JohnJoinedWaiter1.dismiss()
      }
      andrewMessageReceiveCount += 1
    })

    andrewJoinedWaiter.await(Timeout)

    val johnJoinedWaiter2 = new Waiter
    val johnClient = new WebSocketClient("john", players => {
      johnJoinedWaiter2(players shouldEqual johnJoinedExpectedMessage)
      johnJoinedWaiter2.dismiss()
    })

    JohnJoinedWaiter1.await(Timeout)
    johnJoinedWaiter2.await(Timeout)

    andrewClient.close()
    johnClient.close()
  }


  def assertServerResponseAndClose(playerName: String, messages: Seq[String], expectedMessages: Seq[List[Player]]) : Unit = {
    val waiter = new Waiter
    val expectedMessagesStack = mutable.Stack(expectedMessages:_*)
    val ws = new WebSocketClient(playerName,message => {
      waiter(message shouldEqual expectedMessagesStack.pop)
      if(expectedMessagesStack.isEmpty) waiter.dismiss()
    })
    messages.foreach(ws.sendMessageToServer)
    waiter.await(Timeout)
    ws.close()
  }
}
