package triad

import akka.NotUsed
import akka.stream.scaladsl.{
  BroadcastHub,
  Keep,
  Source,
  SourceQueueWithComplete
}
import akka.stream.{Materializer, OverflowStrategy, QueueOfferResult}
import scala.concurrent.Future

/** A very basic streaming message router. */
class LiveMessages(implicit materializer: Materializer) {

  private val (
    in: SourceQueueWithComplete[Message],
    out: Source[Message, NotUsed]
  ) = Source
    .queue[Message](10, OverflowStrategy.dropTail)
    .toMat(BroadcastHub.sink[Message])(Keep.both)
    .run()

  /** Push a single message into the stream. */
  def push(message: Message): Future[QueueOfferResult] = in.offer(message)

  /** Obtain a stream of the message feed. */
  def feed: Source[Message, NotUsed] = out

}
