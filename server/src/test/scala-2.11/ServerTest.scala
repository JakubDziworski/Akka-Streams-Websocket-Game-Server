import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}

import scala.concurrent.duration._
import akka.util.ByteString
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.WSProbe
import org.scalatest.FunSuite
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
/**
  * Created by kuba on 19.09.16.
  */
class ServerTest extends FunSuite with Directives with ScalatestRouteTest {

  test("should be able to connect") {


    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()


//    val materializedActor  = Source.actorRef[Message](5,OverflowStrategy.fail)
//    val graph : Flow[Message,Message,_] = Flow.fromGraph(GraphDSL.create(materializedActor) { implicit builder => actor =>
//      import GraphDSL.Implicits._
//
//      val input = builder.add(Flow[Message].collect {
//        case tm: TextMessage => tm
//        case _ => TextMessage("created")
//      })
//      val output = builder.add(Flow[Message].collect {
//        case tm: TextMessage => tm
//        case _ => TextMessage("created")
//      })
//      val materialized = port2flow(builder.materializedValue)(builder).map(a => TextMessage("Connected"))
//      materialized ~> input
//
//      FlowShape(input.in,output.out)
//    })

    val flow = Flow.fromGraph(GraphDSL.create() { implicit  builder => {
      import GraphDSL.Implicits._
      val flow = builder.add(Flow[Message].map( x => TextMessage("Hello " + x.asTextMessage.getStrictText)))
      val materialized = port2flow(builder.materializedValue)(builder).map(x => TextMessage("connected"))
      val merge = builder.add(Merge[Message](2))
      materialized ~> merge
      flow ~> merge
      FlowShape(flow.in,merge.out)
    }})

    val route = get {
      handleWebSocketMessages(flow)
    }

    val client = WSProbe()
    client.flow
    WS("/",client.flow) ~> route ~> check {
      client.expectMessage("connected")
      client.sendMessage("Peter")
      client.expectMessage("Hello Peter")
    }
  }

}
