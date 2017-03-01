package gcum.opendata

import gcum.db.coordinateToLong
import gcum.geo.Point
import java.io.IOException
import java.util.regex.Pattern

data class Arrondissement(val id: Int, val shape: List<Point>) {
   fun inside(point: Point): Boolean {
      val latitude = point.latitude * 1.0
      val longitude = point.longitude * 1.0
      fun intersect(a: Point, b: Point): Boolean {
         if ((a.latitude - latitude) * (b.latitude - latitude) > 0) return false
         else {
            val p = (latitude - a.latitude) / (b.latitude - a.latitude)
            val root =a.longitude+ p * (b.longitude - a.longitude)
            return root < longitude
         }
      }

      fun nbIntersects(a: Point, b: Point, other: List<Point>): Int =
         (if (intersect(a, b)) 1 else 0) + (if (other.size < 2) 0 else nbIntersects(other[0], other [1], other.drop(1)))

      return (nbIntersects(shape[shape.size - 1], shape [0], shape).rem(2)) == 1
   }
}

object Arrondissements {
   val arrondissements: List<Arrondissement>

   init {
      val brut = Voies.javaClass.getResourceAsStream("arrondissements.csv").use(::readCsv)
      val coordinates = Pattern.compile(".*\"Polygon\".*(\\[\\[\\[.*\\]\\]\\]).*")
      fun getPoint(source: String): Point {
         fun getCoordinate(s: String) = coordinateToLong(s.trim().toDouble())
         val geo = source.split(',')
         return Point(getCoordinate(geo[1]), getCoordinate(geo[0]))
      }
      arrondissements = brut.filter {it[0] != "N_SQ_AR"}.map {
         line->
         val shapeMatcher = coordinates.matcher(line[9])
         if (!shapeMatcher.matches()) throw IOException("Cannot parse $line")
         val shape = shapeMatcher.group(1).split("], [").map {it.trim('[', ']')}.map(::getPoint)
         if (shape.size < 2) throw Exception("Illegal shape")
         Arrondissement(line[1].toInt(), shape)
      }.filterNotNull().toList()
   }

   fun search(point: Point) = arrondissements.filter {it.inside(point)}.map {it.id}
}