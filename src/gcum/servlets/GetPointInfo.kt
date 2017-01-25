package gcum.servlets

import gcum.db.Database
import gcum.geo.Point
import java.time.format.DateTimeFormatter
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest

@WebServlet(name = "GetPointInfo", value = "/getPointInfo")
class GetPointInfo : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val latitude = request.getLong("latitude")
      val longitude = request.getLong("longitude")
      val photos = Database.points[Point(latitude, longitude)]
      return if ((photos == null) || photos.isEmpty()) jsonError("Photo not found") else jsonSuccess {
         put("ids", photos.map {it.id})
         val dates = photos.map {it.moment.date}
         val minDate = dates.min()?.format(DateTimeFormatter.ISO_DATE) ?: throw AssertionError("Impossible")
         val maxDate = dates.max()?.format(DateTimeFormatter.ISO_DATE) ?: throw AssertionError("Impossible")
         put("dates", minDate + if (maxDate == minDate) "" else " -> $maxDate")
         put("street", htmlEncode(photos.first().location.address.street))
      }
      /*return if (photo == null) jsonError("Photo not found") else jsonSuccess {
         put("date", photo.moment.date.format(DateTimeFormatter.ISO_DATE))
         put("time", photo.moment.time?.format(DateTimeFormatter.ISO_TIME) ?: "unknown")
         put("street", photo.location.address.street)
         put("district", photo.location.address.district)
      } */
   }
}