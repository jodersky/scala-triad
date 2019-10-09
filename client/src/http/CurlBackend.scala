package triad
package http

import curl._
import curlh._

import scala.collection.{Map, mutable}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.scalanative.native._
import scala.util.{Failure, Success, Try}

object CurlBackend {

  type Chunk = CStruct4[Ptr[CStruct0], Ptr[CStruct0], CSize, Ptr[Byte]]
  implicit class ChunksOps(val self: Ptr[Chunk]) extends AnyVal {
    @inline def prev: Ptr[Chunk] = (!self._1).cast[Ptr[Chunk]]
    @inline def prev_=(other: Ptr[Chunk]): Unit =
      !(self._1) = other.cast[Ptr[CStruct0]]
    @inline def next: Ptr[Chunk] = (!self._2).cast[Ptr[Chunk]]
    @inline def next_=(other: Ptr[Chunk]): Unit =
      !(self._2) = other.cast[Ptr[CStruct0]]
    @inline def size: CSize = !(self._3)
    @inline def size_=(value: CSize): Unit = !(self._3) = value
    @inline def buffer: Ptr[Byte] = !(self._4)
    @inline def buffer_=(value: Ptr[Byte]): Unit = !(self._4) = value
  }

  object Chunk {

    @inline def NullPtr[T] = 0.cast[Ptr[T]]

    def allocHead() = allocAppend(0, NullPtr[Chunk])

    def allocAppend(size: CSize,
                    head: Ptr[Chunk] = NullPtr[Chunk]): Ptr[Chunk] = {
      val chunk: Ptr[Chunk] = stdlib.malloc(sizeof[Chunk]).cast[Ptr[Chunk]]
      if (chunk == NullPtr[Chunk]) return NullPtr[Chunk]

      chunk.buffer = stdlib.malloc(size)

      if (chunk.buffer == NullPtr[Chunk] && size != 0) {
        stdlib.free(chunk.cast[Ptr[Byte]])
        return NullPtr[Chunk]
      }
      chunk.size = size

      if (head == NullPtr[Chunk]) { // this will be the head
        chunk.next = chunk
        chunk.prev = chunk
      } else {
        val last = head.prev
        last.next = chunk
        chunk.prev = last
        head.prev = chunk
        chunk.next = head
      }
      chunk
    }

    def freeAll(head: Ptr[Chunk]): Unit = {
      var chunk: Ptr[Chunk] = head
      do {
        val next = chunk.next
        stdlib.free(chunk.buffer)
        stdlib.free(chunk.cast[Ptr[Byte]])
        chunk = next
      } while (chunk != head)
    }

    def toArray(head: Ptr[Chunk]): Array[Byte] = {
      val buffer = new ArrayBuffer[Byte]()
      var chunk = head
      do {
        val next = chunk.next
        var i = 0l
        while (i < next.size) {
          buffer += next.buffer(i)
          i += 1
        }
        chunk = next
      } while (chunk != head)
      buffer.toArray
    }

    def traverse(head: Ptr[Chunk])(fct: Array[Byte] => Unit) = {
      var chunk = head
      do {
        val next = chunk.next
        val buffer = new ArrayBuffer[Byte]()
        var i = 0l
        while (i < next.size) {
          buffer += next.buffer(i)
          i += 1
        }
        chunk = next
        fct(buffer.toArray)
      } while (chunk != head)
    }

  }

  private def receive(data: Ptr[Byte],
                      size: CSize,
                      nmemb: CSize,
                      userdata: Ptr[Byte]): CSize = {
    val head = userdata.cast[Ptr[Chunk]]
    val length = size * nmemb
    val chunk = Chunk.allocAppend(length, head)
    string.memcpy(chunk.buffer, data, chunk.size)
    chunk.size
  }
  private val receivePtr: WriteFunction = CFunctionPtr.fromFunction4(receive)

  private def chain[A](success: A)(calls: (() => A)*) = {
    var result: A = success
    for (c <- calls if result == success) {
      result = c()
    }
    result
  }

  private def request(request: Request)(implicit z: Zone): Try[Response] = {
    val curl: CURL = curl_easy_init()
    if (curl != null) {
      val errorBuffer = stackalloc[Byte](CURL_ERROR_SIZE)
      !errorBuffer = 0
      val requestHeaders = stackalloc[curl_slist](1)
      !requestHeaders = 0.cast[curl_slist]

      val responseChunks = Chunk.allocHead()
      val responseHeaderChunks = Chunk.allocHead()

      val curlResult = chain(CURLcode.CURL_OK)(
        () =>
          curl_easy_setopt(curl, CURLoption.CURLOPT_ERRORBUFFER, errorBuffer),
        () =>
          curl_easy_setopt(curl,
                           CURLoption.CURLOPT_CUSTOMREQUEST,
                           toCString(request.method)),
        () =>
          curl_easy_setopt(curl,
                           CURLoption.CURLOPT_URL,
                           toCString(request.url)),
        () => {
          val buffer = ArrayUtils.toBuffer(request.body)
          curl_easy_setopt(curl, CURLoption.CURLOPT_POSTFIELDS, buffer)
          curl_easy_setopt(curl,
                           CURLoption.CURLOPT_POSTFIELDSIZE,
                           request.body.size)
        },
        () => {
          for ((k, v) <- request.headers) {
            !requestHeaders =
              curl_slist_append(!requestHeaders, toCString(s"$k:$v"))
          }
          curl_easy_setopt(curl, CURLoption.CURLOPT_HTTPHEADER, !requestHeaders)
        },
        () =>
          curl_easy_setopt(curl, CURLoption.CURLOPT_WRITEFUNCTION, receivePtr),
        () =>
          curl_easy_setopt(curl, CURLoption.CURLOPT_WRITEDATA, responseChunks),
        () =>
          curl_easy_setopt(curl,
                           CURLoption.CURLOPT_HEADERDATA,
                           responseHeaderChunks),
        () => curl_easy_perform(curl)
      )

      val result = curlResult match {
        case CURLcode.CURL_OK =>
          val responseCode: Ptr[Long] = stackalloc[Long](1)
          curl_easy_getinfo(curl, CURLINFO.CURLINFO_RESPONSE_CODE, responseCode)

          val responseHeaders = mutable.HashMap.empty[String, String]
          Chunk.traverse(responseHeaderChunks) { headerChunk =>
            val line = new String(headerChunk, "utf-8").trim
            if (line.contains(":")) {
              val parts = line.split(":", 2)
              responseHeaders += parts(0) -> parts(1)
            }
          }

          Success(
            Response(
              statusCode = (!responseCode).toInt,
              headers = responseHeaders.toMap,
              body = Chunk.toArray(responseChunks)
            ))

        case code =>
          val message = curl_easy_strerror(curl, code)
          Failure(
            new RuntimeException(
              s"${fromCString(errorBuffer)} (curl exit status $code)"))
      }
      Chunk.freeAll(responseChunks)
      Chunk.freeAll(responseHeaderChunks)
      curl_slist_free_all(!requestHeaders)
      curl_easy_cleanup(curl)
      result
    } else {
      Failure(new RuntimeException(s"curl failed to initialize"))
    }
  }

  def curlVersion = fromCString(curl_version())

}

trait CurlBackend extends Backend {
  def send(req: Request): Future[Response] = Zone { implicit z =>
    Future.fromTry(CurlBackend.request(req))
  }
}
