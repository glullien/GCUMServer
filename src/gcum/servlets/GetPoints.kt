package gcum.servlets

import gcum.db.Database
import gcum.geo.Point
import java.util.regex.Pattern
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest

@WebServlet(name = "GetPoints", value = "/getPoints")
class GetPoints : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val type = request.getString("type", Pattern.compile(".*"))
      return when (type) {
         "All"->encode(Database.points.keys)
         "Inside"->encode(inside(request))
         else->jsonError("Unknown type $type")
      }
   }

   private fun inside(request: HttpServletRequest): Set<Point> {
      val minLatitude = request.getLong("minLatitude")
      val minLongitude = request.getLong("minLongitude")
      val maxLatitude = request.getLong("maxLatitude")
      val maxLongitude = request.getLong("maxLongitude")
      return Database.getPoints(Point(minLatitude, minLongitude), Point(maxLatitude, maxLongitude))
   }

   private fun encode(res: Collection<Point>) = jsonSuccess {
      put("photos", res.map {
         point->
         sub {
            put("latitude", point.latitude)
            put("longitude", point.longitude)
         }
      })
   }
}