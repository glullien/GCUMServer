package gcum.geo

private fun sqrt(a: Double) = Math.sqrt(a)
private fun sqr(a: Long) = (1.0 * a) * a
private fun distanceSquare(a: Point, b: Point) = sqr(a.latitude - b.latitude) + sqr(a.longitude - b.longitude)
//fun distance(a: Point, b: Point) = sqrt(distanceSquare(a, b))
private fun distanceSquare(segmentFrom: Point, segmentTo: Point, point: Point): Double {
   val fromTo = distanceSquare(segmentFrom, segmentTo)
   if (fromTo == 0.0) return distanceSquare(segmentFrom, point)
   val t = ((point.latitude - segmentFrom.latitude) * (segmentTo.latitude - segmentFrom.latitude) + (point.longitude - segmentFrom.longitude) * (segmentTo.longitude - segmentFrom.longitude)) / fromTo
   val b = Math.max(0.0, Math.min(1.0, t))
   return distanceSquare(point, Point(
      (segmentFrom.latitude + b * (segmentTo.latitude - segmentFrom.latitude)).toLong(),
      (segmentFrom.longitude + b * (segmentTo.longitude - segmentFrom.longitude)).toLong()))
}

//fun distance(segmentFrom: Point, segmentTo: Point, point: Point) = sqrt(distanceSquare(segmentFrom, segmentTo, point))
private fun distanceSquare(segments: List<Point>, point: Point) = (1..(segments.size - 1)).map {distanceSquare(segments[it - 1], segments[it], point)}.min() ?: 1E100//throw Exception("No segments")
fun distance(segments: List<Point>, point: Point) = sqrt(distanceSquare(segments, point))


data class Point(val latitude: Long, val longitude: Long) {
   fun inside(min: Point, max: Point) = (min.latitude <= latitude) && (min.longitude <= longitude) && (latitude <= max.latitude) && (longitude <= max.longitude)
}