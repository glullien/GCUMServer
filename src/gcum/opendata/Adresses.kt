package gcum.opendata

import gcum.chars.bestLevenshteinIn
import gcum.db.coordinateToLong
import gcum.geo.Point
import java.io.File

object Addresses {
   val addresses: Map<String, Map<String, Point>>

   init {
      fun point(s: String): Point {
         val split = s.split(", ")
         return Point(coordinateToLong(split[0].toDouble()), coordinateToLong(split[1].toDouble()))
      }

      val cache = File("/tmp/adresse_paris_cache.csv")
      if (cache.exists()) {
         addresses = cache.inputStream().use(::readCsv).map {
            line->
            line[0] to line[1].split('/').map {
               val s = it.split(',')
               s[0] to Point(s[1].toLong(), s[2].toLong())
            }.toMap()
         }.toMap()
      } else {
         val brut = Voies.javaClass.getResourceAsStream("adresse_paris.csv").use(::readCsv)
         val numberPattern = Regex("^\\d+[a-zA-Z]*$")
         val list = brut.filter {it[0] != "Geometry X Y"}.map {
            line->
            val a = line[14]
            val s = a.indexOf(' ')
            if (s < 0) null else {
               val number = a.substring(0, s)
               if (!number.matches(numberPattern)) null
               else {
                  val street = a.substring(s + 1)
                  val point = point(line[0])
                  street to (number to point)
               }
            }
         }.filterNotNull()
         addresses = list.map {it.first}.toSet().associate {street-> street to list.filter {it.first == street}.map {it.second}.toMap()}

         File("/tmp/adresse_paris_cache.csv").printWriter().use {
            w->
            addresses.forEach {street, numbersPoints->
               w.println(street + ";" + numbersPoints.map {it.key + "," + it.value.latitude + "," + it.value.longitude}.joinToString("/"))
            }
         }
      }
   }

   private fun getAddresses(street: String) = bestLevenshteinIn(street, addresses.toList(), {a-> a.first}, 1).firstOrNull()?.second
   fun getNumber(street: String, point: Point) = getAddresses(street)?.minBy {point.distance(it.value)}?.key

}