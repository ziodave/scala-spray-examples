import akka.actor.Actor
import spray.routing.HttpService

class WebServiceActor extends Actor with WebService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing,
  // timeout handling or alternative handler registration
  def receive = runRoute(route)

}

trait WebService extends HttpService with FruitMarshallers {

  implicit def executionContext = actorRefFactory.dispatcher

  val route = path("fruits") {
    get {
      complete {
        Seq(Fruit("Bananas", 1), Fruit("Oranges", 2), Fruit("Lemons", 0.5))
      }
    }
  }

}