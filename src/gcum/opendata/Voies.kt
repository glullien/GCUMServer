package gcum.opendata

import gcum.chars.bestLevenshteinIn
import gcum.db.coordinateToLong
import gcum.geo.Point
import gcum.geo.distance
import gcum.utils.Cache
import gcum.utils.min
import java.io.IOException
import java.util.regex.Pattern

data class Voie(val point: Point, val shapes: List<List<Point>>, val name: String) {
   fun distance(point: Point) = shapes.map {distance(it, point)}.min() ?: Double.POSITIVE_INFINITY
}

object Voies {
   val voies: List<Voie>

   init {
      val brut = Voies.javaClass.getResourceAsStream("voies-paris-et-petite-couronne.csv").use(::readCsv)
      val coordinatesLine = Pattern.compile(".*\"LineString\".*(\\[\\[.*\\]\\]).*")
      val coordinatesMultiLine = Pattern.compile(".*\"MultiLineString\".*(\\[\\[\\[.*\\]\\]\\]).*")
      fun getPoint(source: String, latitudeFirst: Boolean): Point {
         fun getCoordinate(s: String) = coordinateToLong(s.trim().toDouble())
         val geo = source.split(',')
         val i = if (latitudeFirst) 0 else 1
         return Point(getCoordinate(geo[i]), getCoordinate(geo[1 - i]))
      }
      voies = brut.filter {it[0] != "Geo Point"}.filter {it[4].trim().toInt() in 75000..75999}.filterNot {it[9].startsWith("Voie Dg/")}.map {
         line->
         if (line [1].contains("MultiLineString")) {
            val shapeMatcher = coordinatesMultiLine.matcher(line[1])
            if (!shapeMatcher.matches()) throw IOException("Cannot parse $line")
            val shapes = shapeMatcher.group(1).split("]], [[").map {it.split("], [").map {it.trim('[', ']')}.map {getPoint(it, false)}}
            Voie(getPoint(line [0], true), shapes, line[9].trim())
         } else if (line [1].contains("LineString")) {
            val shapeMatcher = coordinatesLine.matcher(line[1])
            if (!shapeMatcher.matches()) throw IOException("Cannot parse $line")
            val shape = shapeMatcher.group(1).split("], [").map {it.trim('[', ']')}.map {getPoint(it, false)}
            if (shape.size < 2) throw Exception("Illegal shape")
            Voie(getPoint(line [0], true), listOf(shape), line[9].trim())
         } else {
            Voie(getPoint(line [0], true), listOf<List<Point>>(), line[9].trim())
         }
      }.filterNotNull().toList()
   }

   private val searchCache = Cache<String, List<Voie>>()
   fun searchBest(pattern: String, maxNumber: Int) = searchCache.get(pattern + "@" + maxNumber) {bestLevenshteinIn(pattern, voies, {it.name}, maxNumber)}
   fun searchBest(pattern: String) = searchBest(pattern, 1) [0]
   fun searchClosest(point: Point) = voies.minBy {it.distance(point)} ?: throw Exception("Impossible")
   fun searchClosest(point: Point, nb: Int) = voies.sortedBy {it.distance(point)}.subList(0, nb.min(voies.size))
   fun get(street: String) = voies.firstOrNull {it.name == street}
}