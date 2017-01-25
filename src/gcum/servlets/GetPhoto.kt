package gcum.servlets

import gcum.db.Database
import java.io.IOException
import javax.imageio.ImageIO
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(name = "GetPhoto", value = "/getPhoto")
class GetPhoto : HttpServlet() {
   @Throws(ServletException::class, IOException::class)
   override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
      request.characterEncoding = "UTF-8"
      val id = request.getLong("id")
      val maxSize = request.getIntOrNull("maxSize")
      val photo = Database.getPhoto(id) ?: throw ServletException("Photo $id not found")
      val image = photo.getImage(maxSize ?: Int.MAX_VALUE)
      response.contentType = "image/jpeg"
      response.outputStream.use {ImageIO.write(image, "JPG", it)}
   }

}