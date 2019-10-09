package triad
package http

import scala.scalanative.native._

object ArrayUtils {

  def toBuffer(array: Array[Byte])(implicit z: Zone): Ptr[Byte] = {
    val buffer = z.alloc(array.size)
    var i = 0
    while (i < array.size) {
      buffer(i) = array(i)
      i += 1
    }
    buffer
  }

  def toArray(buffer: Ptr[Byte], size: CSize): Array[Byte] = {
    val array = new Array[Byte](size.toInt)
    var i = 0
    while (i < array.size) {
      array(i) = buffer(i)
      i += 1
    }
    array
  }

}
