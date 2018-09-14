package hiketra

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.routing.FromConfig
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

object MicroserviceServer extends LazyLogging with App {

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val config = ConfigFactory.load()

  implicit val system: ActorSystem = ActorSystem(actorSystemName)
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def actorSystemName: String = s"akka-playground"

  lazy val routes: Route = {
    //todo: requires auditing or JWT/securityDirective?
    get(
      path("test") {
        complete("Hello :)")
      })
  }

  logger.info("Initializing HTTP server...")
  Http().bindAndHandle(routes, "localhost", 1234)
  logger.info(s"Server online at https://locallhost:1234/")
}
