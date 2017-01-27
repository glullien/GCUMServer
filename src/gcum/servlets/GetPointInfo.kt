package gcum.servlets

import gcum.db.Database
import gcum.db.Photo
import gcum.geo.Point
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest

@WebServlet(name = "GetPointInfo", value = "/getPointInfo")
class GetPointInfo : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val latitude = request.getLong("latitude")
      val longitude = request.getLong("longitude")
      val allPhotos = Database.points[Point(latitude, longitude)]
      if ((allPhotos == null) || allPhotos.isEmpty()) throw IllegalAccessException("Photo not found")
      val photos = when (request.getEnum<TimeFrame>("timeFrame")) {
         TimeFrame.All->allPhotos
         TimeFrame.LastDay->after(LocalDate.now().minusDays(1), allPhotos)
         TimeFrame.LastWeek->after(LocalDate.now().minusDays(7), allPhotos)
         TimeFrame.LastMonth->after(LocalDate.now().minusMonths(1), allPhotos)
      }
      if (photos.isEmpty()) throw IllegalAccessException("Photos not found")
      return jsonSuccess {
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

   private fun after(date: LocalDate, source: Collection<Photo>) = source.filterNot {it.moment.date.isBefore(date)}
}