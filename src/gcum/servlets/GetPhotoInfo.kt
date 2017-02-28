package gcum.servlets

import gcum.db.Database
import java.time.format.DateTimeFormatter
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest

@WebServlet(name = "GetPhotoInfo", value = "/getPhotoInfo")
class GetPhotoInfo : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val id = request.getString("id")
      val photo = Database.getPhoto(id) ?: throw ServletException("Photo $id not found")
      return jsonSuccess {
         put("date", photo.moment.date.format(DateTimeFormatter.ISO_DATE))
         put("time", photo.moment.time?.format(DateTimeFormatter.ISO_TIME) ?: "unknown")
         /*put("street", photo.location.address.street)
         put("district", photo.location.address.district)
         put("city", photo.location.address.city)
         put("latitude", photo.location.coordinates.point.latitude)
         put("longitude", photo.location.coordinates.point.longitude)*/
         put("locationSource", photo.location.coordinates.source.toString())
         if (photo.username != null) put("username", photo.username)
         put("likesCount", photo.likes.size)
         put("isLiked", photo.likes.contains(Sessions.username(request.session)))
      }
   }
}

@WebServlet(name = "ToggleLike", value = "/toggleLike")
class ToggleLike : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val photoId = request.getString("photoId")
      val username = username(request) ?: return jsonError("Vous devez être connecté")
      Database.toggleLike(photoId, username)
      val photo = Database.getPhoto(photoId)
      return jsonSuccess {
         put("likesCount", photo?.likes?.size ?: 0)
         put("isLiked", photo?.likes?.contains(username) ?: false)
      }
   }
}