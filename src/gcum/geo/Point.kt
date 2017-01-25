package gcum.geo

data class Point(val latitude: Long, val longitude: Long) {
   fun inside(min: Point, max: Point) = (min.latitude <= latitude) && (min.longitude <= longitude) && (latitude <= max.latitude) && (longitude <= max.longitude)
}