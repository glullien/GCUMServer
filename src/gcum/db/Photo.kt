package gcum.db

import gcum.conf.KProperties
import gcum.geo.Point
import gcum.opendata.Voies
import gcum.utils.getLogger
import org.apache.sanselan.Sanselan
import org.apache.sanselan.formats.jpeg.JpegImageMetadata
import org.apache.sanselan.formats.tiff.constants.ExifTagConstants
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong
import java.util.regex.Pattern
import javax.imageio.ImageIO

private val PROPERTIES_DISTRICT = "district"
private val PROPERTIES_STREET = "street"
private val PROPERTIES_DATE = "date"
private val PROPERTIES_TIME = "time"
private val PROPERTIES_LATITUDE = "latitude"
private val PROPERTIES_LONGITUDE = "longitude"
private val PROPERTIES_COORDINATES_SOURCE = "coordinates.source"
private val PROPERTIES_WIDTH = "width"
private val PROPERTIES_HEIGHT = "height"
private val PARIS = "Paris"

private val log = getLogger()

enum class CoordinatesSource {Street, Device }
data class Moment(val date: LocalDate, val time: LocalTime?)
data class Address(val street: String, val district: Int, val city: String)
data class Coordinates(val point: Point, val source: CoordinatesSource)
data class Location(val address: Address, val coordinates: Coordinates)
data class Details(val width: Int, val height: Int)

private fun BufferedImage.rotate(angle: Double): BufferedImage {
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

private fun readImage(file: File, metaData: MetaData? = getMetaData(file)): BufferedImage {
   val brut = ImageIO.read(file)
   return when (metaData?.orientation) {
      ExifTagConstants.ORIENTATION_VALUE_ROTATE_90_CW->brut.rotate(90.0)
      ExifTagConstants.ORIENTATION_VALUE_ROTATE_270_CW->brut.rotate(270.0)
      ExifTagConstants.ORIENTATION_VALUE_ROTATE_180->brut.rotate(180.0)
      else->brut
   }

}

data class Photo(val id: Long, val moment: Moment, val location: Location, val details: Details, val file: File) {
   fun inside(min: Point, max: Point) = location.coordinates.point.inside(min, max)

   fun writeImage(out: OutputStream, maxSize: Int) {
      val fullW = details.width
      val fullH = details.height
      if ((fullW <= maxSize) && (fullH <= maxSize)) FileInputStream(file).use {it.copyTo(out)}
      else {
         val targetWidth = if (fullH <= fullW) maxSize else maxSize * fullW / fullH
         val targetHeight = if (fullW <= fullH) maxSize else maxSize * fullH / fullW
         val sep = File.separator
         val resizedFileName = File("${file.parent}${sep}aux$sep${file.nameWithoutExtension}-$targetWidth-$targetHeight.JPG")
         if (!resizedFileName.exists()) {
            val full = readImage(file)
            val resizedImage = BufferedImage(targetWidth, targetHeight, full.type)
            resizedImage.createGraphics().drawImage(full, 0, 0, targetWidth, targetHeight, null)
            FileOutputStream(resizedFileName).use {ImageIO.write(resizedImage, "JPG", it)}
         }
         FileInputStream(resizedFileName).use {it.copyTo(out)}
      }
   }

}

private val nextPhotoId = AtomicLong()
fun getNextPhotoId() = nextPhotoId.andIncrement

fun createPhoto(imageFile: File, auxData: KProperties): Photo {
   val moment = Moment(auxData.getDate(PROPERTIES_DATE), auxData.getTimeOrNull(PROPERTIES_TIME))
   val address = Address(auxData.getString(PROPERTIES_STREET), auxData.getInt(PROPERTIES_DISTRICT), PARIS)
   val point = Point(auxData.getLong(PROPERTIES_LATITUDE), auxData.getLong(PROPERTIES_LONGITUDE))
   val coordinates = Coordinates(point, auxData.getEnum(PROPERTIES_COORDINATES_SOURCE))
   val location = Location(address, coordinates)
   val details = Details(auxData.getInt(PROPERTIES_WIDTH), auxData.getInt(PROPERTIES_HEIGHT))
   return Photo(getNextPhotoId(), moment, location, details, imageFile)
}

fun buildProperties(imageFile: File, auxFile: File, districtDir: File, streetDir: File, dateDir: File): KProperties {
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
   val res = KProperties(auxFile)
   val voie = Voies.search(street)
   val metaData = getMetaData(imageFile)
   val dateTimeFromMetaData = metaData?.originalDateTime
   val full = readImage(imageFile, metaData)
   res.setInt(PROPERTIES_DISTRICT, district)
   res.setString(PROPERTIES_STREET, voie.name)
   res.setDate(PROPERTIES_DATE, date)
   if (dateTimeFromMetaData != null) {
      if (dateTimeFromMetaData.toLocalDate() == date) res.setTime(PROPERTIES_TIME, dateTimeFromMetaData.toLocalTime())
      else log.severe("Incoherent date=$date and metaData=$dateTimeFromMetaData")
   }

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

   res.setInt(PROPERTIES_WIDTH, full.width)
   res.setInt(PROPERTIES_HEIGHT, full.height)
   res.save()
   return res
}

private data class MetaData(val orientation: Int?, val originalDateTime: LocalDateTime?, val location: Point?)

private val exitDateTimeFormat = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")

private fun getMetaData(file: File): MetaData? {
   try {
      val metadata = Sanselan.getMetadata(file)
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
         val point = if (gps == null) null else Point((gps.latitudeAsDegreesNorth * 1E10).toLong(), (gps.longitudeAsDegreesEast * 1E10).toLong())
         /* val latitude = metadata.findEXIFValue(GPSTagConstants.GPS_TAG_GPS_LATITUDE)
          val latitudeRef = metadata.findEXIFValue(GPSTagConstants.GPS_TAG_GPS_LATITUDE_REF)
          val longitude = metadata.findEXIFValue(GPSTagConstants.GPS_TAG_GPS_LONGITUDE)
          val longitudeRef = metadata.findEXIFValue(GPSTagConstants.GPS_TAG_GPS_LONGITUDE_REF)

          val point = if (latitude == null || latitudeRef == null || longitude == null || longitudeRef == null) null else {
             fun ref(o: Any) = o.toString().trim {!it.isLetterOrDigit()}
             fun degree(o: Any): Long {
                fun d(o: List<RationalNumber>, f: Double): Double = if (o.isEmpty()) 0.0 else ((f * o[0].numerator) / o[0].divisor) + d(o.drop(1), f / 60)
                return if (o is Array<*>) {
                   if (o[0] is RationalNumber) (d((o as Array<RationalNumber>).toList(), 1.0) * 1E10).toLong()
                   else{
                      throw IllegalArgumentException("o[bytes= ${o as Array<Byte>}")
                   }
                } else {
                   throw IllegalArgumentException("o = $o")
                }
             }

             val latitudeDegree = degree(latitude.value) * if (ref(latitudeRef.value) == "N") 1 else -1
             val longitudeDegree = degree(longitude.value) * if (ref(longitudeRef.value) == "E") 1 else -1
             Point(latitudeDegree, longitudeDegree)
          }                       */
         val orientation = metadata.findEXIFValue(ExifTagConstants.EXIF_TAG_ORIENTATION)?.intValue
         return MetaData(orientation, dateTime, point)
      }
   } catch (e: Exception) {
      e.printStackTrace()
      return null
   }
}
