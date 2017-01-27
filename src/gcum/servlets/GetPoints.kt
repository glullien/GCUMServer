package gcum.servlets

import gcum.db.Database
import gcum.db.Photo
import gcum.geo.Point
import java.time.LocalDate
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest

@WebServlet(name = "GetPoints", value = "/getPoints")
class GetPoints : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val all = Database.points
      val inZone = when (request.getEnum<Zone>("zone")) {
         Zone.All->all
         Zone.Inside->inside(request, all)
      }
      val inTimeFrame = when (request.getEnum<TimeFrame>("timeFrame")) {
         TimeFrame.All->inZone
         TimeFrame.LastDay->after(LocalDate.now().minusDays(1), inZone)
         TimeFrame.LastWeek->after(LocalDate.now().minusDays(7), inZone)
         TimeFrame.LastMonth->after(LocalDate.now().minusMonths(1), inZone)
      }
      return encode(inTimeFrame.keys)
   }

   private fun after(date: LocalDate, source: Map<Point, Collection<Photo>>) = source.filterValues {it.any {!it.moment.date.isBefore(date)}}

   private fun inside(request: HttpServletRequest, source: Map<Point, Collection<Photo>>): Map<Point, Collection<Photo>> {
      val minLatitude = request.getLong("minLatitude")
      val minLongitude = request.getLong("minLongitude")
      val maxLatitude = request.getLong("maxLatitude")
      val maxLongitude = request.getLong("maxLongitude")
      val min = Point(minLatitude, minLongitude)
      val max = Point(maxLatitude, maxLongitude)
      return source.filterKeys {it.inside(min, max)}
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