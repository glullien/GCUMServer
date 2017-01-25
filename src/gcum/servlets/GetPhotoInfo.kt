package gcum.servlets

import gcum.db.Database
import java.time.format.DateTimeFormatter
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest


@WebServlet(name = "GetPhotoInfo", value = "/getPhotoInfo")
class GetPhotoInfo : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val id = request.getLong("id")
      val photo = Database.getPhoto(id) ?: throw ServletException("Photo $id not found")
      return jsonSuccess {
         put("date", photo.moment.date.format(DateTimeFormatter.ISO_DATE))
         put("time", photo.moment.time?.format(DateTimeFormatter.ISO_TIME) ?: "unknown")
         /*put("street", photo.location.address.street)
         put("district", photo.location.address.district)
         put("city", photo.location.address.city)
         put("latitude", photo.location.coordinates.point.latitude)
         put("longitude", photo.location.coordinates.point.longitude)
         put("locationSource", photo.location.coordinates.source)  */
      }
   }
}