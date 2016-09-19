import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.{ActorMaterializer, FlowShape}
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, Merge, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
  * Created by andrew on 17.09.16.
  */
class Test extends FunSuite {


//  implicit val system = ActorSystem("test")
//  implicit val materializer = ActorMaterializer()
//
//  val alternativeHandleMsg = Flow.fromGraph(GraphDSL.create() {
//    builder => {
//      import GraphDSL.Implicits._
//
//      val ignoringFlow = Flow[Message].collect {case m => TextMessage(m.asTextMessage.getStrictText)}
//      val merge = builder.add(Merge[Message](2))
//
//      ignoringFlow ~> merge
//
//      FlowShape(ignoringFlow.in,ignoringFlow.out)
//    }
//  })
//
//
//
//  test("fsd") {
//    val (pub, sub) = TestSource.probe[Message]
//      .via(alternativeHandleMsg)
//      .toMat(TestSink.probe[Message])(Keep.both)
//      .run()
//    sub.request(1)
//    pub.sendNext(TextMessage("cos"))
//    sub.expectNext(TextMessage("cos"))
//  }

}
