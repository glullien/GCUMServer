package gcum.servlets

import gcum.db.CoordinatesSource
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
      val allPhotos = Database.allPoints[Point(latitude, longitude)]
      if ((allPhotos == null) || allPhotos.isEmpty()) throw IllegalAccessException("Photo not found")
      val inTimeFrame = when (request.getEnum<TimeFrame>("timeFrame")) {
         TimeFrame.All->allPhotos
         TimeFrame.LastDay->after(LocalDate.now().minusDays(1), allPhotos)
         TimeFrame.LastWeek->after(LocalDate.now().minusDays(7), allPhotos)
         TimeFrame.LastMonth->after(LocalDate.now().minusMonths(1), allPhotos)
      }
      val locationSources = request.getEnums<CoordinatesSource>("locationSources")
      val photos = inTimeFrame.filter {locationSources.contains(it.location.coordinates.source)}
      if (photos.isEmpty()) throw IllegalAccessException("Photos not found")
      val sortedPhotos = photos.sortedByDescending {it.moment}
      val username = username(request)
      return jsonSuccess {
         put("photos", sortedPhotos.map {sub {putPhotoInfo(it, username)}})
         val minDate = sortedPhotos.last().moment.date.format(DateTimeFormatter.ISO_DATE)
         val maxDate = sortedPhotos.first().moment.date.format(DateTimeFormatter.ISO_DATE)
         put("dates", minDate + if (maxDate == minDate) "" else " -> $maxDate")
         put("street", htmlEncode(photos.first().location.address.street))
      }
   }

   private fun after(date: LocalDate, source: Collection<Photo>) = source.filterNot {it.moment.date.isBefore(date)}
}