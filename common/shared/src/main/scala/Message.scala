package triad

import java.security.MessageDigest
import java.time.Instant

case class Message(content: String,
                   author: String,
                   timestamp: Instant = Instant.now()) {

  lazy val id: String = {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update(content.getBytes)
    digest.update(author.getBytes)
    digest.update((timestamp.getEpochSecond & 0xff).toByte)
    digest.update(((timestamp.getEpochSecond >> 8) & 0xff).toByte)
    digest.update(((timestamp.getEpochSecond >> 16) & 0xff).toByte)
    digest.update(((timestamp.getEpochSecond >> 24) & 0xff).toByte)
    digest.update(((timestamp.getEpochSecond >> 32) & 0xff).toByte)
    digest.update(((timestamp.getEpochSecond >> 40) & 0xff).toByte)
    digest.update(((timestamp.getEpochSecond >> 48) & 0xff).toByte)
    digest.update(((timestamp.getEpochSecond >> 56) & 0xff).toByte)
    Message.bytesToHex(digest.digest())
  }

  def hashTags: Seq[String] =
    content.split("\\s").filter(_.startsWith("#")).map(_.drop(1))

}

object Message {
  private def bytesToHex(hash: Array[Byte]): String = {
    val hexString = new StringBuffer(hash.length * 2)
    var i = 0
    while (i < hash.length) {
      val hex = Integer.toHexString(0xff & hash(i))
      if (hex.length == 1) hexString.append('0')
      hexString.append(hex)
      i += 1
    }
    hexString.toString
  }
}
