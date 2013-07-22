import akka.actor.Actor
import spray.http._
import spray.httpx.marshalling.{MarshallingContext, Marshaller}
import spray.http.MediaTypes._
import Actor.noSender

/**
 *
 * @constructor
 */
case class Fruit(val name: String, val kg: Double) {
  def toCSV() = s"$name\t$kg\n"
  def toXML() = <fruit name={ name } kg={ kg.toString } />
}

trait FruitMarshallers {

  implicit val SeqFruitMarshaller =
    Marshaller.of[Seq[Fruit]](`text/xml`, `text/csv`) { (value: Seq[Fruit], contentType: ContentType, ctx: MarshallingContext) =>

      val responder = ctx.startChunkedMessage(HttpEntity(contentType, ""))(noSender)

      contentType.mediaType match {
        case `text/csv` => sendCSV(value)
        case `text/xml` => sendXML(value)
        case _          => // we shouldn't be here
      }

      responder ! ChunkedMessageEnd

      def sendCSV(fruits: Seq[Fruit]): Unit = {
        responder ! MessageChunk("name\tkg\n")
        fruits foreach { f => responder ! MessageChunk(f.toCSV()) }
      }
      def sendXML(fruits: Seq[Fruit]): Unit = {
        responder ! MessageChunk("<fruits>\n")
        fruits foreach { f => responder ! MessageChunk(f.toXML().toString + "\n") }
        responder ! MessageChunk("</fruits>")
      }

    }

}
