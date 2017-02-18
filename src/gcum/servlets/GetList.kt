package gcum.servlets

import gcum.db.Database
import java.time.format.DateTimeFormatter
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest

@WebServlet(name = "GetList", value = "/getList")
class GetList : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val number = request.getInt("number")
      val district = request.getString("district")
      val start = request.getString("start")
      val photos = Database.getPhotos(
         number,
         if (district == "All") null else district.toInt(),
         if (start == "Latest") null else start.toLong()
      )
      val username = Sessions.username(request.session)
      return jsonSuccess {
         put("photos", photos.map {
            photo->
            sub {
               put("date", photo.moment.date.format(DateTimeFormatter.ISO_DATE))
               put("time", photo.moment.time?.format(DateTimeFormatter.ISO_TIME) ?: "unknown")
               put("street", photo.location.address.street)
               put("district", photo.location.address.district)
               put("city", photo.location.address.city)
               put("locationSource", photo.location.coordinates.source.toString())
               put("latitude", photo.location.coordinates.point.latitude)
               put("longitude", photo.location.coordinates.point.longitude)
               if (photo.username != null) put("username", photo.username)
               put("likesCount", photo.likes.size)
               put("isLiked", photo.likes.contains(username))
               put("id", photo.id)
            }
         })
      }
   }
}