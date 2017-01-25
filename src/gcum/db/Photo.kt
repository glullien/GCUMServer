package gcum.db

import gcum.conf.KProperties
import gcum.geo.Point
import gcum.opendata.Voies
import java.awt.image.BufferedImage
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicLong
import java.util.regex.Pattern
import javax.imageio.ImageIO

private val PROPERTIES_DISTRICT = "district"
private val PROPERTIES_STREET = "street"
private val PROPERTIES_DATE = "date"
private val PROPERTIES_LATITUDE = "latitude"
private val PROPERTIES_LONGITUDE = "longitude"
private val PROPERTIES_COORDINATES_SOURCE = "coordinates.source"
private val PARIS = "Paris"

enum class CoordinatesSource {Street }
data class Moment(val date: LocalDate, val time: LocalTime?)
data class Address(val street: String, val district: Int, val city: String)
data class Coordinates(val point: Point, val source: CoordinatesSource)
data class Location(val address: Address, val coordinates: Coordinates)

data class Photo(val id: Long, val moment: Moment, val location: Location, val file: File) {
   fun inside(min: Point, max: Point) = location.coordinates.point.inside(min, max)
   fun getImage(maxSize: Int): BufferedImage {
      val full = ImageIO.read(file)
      val fullW = full.width
      val fullH = full.height
      if ((fullW <= maxSize) && (fullH <= maxSize)) return full else {
         val targetWidth = if (fullH <= fullW) maxSize else maxSize * fullW / fullH
         val targetHeight = if (fullW <= fullH) maxSize else maxSize * fullH / fullW
         val resizedImage = BufferedImage(targetWidth, targetHeight, full.type)
         resizedImage.createGraphics().drawImage(full, 0, 0, targetWidth, targetHeight, null)
         return resizedImage
      }
   }
}

private val nextPhotoId = AtomicLong()
fun getNextPhotoId() = nextPhotoId.andIncrement

fun createPhoto(imageFile: File, auxData: KProperties): Photo {
   val moment = Moment(auxData.getDate(PROPERTIES_DATE), null)
   val address = Address(auxData.getString(PROPERTIES_STREET), auxData.getInt(PROPERTIES_DISTRICT), PARIS)
   val point = Point(auxData.getLong(PROPERTIES_LATITUDE), auxData.getLong(PROPERTIES_LONGITUDE))
   val coordinates = Coordinates(point, auxData.getEnum(PROPERTIES_COORDINATES_SOURCE))
   val location = Location(address, coordinates)
   return Photo(getNextPhotoId(), moment, location, imageFile)
}

fun buildProperties(imageFile: File, auxFile: File, districtDir: File, streetDir: File, dateDir: File): KProperties {
   fun districtFromDirName(name: String): Int {
      val m = Pattern.compile("(\\d*)er?").matcher(name)
      if (!m.matches()) throw IllegalArgumentException("district dir $name must be *e(r)")
      else return m.group(1).toInt()
   }

   fun streetFromDirName(name: String): String {
      return name.replace('_', ' ')
   }

   fun dateFromDirName(name: String): LocalDate {
      val m = Pattern.compile("(\\d*)_(\\d*)_(\\d*)").matcher(name)
      if (!m.matches()) throw IllegalArgumentException("date dir $name must be yyyy_mm_dd")
      else return LocalDate.of(m.group(1).toInt(), m.group(2).toInt(), m.group(3).toInt())
   }

   val district = districtFromDirName(districtDir.name)
   val street = streetFromDirName(streetDir.name)
   val date = dateFromDirName(dateDir.name)
   val res = KProperties(auxFile)
   val voie = Voies.search(street)
   res.setInt(PROPERTIES_DISTRICT, district)
   res.setString(PROPERTIES_STREET, voie.name)
   res.setDate(PROPERTIES_DATE, date)
   res.setLong(PROPERTIES_LONGITUDE, voie.point.longitude)
   res.setLong(PROPERTIES_LATITUDE, voie.point.latitude)
   res.setEnum(PROPERTIES_COORDINATES_SOURCE, CoordinatesSource.Street)
   println(res.toString())
   return res
}