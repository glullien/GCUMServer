package gcum.geo

data class Point(val latitude: Long, val longitude: Long) {
   fun inside(min: Point, max: Point) = (min.latitude <= latitude) && (min.longitude <= longitude) && (latitude <= max.latitude) && (longitude <= max.longitude)

   fun flatDistance(segments: List<Point>): Double {
      fun sqr(a: Long) = (1.0 * a) * a
      fun pointToPoint(a: Point, b: Point) = sqr(a.latitude - b.latitude) + sqr(a.longitude - b.longitude)
      fun segmentDistance(segmentFrom: Point, segmentTo: Point): Double {
         val fromTo = pointToPoint(segmentFrom, segmentTo)
         if (fromTo == 0.0) return pointToPoint(segmentFrom, this)
         val t = ((latitude - segmentFrom.latitude) * (segmentTo.latitude - segmentFrom.latitude) + (longitude - segmentFrom.longitude) * (segmentTo.longitude - segmentFrom.longitude)) / fromTo
         val b = Math.max(0.0, Math.min(1.0, t))
         return pointToPoint(this, Point(
            (segmentFrom.latitude + b * (segmentTo.latitude - segmentFrom.latitude)).toLong(),
            (segmentFrom.longitude + b * (segmentTo.longitude - segmentFrom.longitude)).toLong()))
      }
      return (1..(segments.size - 1)).map {segmentDistance(segments[it - 1], segments[it])}.min() ?: 1E100
   }

   fun distance(other: Point): Long {
      fun toRadian(degree: Long) = Math.PI * degree.toDouble() * 1E-5 / 180
      val R = 6378000
      val latA = toRadian(latitude)
      val lonA = toRadian(longitude)
      val latB = toRadian(other.latitude)
      val lonB = toRadian(other.longitude)
      return Math.round(R * (Math.PI / 2 - Math.asin(Math.sin(latB) * Math.sin(latA) + Math.cos(lonB - lonA) * Math.cos(latB) * Math.cos(latA))))
   }
}
