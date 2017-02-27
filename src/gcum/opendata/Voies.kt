package gcum.opendata

import gcum.chars.bestLevenshteinIn
import gcum.db.coordinateToLong
import gcum.geo.Point
import gcum.geo.distance
import gcum.utils.Cache
import gcum.utils.first
import java.io.IOException
import java.util.regex.Pattern


private val margin = 100

data class Voie(val point: Point, val shapes: List<List<Point>>, val name: String) {
   val minLatitude = shapes.map {it.map {it.latitude}.min() ?: throw Exception("Missing point")}.min() ?: throw Exception("Missing point")
   val minLongitude = shapes.map {it.map {it.longitude}.min() ?: throw Exception("Missing point")}.min() ?: throw Exception("Missing point")
   val maxLatitude = shapes.map {it.map {it.latitude}.max() ?: throw Exception("Missing point")}.max() ?: throw Exception("Missing point")
   val maxLongitude = shapes.map {it.map {it.longitude}.max() ?: throw Exception("Missing point")}.max() ?: throw Exception("Missing point")
   fun distance(point: Point) = shapes.map {distance(it, point)}.min() ?: Double.POSITIVE_INFINITY
   fun relevant(point: Point) = (minLatitude - margin < point.latitude) && (minLongitude - margin < point.longitude) && (point.latitude < maxLatitude + margin) && (point.longitude < maxLongitude + margin)
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

      val regexUnnamed = Regex("^(passage|voie|Voie|place) [A-Za-z]{1,2}/\\d+$")
      voies = brut.filter {it[0] != "Geo Point"}.filter {it[4].trim().toInt() in 75000..75999}.filterNot {regexUnnamed.matches(it[9])}.map {
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

   /*fun searchBest(pattern: String, maxNumber: Int, isCancelled: AtomicBoolean = AtomicBoolean(false)): List<Voie> {
       return searchCache.get(pattern + "@" + maxNumber) {
          val res = bestLevenshteinIn(pattern, voies, {it.name}, maxNumber, isCancelled)
          if (isCancelled.get()) throw Exception("Cancelled")
          res
       }
    }*/

   fun searchBest(pattern: String, maxNumber: Int) = searchCache.get(pattern + "@" + maxNumber) {bestLevenshteinIn(pattern, voies, {it.name}, maxNumber)}
   fun searchBest(pattern: String) = searchBest(pattern, 1) [0]
   fun searchClosest(point: Point) = voies.minBy {it.distance(point)} ?: throw Exception("Impossible")
   fun searchClosest(point: Point, nb: Int) = voies.first(nb) {it.distance(point)}
   fun searchClosest2(point: Point, nb: Int) = voies.filter {it.relevant(point)}.first(nb) {it.distance(point)}
   fun get(street: String) = voies.firstOrNull {it.name == street}
}