package gcum.opendata

import gcum.chars.bestLevenshteinIn
import gcum.db.Address
import gcum.geo.Point
import gcum.utils.Cache

object VoiesArrondissements {
   val addresses: List<Address>

   init {
      val brut = Voies.javaClass.getResourceAsStream("streets.csv").use(::readCsv)
      addresses = brut.filter {it[0] != "TYPE DE VOIE"}.map {
         line->
         Address(line [0].trim(), line[1].toInt(), "Paris")
      }.filterNotNull().toList()
   }

   private val searchCache = Cache<String, Address>()
   private fun searchBest(name: String) = searchCache.get(name) {bestLevenshteinIn(name, addresses, {it.street}, 1) [0]}
   private fun searchExact(name: String) = addresses.filter {it.street == name}
   private fun search(name: String) = searchExact(searchBest(name).street)
   fun districts(name: String) = search(name).map {it.district}

   fun district(point: Point, street: String): Int? {
      val districtsFromPoint = Arrondissements.search(point)
      val districtsFromStreet = districts(street)
      val intersect = districtsFromPoint.intersect(districtsFromStreet)
      return intersect.firstOrNull() ?: districtsFromStreet.firstOrNull() ?: districtsFromPoint.firstOrNull()
   }

}