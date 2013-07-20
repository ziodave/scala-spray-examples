import akka.actor.ActorRef
import spray.http._
import spray.httpx.marshalling.{MarshallingContext, Marshaller}
import spray.http.MediaTypes._

/**
 *
 * @constructor
 */
case class Fruit(val name: String, val kg: Double) {
  def toCSV() = s"$name\t$kg\n"
  def toXML() = <fruit name={ name } kg={ kg.toString } />
}

trait FruitMarshallers {
  implicit def actorRef: ActorRef

  implicit val ChargingStationsMarshaller =
    Marshaller.of[Seq[Fruit]](`text/xml`, `text/csv`) { (value: Seq[Fruit], contentType: ContentType, ctx: MarshallingContext) =>

      val responder = ctx.startChunkedMessage(HttpEntity(contentType, ""))(actorRef)

      contentType.mediaType match {
        case `text/csv` => sendCSV(value, responder)
        case `text/xml` => sendXML(value, responder)
        case _          => // we shouldn't be here
      }

      responder ! ChunkedMessageEnd
    }

  def sendCSV(fruits: Seq[Fruit], responder: ActorRef): Unit = {
    responder ! MessageChunk("name\tkg\n")
    fruits foreach {c => responder ! MessageChunk(c.toCSV()) }
  }
  def sendXML(fruits: Seq[Fruit], responder: ActorRef): Unit = {
    responder ! MessageChunk("<fruits>\n")
    fruits foreach {c => responder ! MessageChunk(c.toXML().toString + "\n") }
    responder ! MessageChunk("</fruits>")
  }
}
