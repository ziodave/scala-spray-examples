import akka.actor.{ActorRef, ActorRefFactory, Props, Actor}
import spray.can.Http
import spray.http._
import spray.httpx.marshalling.{MarshallingContext, Marshaller}
import spray.http.MediaTypes._
import Actor.noSender
import spray.routing.RequestContext
import spray.util.SprayActorLogging

/**
 *
 * @constructor
 */
case class Fruit(val name: String, val kg: Double) {
  def toCSV() = s"$name\t$kg\n"
  def toXML() = <fruit name={ name } kg={ kg.toString } />
}

trait FruitMarshallers {

  implicit def actorRefFactory: ActorRefFactory

  implicit val SeqFruitMarshaller =
    Marshaller.of[Seq[Fruit]](`text/xml`, `text/csv`) { (fruits: Seq[Fruit], contentType: ContentType, ctx: MarshallingContext) =>

      actorRefFactory.actorOf {
        Props {
          new Actor with SprayActorLogging {

            val responder: ActorRef = ctx.startChunkedMessage(HttpEntity(contentType, ""), Some(Ok(-1)))(self)

            sealed case class Ok(seq: Int)

            def send = contentType.mediaType match {
              case `text/csv` => sendCSV
              case `text/xml` => sendXML
            }

            def stop() = {
              responder ! ChunkedMessageEnd
              context.stop(self)
            }

            def sendCSV = PartialFunction[Int, Unit] {
              case -1 =>
                responder ! MessageChunk("name\tkg\n").withAck(Ok(0))
              case seq if seq < fruits.length =>
                responder ! MessageChunk(fruits(seq).toCSV()).withAck(Ok(seq + 1))
              case _ => stop()
            }

            def sendXML = PartialFunction[Int, Unit] {
              case -1 =>
                responder ! MessageChunk("<fruits>\n").withAck(Ok(0))
              case seq if seq < fruits.length  =>
                responder ! MessageChunk(fruits(seq).toXML().toString + "\n").withAck(Ok(seq + 1))
              case seq if seq == fruits.length =>
                responder ! MessageChunk("</fruits>").withAck(Ok(seq + 1))
              case _ => stop()
            }

            def receive = {
              case Ok(seq) => send(seq)
              case ev: Http.ConnectionClosed =>
                log.warning("Stopping response streaming due to {}", ev)
            }
          }
        }
      }

    }

}
