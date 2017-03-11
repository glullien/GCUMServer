package gcum.opendata

import gcum.chars.toStdLowerChars
import gcum.db.Address
import gcum.geo.Point

object VoiesArrondissements {
   val addresses: Map<String, List<Int>>

   init {
      val brut = Voies.javaClass.getResourceAsStream("streets.csv").use(::readCsv)
      val regexUnnamed = Regex("^(passage|voie|Voie|place) [A-Za-z]{1,2}/\\d+$")
      val l = brut.filter {it[0] != "TYPE DE VOIE"}.filterNot {regexUnnamed.matches(it[0])}.map {
         line->
         Address(null, line [0].trim(), line[1].toInt(), "Paris")
      }.filterNotNull()

      fun String.normalize() = toStdLowerChars().replace('-', ' ')
      val voies = Voies.voies.associate {it.name.normalize() to it}
      fun voie(street: String) = voies [street.normalize()] ?: throw Exception("missing street $street")
      addresses = l.map {it.street}.toSet().associate {street-> voie(street).name to l.filter {it.street == street}.map {it.district}}
   }

   fun districts(voie: Voie) = addresses[voie.name] ?: throw Exception("missing street ${voie.name}")
   fun districtsOrNull(voie: Voie) = addresses[voie.name]
   fun districtsOrEmpty(voie: Voie) = addresses[voie.name] ?: emptyList()

   fun district(point: Point, voie: Voie): Int? {
      val districtsFromPoint = Arrondissements.search(point)
      val districtsFromStreet = districtsOrEmpty(voie)
      val intersect = districtsFromPoint.intersect(districtsFromStreet)
      return intersect.firstOrNull() ?: districtsFromStreet.firstOrNull() ?: districtsFromPoint.firstOrNull()
   }

}