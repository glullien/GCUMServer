package gcum.servlets

import gcum.opendata.Voies
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest

@WebServlet(name = "SearchStreet", value = "/searchStreet")
class SearchStreet : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val nbAnswers = Math.min(25, request.getInt("nbAnswers"))
      val pattern = request.getString("pattern")
      val result = Voies.search(pattern, nbAnswers)
      //return jsonSuccess {put("streets", result.map {it.name})}
      return jsonSuccess {put("streets", result.map {sub {put("name", it.name)}})}
   }
}