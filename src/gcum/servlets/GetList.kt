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
      fun mergeFilters(a: ((Photo) -> Boolean)?, b: ((Photo) -> Boolean)?) = if (a == null) b else if (b == null) a else {photo-> a(photo) && b(photo)}

      val number = request.getInt("number")
      val district = request.getStringOrNull("district") ?: "All"
      val author = request.getStringOrNull("author") ?: "<all>"
      val start = request.getStringOrNull("start")
      val after = request.getStringOrNull("after")
      val sort = request.getStringOrNull("sort") ?: "date"

      val districtInt = if (district == "All") null else district.toInt()
      val districtFilter: ((Photo) -> Boolean)? = if (districtInt == null) null else {photo-> photo.location.address.district == districtInt}

      val username = username(request)
      val authorUsername = if (author == "<all>") null else if (author == "<myself>") username else author
      val userFilter: ((Photo) -> Boolean)? = if (authorUsername == null) null else {photo-> photo.username == authorUsername}

      val filter = mergeFilters(districtFilter, userFilter)

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
      return jsonSuccess {
         put("photos", photos.list.map {sub {putPhotoInfo(it, username)}})
         put("nbAfter", photos.nbAfter)
      }
   }
}