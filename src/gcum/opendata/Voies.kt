package gcum.opendata

import gcum.chars.bestLevenshteinIn
import gcum.db.coordinateToLong
import gcum.geo.Point
import gcum.geo.distance
import gcum.utils.Cache
import java.io.IOException
import java.util.regex.Pattern

data class Voie(val point: Point, val shapes: List<Point>, val name: String) {
   fun distance(point: Point) = distance(shapes, point)
}

object Voies {
   val voies: List<Voie>

   init {
      val brut = readCsv(Voies.javaClass.getResourceAsStream("voies-paris-et-petite-couronne.csv"))
      val coordinates = Pattern.compile(".*\"LineString\".*(\\[\\[.*\\]\\]).*")
      fun getPoint(source: String, latitudeFirst: Boolean): Point {
         fun getCoordinate(s: String) = coordinateToLong(s.trim().toDouble())
         val geo = source.split(',')
         val i = if (latitudeFirst) 0 else 1
         return Point(getCoordinate(geo[i]), getCoordinate(geo[1 - i]))
      }
      voies = brut.filter {it[0] != "Geo Point"}.filter {it[4].trim().toInt() in 75000..75999}.filterNot {it[9].startsWith("Voie Dg/")}.map {
         line->
         if (!line [1].contains("MultiLineString")) {              //TODO
            val shapeMatcher = coordinates.matcher(line[1])
            if (!shapeMatcher.matches()) throw IOException("Cannot parse $line")
            val shape = shapeMatcher.group(1).split("], [").map {it.trim('[', ']')}.map {getPoint(it, false)}
            if (shape.size < 2) throw Exception("Illegal shape")
            Voie(getPoint(line [0], true), shape, line[9].trim())
         } else {
            Voie(getPoint(line [0], true), listOf<Point>(), line[9].trim())
         }
      }.filterNotNull().toList()
   }

   private val searchCache = Cache<String, Voie>()
   fun search(pattern: String, maxNumber: Int) = bestLevenshteinIn(pattern, voies, {it.name}, maxNumber)
   fun search(pattern: String) = searchCache.get(pattern) {search(pattern, 1) [0]}
   fun search(point: Point) = voies.minBy {it.distance(point)} ?: throw Exception("Impossible")
   fun get(street: String) = voies.firstOrNull {it.name == street}
}