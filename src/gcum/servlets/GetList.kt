package gcum.servlets

import gcum.db.Database
import gcum.db.Photo
import gcum.db.PhotosListStart
import gcum.db.firstPhoto
import gcum.geo.Point
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest

@WebServlet(name = "GetList", value = "/getList")
class GetList : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val number = request.getInt("number")
      val district = request.getString("district")
      val start = request.getStringOrNull("start")
      val after = request.getStringOrNull("after")
      val sort = request.getStringOrNull("sort") ?: "date"
      val filter: ((Photo) -> Boolean)? = if (district == "All") null else {photo: Photo-> photo.location.address.district == district.toInt()}
      val comparator = when (sort) {
         "date"->Comparator<Photo> {o1, o2-> o2.moment.compareTo(o1.moment)}
         "closest"-> {
            val point = Point(request.getLong("latitude"), request.getLong("longitude"))
            Comparator<Photo> {o1, o2-> o1.location.coordinates.point.distance(point).compareTo(o2.location.coordinates.point.distance(point))}
         }
         else->throw JsonServletException("Illegal sort $sort")
      }
      val photosListStart = if (start == null && after == null) firstPhoto else
         if (start == "Latest") firstPhoto else
            if (start != null) PhotosListStart(start, 0) else PhotosListStart(after, 1)
      val photos = Database.getPhotos(number, filter, comparator, photosListStart)
      val username = Sessions.username(request.session)
      return jsonSuccess {
         put("photos", photos.list.map {sub {putPhotoInfo(it, username)}})
         put("nbAfter", photos.nbAfter)
      }
   }
}