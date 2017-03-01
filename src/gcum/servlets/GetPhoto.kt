package gcum.servlets

import gcum.db.Database
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(name = "GetPhoto", value = "/getPhoto")
class GetPhoto : HttpServlet() {
   @Throws(ServletException::class, IOException::class)
   override fun doPost(request: HttpServletRequest, response: HttpServletResponse) = doGet(request, response)

   @Throws(ServletException::class, IOException::class)
   override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
      request.characterEncoding = "UTF-8"
      val id = request.getString("id")
      val maxSize = request.getIntOrNull("maxSize")
      val maxWidth = request.getIntOrNull("maxWidth")
      val maxHeight = request.getIntOrNull("maxHeight")
      val photo = Database.getPhoto(id) ?: throw ServletException("Photo $id not found")
      response.contentType = "image/jpeg"
      response.outputStream.use {
         if ((maxWidth != null) && (maxHeight != null)) photo.writeImage(it, maxWidth, maxHeight)
         else photo.writeImage(it, maxSize ?: Int.MAX_VALUE)
      }
   }

}