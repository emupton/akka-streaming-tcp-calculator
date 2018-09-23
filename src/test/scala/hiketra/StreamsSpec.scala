package hiketra

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, scaladsl }
import akka.stream.scaladsl.{ Sink, Source }
import hiketra.MicroserviceServer.{ actorSystemName, operators }
import org.scalatest.{ Matchers, WordSpec }
import org.scalatest.matchers._

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

class StreamsSpec extends WordSpec with Matchers {

  //deliberately not using implicits for clarity/understandings sake
  val system: ActorSystem = ActorSystem("system")
  val materializer: ActorMaterializer = ActorMaterializer()(system)

  val source: Source[Int, NotUsed] = scaladsl.Source(1 to 10)

  val sink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)

  val sum: Future[Int] = source.runWith(sink)(materializer)

  "run" in {
    Await.result(sum, 3.seconds) shouldBe (1 to 10).fold(0)(_ + _)
  }

  "parseInput works" in {
    def parseInput(clientInput: String): String = {
      val operators: Seq[(Char, ((Int, Int) => Int))] = Seq(('+', (a, b) => a + b), ('-', (a, b) => a - b))
      val str = clientInput
      val strOperator: Char = str.filter(x => operators.exists(y => y._1 == x)).head
      val operatorFunc = operators.filter(x => x._1 == strOperator).head._2
      val arr = str.split(strOperator)
      operatorFunc(arr(0).toInt, arr(1).toInt).toString
    }

    parseInput("1+2") shouldBe "3"
  }

}
