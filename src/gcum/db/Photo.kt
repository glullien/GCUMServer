package gcum.db

import gcum.conf.KProperties
import gcum.geo.Point
import gcum.opendata.Voie
import gcum.opendata.Voies
import gcum.utils.getLogger
import org.apache.sanselan.Sanselan
import org.apache.sanselan.common.IImageMetadata
import org.apache.sanselan.formats.jpeg.JpegImageMetadata
import org.apache.sanselan.formats.tiff.constants.ExifTagConstants
import java.awt.Dimension
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import javax.imageio.ImageIO

private val PROPERTIES_ID = "id"
private val PROPERTIES_DISTRICT = "district"
private val PROPERTIES_STREET = "street"
private val PROPERTIES_DATE = "date"
private val PROPERTIES_TIME = "time"
private val PROPERTIES_USERNAME = "username"
private val PROPERTIES_LIKES = "likes"
private val PROPERTIES_LATITUDE = "latitude"
private val PROPERTIES_LONGITUDE = "longitude"
private val PROPERTIES_COORDINATES_SOURCE = "coordinates.source"
private val PROPERTIES_WIDTH = "width"
private val PROPERTIES_HEIGHT = "height"
private val PARIS = "Paris"

private val log = getLogger()

enum class CoordinatesSource {Street, Device }
data class Moment(val date: LocalDate, val time: LocalTime?) : Comparable<Moment> {
   override fun compareTo(other: Moment): Int {
      val dateCompareTo = date.compareTo(other.date)
      return if (dateCompareTo != 0) dateCompareTo
      else if ((time == null) && (other.time == null)) 0
      else if (time == null) -1
      else if (other.time == null) 1
      else time.compareTo(other.time)
   }
}

data class Address(val street: String, val district: Int, val city: String) {
   val text: String get() = street + ", dans le " + district + (if (district == 1) "er" else "e")
}

data class Coordinates(val point: Point, val source: CoordinatesSource)
data class Location(val address: Address, val coordinates: Coordinates)
data class Details(val width: Int, val height: Int)

fun coordinateToLong(d: Double) = (d * 1E5).toLong()

fun BufferedImage.rotate(angle: Double): BufferedImage {
   val destImg = when (angle.toInt()) {
      0, 180->BufferedImage(width, height, type)
      90, 270->BufferedImage(height, width, type)
      else->throw Exception("Illegal value")
   }
   val g = destImg.createGraphics()
   val at = AffineTransform()
   at.translate(destImg.width / 2.0, destImg.height / 2.0)
   at.rotate(Math.toRadians(angle))
   at.translate(-width / 2.0, -height / 2.0)
   g.drawImage(this, at, null)
   return destImg
}

fun readImage(file: File, metaData: MetaData? = getMetaData(file)) = readImage(ImageIO.read(file), metaData)
fun readImage(bytes: ByteArray, metaData: MetaData? = getMetaData(bytes)) = bytes.inputStream().use {readImage(ImageIO.read(it), metaData)}
private fun readImage(brut: BufferedImage, metaData: MetaData?) = when (metaData?.orientation) {
   ExifTagConstants.ORIENTATION_VALUE_ROTATE_90_CW->brut.rotate(90.0)
   ExifTagConstants.ORIENTATION_VALUE_ROTATE_270_CW->brut.rotate(270.0)
   ExifTagConstants.ORIENTATION_VALUE_ROTATE_180->brut.rotate(180.0)
   else->brut
}

private fun Dimension.isSmaller(o: Dimension) = (width <= o.width) && (height <= o.height)
private fun Dimension.ratio() = width * 1.0 / height
private fun Dimension.targetIn(o: Dimension): Dimension {
   val ratio = ratio()
   return if (ratio < o.ratio()) Dimension((o.height * ratio).toInt(), o.height) else Dimension(o.width, (o.width / ratio).toInt())
}

