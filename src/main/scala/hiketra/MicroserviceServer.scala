package hiketra

import akka.NotUsed
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.routing.FromConfig
import akka.stream.{ ActorMaterializer, ClosedShape, FlowShape }
import akka.stream.scaladsl.Tcp.{ IncomingConnection, ServerBinding }
import akka.stream.scaladsl.{ Broadcast, Flow, Framing, GraphDSL, Keep, RunnableGraph, Sink, Source, Tcp }
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ ExecutionContext, Future }

object MicroserviceServer extends LazyLogging with App {

  val actorSystemName: String = s"akka-playground"

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  implicit val system: ActorSystem = ActorSystem(actorSystemName)
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  //binding stream of tcp requests
  val connections: Source[IncomingConnection, Future[ServerBinding]] = Tcp().bind("127.0.0.1", 8888)

  val operators: Seq[(Char, ((Int, Int) => Int))] = Seq(('+', (a, b) => a + b), ('-', (a, b) => a - b))

  def parseInput(clientInput: String): String = {
    val operators: Seq[(Char, ((Int, Int) => Int))] = Seq(('+', (a, b) => a + b), ('-', (a, b) => a - b))
    val str = clientInput.replaceAll("[^A-Za-z0-9+-]", "")
    val strOperator: Char = str.filter(x => operators.exists(y => y._1 == x)).head
    val operatorFunc = operators.filter(x => x._1 == strOperator).head._2
    val arr = str.split(strOperator)
    operatorFunc(arr(0).toInt, arr(1).toInt).toString
  }

  connections.runForeach({ connection: IncomingConnection =>
    println(s"New connection from: ${connection.remoteAddress}")

    val echo = Flow[ByteString].via(Framing.delimiter(
      ByteString("\n"),
      maximumFrameLength = 256,
      allowTruncation = true)).map(_.utf8String).map(parseInput(_))
      .map(ByteString(_))

    connection.handleWith(echo)

  })(materializer)

  /* RunnableGraph.fromGraph(GraphDSL.create(){implicit builder =>
    import GraphDSL.Implicits._
    val in = connections
    val out = Sink.ignore
    def bcast = builder.add(Broadcast[ByteString](5))
    val echo = Flow[ByteString].via(Framing.delimiter(
      ByteString("\n"),
      maximumFrameLength = 256,
      allowTruncation = true)).map(_.utf8String)
      .map(_ + "!!!\n")
      .map(ByteString(_))
    in.runForeach(connection =>
      connection.
    )

    val sink = Sink.foreach
    ClosedShape
  })*/

  //  val g = RunnableGraph.fromGraph(GraphDSL.create(){implicit builder: GraphDSL.Builder[NotUsed]} =>
  //}

}
