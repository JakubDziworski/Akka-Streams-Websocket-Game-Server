import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.jakubdziworski.game_client.Client
import com.jakubdziworski.game_client.domain.{Player, Position}
import org.scalatest.{FunSuite, Matchers}

class ClientTest extends FunSuite with Matchers {

  test("should be able to login player") {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val testSink = TestSink.probe[List[Player]]
    val outgoing = Source.empty[String]
    val client = new Client("jacob")

    val (_,testProbe) = client.run(outgoing,testSink)

    testProbe.request(1)
    testProbe.expectNext(List(Player("jacob",Position(0,0))))
  }

  test("should be able to move player") {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    val client = new Client("Jacob")
    val input = Source.actorRef[String](5,OverflowStrategy.dropNew)
    val output = TestSink.probe[List[Player]]

    val ((inputMat,result),outputMat) = client.run(input,output)

    inputMat ! "up"

    outputMat.request(2)
    outputMat.expectNext(List(Player("Jacob",Position(0,0))))
    outputMat.expectNext(List(Player("Jacob",Position(0,1))))
  }

}


