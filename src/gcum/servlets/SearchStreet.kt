package gcum.servlets

import gcum.geo.Point
import gcum.opendata.Arrondissements
import gcum.opendata.Voies
import gcum.opendata.VoiesArrondissements
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest

@WebServlet(name = "SearchStreet", value = "/searchStreet")
class SearchStreet : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val nbAnswers = Math.min(25, request.getInt("nbAnswers"))
      val pattern = request.getString("pattern")
      val result = Voies.searchBest(pattern, nbAnswers)
      return jsonSuccess {put("streets", result.map {sub {put("name", it.name)}})}
   }
}

@WebServlet(name = "SearchAddress", value = "/searchAddress")
class SearchAddress : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val nbAnswers = Math.min(25, request.getInt("nbAnswers"))
      val pattern = request.getString("pattern")
      val result = Voies.searchBest(pattern, nbAnswers)
      return jsonSuccess {
         put("streets", result.flatMap {
            street->
            VoiesArrondissements.districtsOrEmpty(street).map {
               sub {
                  put("street", street.name)
                  put("district", it)
                  put("city", "Paris")
               }
            }
         })
      }
   }
}

@WebServlet(name = "SearchClosest", value = "/searchClosest")
class SearchClosest : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val latitude = request.getLong("latitude")
      val longitude = request.getLong("longitude")
      val nb = request.getInt("nb")
      val point = Point(latitude, longitude)
      return jsonSuccess {
         if (nb == 1) {
            val street = Voies.searchClosest(point)
            val district = VoiesArrondissements.district(point, street) ?: throw Exception("Cannot find district")
            put("streets", listOf(sub {
               put("street", street.name)
               put("district", district)
               put("city", "Paris")
            }))
         } else {
            val streets = Voies.searchClosest(point, nb)
            put("streets", streets.flatMap {
               street->
               val districts = VoiesArrondissements.districtsOrEmpty(street)
               val districtsFromPoint = Arrondissements.search(point)
               val orderedDistricts = districts.intersect(districtsFromPoint) + districts.minus(districtsFromPoint)
               orderedDistricts.map {
                  sub {
                     put("street", street.name)
                     put("district", it)
                     put("city", "Paris")
                  }
               }
            })
         }
      }
   }
}