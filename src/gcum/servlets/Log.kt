package gcum.servlets

import gcum.utils.getLogger
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest

private val log = getLogger()

@WebServlet(name = "Log", value = "/log")
class Log : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val username = username(request)
      val message = request.getString("message")
      val exception = request.getStringOrNull("exception")?.split('\\')?.joinToString("\n")
      log.info(message + (if (username != null) " from " + username else "") + (if (exception != null) " " + exception else ""))
      return jsonSuccess {}
   }
}