private fun BufferedImage.resize(d: Dimension): BufferedImage {
   val res = BufferedImage(d.width, d.height, type)
   res.createGraphics().drawImage(this, 0, 0, d.width, d.height, null)
   return res
}

data class Photo(val id: String, val moment: Moment, val location: Location, val details: Details, val username: String?, val likes: Set<String>, val file: File) {

   fun getOriginalBytes() = file.readBytes()

   fun getBytes(maxWidth: Int, maxHeight: Int = maxWidth): ByteArray {
      val max = Dimension(maxWidth, maxHeight)
      val full = Dimension(details.width, details.height)
      val target = if (full.isSmaller(max)) full else full.targetIn(max)
      val sep = File.separator
      val resizedFile = File("${file.parent}${sep}aux$sep${file.nameWithoutExtension}-${target.width}-${target.height}.JPG")
      if (!resizedFile.exists()) {
         val reOrientedImage = readImage(file)
         val resizedImage = if (full == target) reOrientedImage else reOrientedImage.resize(target)
         FileOutputStream(resizedFile).use {ImageIO.write(resizedImage, "JPG", it)}
      }
      return resizedFile.readBytes()
   }

   fun saveProperties(auxFile: File) {
      val res = KProperties(auxFile)
      res.setString(PROPERTIES_ID, id)
      res.setInt(PROPERTIES_DISTRICT, location.address.district)
      res.setString(PROPERTIES_STREET, location.address.street)
      res.setDate(PROPERTIES_DATE, moment.date)
      if (moment.time != null) res.setTime(PROPERTIES_TIME, moment.time)
      res.setLong(PROPERTIES_LONGITUDE, location.coordinates.point.longitude)
      res.setLong(PROPERTIES_LATITUDE, location.coordinates.point.latitude)
      res.setEnum(PROPERTIES_COORDINATES_SOURCE, location.coordinates.source)
      res.setInt(PROPERTIES_WIDTH, details.width)
      res.setInt(PROPERTIES_HEIGHT, details.height)
      if (username != null) res.setString(PROPERTIES_USERNAME, username)
      res.setString(PROPERTIES_LIKES, likes.joinToString(","))
      res.save()
   }

}

fun createPhoto(imageFile: File, auxData: KProperties): Photo {
   val id = auxData.getString(PROPERTIES_ID)
   val moment = Moment(auxData.getDate(PROPERTIES_DATE), auxData.getTimeOrNull(PROPERTIES_TIME))
   val address = Address(auxData.getString(PROPERTIES_STREET), auxData.getInt(PROPERTIES_DISTRICT), PARIS)
   val point = Point(auxData.getLong(PROPERTIES_LATITUDE), auxData.getLong(PROPERTIES_LONGITUDE))
   val coordinates = Coordinates(point, auxData.getEnum(PROPERTIES_COORDINATES_SOURCE))
   val location = Location(address, coordinates)
   val details = Details(auxData.getInt(PROPERTIES_WIDTH), auxData.getInt(PROPERTIES_HEIGHT))
   val username = auxData.getStringOrNull(PROPERTIES_USERNAME)
   val likes = auxData.getStringOrNull(PROPERTIES_LIKES)?.split(",")?.filterNot {it.isEmpty()}?.toSet() ?: emptySet()
   return Photo(id, moment, location, details, username, likes, imageFile)
}

fun buildProperties(id: String, imageFile: File, auxFile: File, districtDir: File, streetDir: File, dateDir: File): KProperties {
   fun districtFromDirName(name: String): Int {
      val m = Pattern.compile("(\\d*)er?").matcher(name)
      if (!m.matches()) throw IllegalArgumentException("district dir $name must be *e(r)")
      else return m.group(1).toInt()
   }

   fun streetFromDirName(name: String) = name.replace('_', ' ')

   fun dateFromDirName(name: String): LocalDate {
      val m = Pattern.compile("(\\d*)_(\\d*)_(\\d*)").matcher(name)
      if (!m.matches()) throw IllegalArgumentException("date dir $name must be yyyy_mm_dd")
      else return LocalDate.of(m.group(1).toInt(), m.group(2).toInt(), m.group(3).toInt())
   }

   val district = districtFromDirName(districtDir.name)
   val street = streetFromDirName(streetDir.name)
   val date = dateFromDirName(dateDir.name)
   val voie = Voies.searchBest(street)
   return buildProperties(id, imageFile, auxFile, district, voie, date, null, null)
}

