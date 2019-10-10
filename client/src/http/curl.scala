package triad
package http

import scala.scalanative.native._

object curlh {
  final val CURL_ERROR_SIZE = 256

  type CURL = Ptr[CStruct0]

  /*
   * #define CURLOPTTYPE_LONG          0
   * #define CURLOPTTYPE_OBJECTPOINT   10000
   * #define CURLOPTTYPE_STRINGPOINT   10000
   * #define CURLOPTTYPE_FUNCTIONPOINT 20000
   * #define CURLOPTTYPE_OFF_T         30000
   */

  type CURLcode = CInt
  object CURLcode {
    final val CURL_OK: CInt = 0
  }

  type CURLoption = CInt
  object CURLoption {
    final val CURLOPT_VERBOSE: CInt = 41
    final val CURLOPT_POSTFIELDSIZE: CInt = 60
    final val CURLOPT_WRITEDATA: CInt = 10001
    final val CURLOPT_URL: CInt = 10002
    final val CURLOPT_ERRORBUFFER: CInt = 10010
    final val CURLOPT_POSTFIELDS: CInt = 10015
    final val CURLOPT_HTTPHEADER: CInt = 10023
    final val CURLOPT_HEADERDATA: CInt = 10029
    final val CURLOPT_CUSTOMREQUEST: CInt = 10036
    final val CURLOPT_WRITEFUNCTION: CInt = 20011
  }

  type CURLINFO = CInt
  object CURLINFO {
    final val CURLINFO_RESPONSE_CODE = 0x200002
  }

  type WriteFunction = CFunctionPtr4[
    Ptr[Byte], // data
    CSize, // size
    CSize, // nmemb
    Ptr[Byte], // userdata
    CSize // return
  ]

  type curl_slist = Ptr[CStruct0]

}

@link("curl")
@extern
object curl {
  import curlh._

  def curl_easy_init(): CURL = extern

  def curl_easy_setopt(
      curl: CURL,
      option: CURLoption,
      parameter: CVararg*
  ): CURLcode = extern

  def curl_easy_perform(curl: CURL): CURLcode = extern

  def curl_easy_getinfo(
      curl: CURL,
      option: CURLINFO,
      parameter: CVararg*
  ): CURLcode = extern

  def curl_easy_cleanup(curl: CURL): Unit = extern

  def curl_easy_strerror(curl: CURL, code: CURLcode): CString = extern

  def curl_slist_append(head: curl_slist, string: CString): curl_slist = extern

  def curl_slist_free_all(head: curl_slist): Unit = extern

  def curl_version(): CString = extern

}
