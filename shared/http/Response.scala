package triad
package http

case class Response(
    statusCode: Int,
    headers: Map[String, String],
    body: Array[Byte]
)