fun buildProperties(id: String, imageFile: File, auxFile: File, district: Int, voie: Voie, date: LocalDate, point: Point?, username: String?): KProperties {
   val res = KProperties(auxFile)
   val metaData = getMetaData(imageFile)
   val dateTimeFromMetaData = metaData?.originalDateTime
   val full = readImage(imageFile, metaData)
   res.setString(PROPERTIES_ID, id)
   res.setInt(PROPERTIES_DISTRICT, district)
   res.setString(PROPERTIES_STREET, voie.name)
   res.setDate(PROPERTIES_DATE, date)
   if (dateTimeFromMetaData != null) {
      if (dateTimeFromMetaData.toLocalDate() == date) res.setTime(PROPERTIES_TIME, dateTimeFromMetaData.toLocalTime())
      else log.severe("Incoherent date=$date and metaData=$dateTimeFromMetaData")
   }

   if (point != null) {
      res.setLong(PROPERTIES_LONGITUDE, point.longitude)
      res.setLong(PROPERTIES_LATITUDE, point.latitude)
      res.setEnum(PROPERTIES_COORDINATES_SOURCE, CoordinatesSource.Device)
   } else {
      val deviceLocation = metaData?.location
      if (deviceLocation != null) {
         res.setLong(PROPERTIES_LONGITUDE, deviceLocation.longitude)
         res.setLong(PROPERTIES_LATITUDE, deviceLocation.latitude)
         res.setEnum(PROPERTIES_COORDINATES_SOURCE, CoordinatesSource.Device)
      } else {
         res.setLong(PROPERTIES_LONGITUDE, voie.point.longitude)
         res.setLong(PROPERTIES_LATITUDE, voie.point.latitude)
         res.setEnum(PROPERTIES_COORDINATES_SOURCE, CoordinatesSource.Street)
      }
   }

   res.setInt(PROPERTIES_WIDTH, full.width)
   res.setInt(PROPERTIES_HEIGHT, full.height)
   if (username != null) res.setString(PROPERTIES_USERNAME, username)
   res.save()
   return res
}

data class MetaData(val orientation: Int?, val originalDateTime: LocalDateTime?, val location: Point?)

private val exitDateTimeFormat = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")

fun getMetaData(file: File) = getMetaData({Sanselan.getMetadata(file)})
fun getMetaData(bytes: ByteArray) = getMetaData({Sanselan.getMetadata(bytes)})
private fun getMetaData(sanselanGetMetaData: () -> IImageMetadata?): MetaData? {
   try {
      val metadata = sanselanGetMetaData()
      if (metadata !is JpegImageMetadata) return null else {
         // val device = metadata.findEXIFValue(ExifTagConstants.EXIF_TAG_MODEL)?.stringValue

         val dateField = metadata.findEXIFValue(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL)
         val dateTime = try {
            if (dateField == null) null else exitDateTimeFormat.parse(dateField.value.toString().trim {!it.isDigit()}, LocalDateTime::from)
         } catch (e: Exception) {
            println("Cannot parse date field ${dateField?.value}")
            null
         }

         val gps = metadata.exif.gps
         val point = if (gps == null) null else Point(coordinateToLong(gps.latitudeAsDegreesNorth), coordinateToLong(gps.longitudeAsDegreesEast))
         val orientation = metadata.findEXIFValue(ExifTagConstants.EXIF_TAG_ORIENTATION)?.intValue
         return MetaData(orientation, dateTime, point)
      }
   } catch (e: Exception) {
      e.printStackTrace()
      return null
   }
}
