package gcum.opendata

import gcum.chars.bestLevenshteinIn
import gcum.geo.Point
import gcum.utils.Cache

object Voies {

   data class Voie(val point: Point, val name: String)

   val voies: List<Voie>

   init {
      val brut = readCsv(Voies.javaClass.getResourceAsStream("voies-paris-et-petite-couronne.csv"))
      voies = brut.filter {it[0] != "Geo Point"}.map {
         line->
         if (line[4].trim().toInt() in 75000..75999) {
            val geo = line[0].split(',')
            fun getCoordinate(s: String) = (s.trim().toDouble() * 1E10).toLong()
            Voie(Point(getCoordinate(geo[0]), getCoordinate(geo[1])), line[9].trim())
         } else null
      }.filterNotNull().toList()
   }

   private val searchCache = Cache<String, Voie>()
   fun search(name: String) = searchCache.get(name) {bestLevenshteinIn(name, voies, {it.name}, 1) [0]}
}