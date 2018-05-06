package triad
package http

case class Request(
    method: String,
    url: String,
    headers: Map[String, String] = Map.empty,
    body: Array[Byte] = Array.empty
)
