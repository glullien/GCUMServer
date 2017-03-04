package gcum.servlets

import gcum.db.Database
import gcum.db.PhotosListStart
import gcum.db.firstPhoto
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest

@WebServlet(name = "GetList", value = "/getList")
class GetList : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val number = request.getInt("number")
      val district = request.getString("district")
      val start = request.getStringOrNull("start")
      val after = request.getStringOrNull("after")
      val photos = Database.getPhotos(
         number,
         if (district == "All") null else district.toInt(),
         if (start == null && after == null) firstPhoto else
            if (start == "Latest") firstPhoto else
               if (start != null) PhotosListStart(start, 0) else PhotosListStart(after, 1)
      )
      val username = Sessions.username(request.session)
      return jsonSuccess {
         put("photos", photos.list.map {sub {putPhotoInfo(it, username)}})
         put("nbAfter", photos.nbAfter)
      }
   }
}