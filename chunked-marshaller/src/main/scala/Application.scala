import akka.actor.{Props, ActorSystem}
import akka.io.IO
import spray.can.Http

object Application extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem()

  // create and start our service actor
  val service = system.actorOf(Props[WebServiceActor], "web-service")

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ! Http.Bind(service, "", port = 8080)

}
