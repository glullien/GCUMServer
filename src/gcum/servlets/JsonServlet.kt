package gcum.servlets

import gcum.utils.getLogger
import org.json.simple.JSONObject
import java.io.IOException
import java.util.*
import java.util.regex.Pattern
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private val log = getLogger()

abstract class JsonServlet : HttpServlet() {

   @Throws(ServletException::class, IOException::class)
   override fun doGet(request: HttpServletRequest, response: HttpServletResponse) = doPost(request, response)

   @Throws(ServletException::class, IOException::class)
   override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
      request.characterEncoding = "UTF-8"
      response.contentType = "text/html"
      response.characterEncoding = "UTF-8"
      response.outputStream.use {
         try {
            val jsonResult = doPost(request)
            it.println(JSONObject.toJSONString(jsonResult))
         } catch (e: Exception) {
            log.severe("Cannot process request", e)
            it.println(JSONObject.toJSONString(jsonError("internalError")))
         } catch (e: AssertionError) {
            log.severe("Cannot process request", e)
            it.println(JSONObject.toJSONString(jsonError("internalError")))
         } catch (t: Throwable) {
            log.severe("Cannot process request", t)
            throw t
         }
      }
   }

   @Throws(IOException::class)
   protected abstract fun doPost(request: HttpServletRequest): Map<String, *>
}

fun jsonError(errorMessage: String): Map<String, Any> {
   val res = HashMap<String, Any>()
   res.put("result", "error")
   res.put("message", htmlEncode(errorMessage))
   return res
}

fun jsonSuccess(init: MutableMap<String, Any>.() -> Unit): Map<String, Any> {
   val res = HashMap<String, Any>()
   res.put("result", "success")
   res.init()
   return res
}

fun sub(init: MutableMap<String, Any>.() -> Unit): Map<String, Any> {
   val res = HashMap<String, Any>()
   res.init()
   return res
}

fun ServletRequest.getStringOrNull(key: String, pattern: Pattern): String? {
   val parameter = getParameter(key)
   return if ((parameter == null) || (parameter.isEmpty())) null
   else if (pattern.matcher(parameter).matches()) parameter
   else throw IllegalArgumentException("Invalid $key:$parameter")
}
fun ServletRequest.getString(key: String, pattern: Pattern): String = getStringOrNull(key, pattern) ?: throw IllegalArgumentException("Missing $key")
inline fun <reified T : Enum<T>> ServletRequest.getEnum(key: String): T = java.lang.Enum.valueOf(T::class.java, getString(key, Pattern.compile(".*")))
inline fun <reified T : Enum<T>> ServletRequest.getEnums(key: String): List<T> = getString(key, Pattern.compile(".*")).split(',').map { java.lang.Enum.valueOf(T::class.java, it)}

private val DOUBLE = Pattern.compile("^\\d+\\.\\d+$")
fun ServletRequest.getDoubleOrNull(name: String): Double? {
   val s = getStringOrNull(name, DOUBLE)
   return if ((s == null)) null else s.toDouble()
}
fun ServletRequest.getDouble(key: String): Double = getDoubleOrNull(key) ?: throw IllegalArgumentException("Missing $key")

private val INT = Pattern.compile("^\\d+$")
fun ServletRequest.getIntOrNull(name: String): Int? {
   val s = getStringOrNull(name, INT)
   return if ((s == null)) null else s.toInt()
}
fun ServletRequest.getInt(key: String): Int = getIntOrNull(key) ?: throw IllegalArgumentException("Missing $key")

private val LONG = Pattern.compile("^\\d+$")
fun ServletRequest.getLongOrNull(name: String): Long? {
   val s = getStringOrNull(name, LONG)
   return if ((s == null)) null else s.toLong()
}
fun ServletRequest.getLong(key: String): Long = getLongOrNull(key) ?: throw IllegalArgumentException("Missing $key")
