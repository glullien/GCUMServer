package gcum.servlets

import gcum.db.Database
import gcum.db.MetaData
import gcum.db.getMetaData
import gcum.db.readImage
import gcum.geo.Point
import gcum.opendata.Arrondissements
import gcum.opendata.Voie
import gcum.opendata.Voies
import gcum.opendata.VoiesArrondissements
import gcum.utils.getLogger
import java.awt.image.BufferedImage
import java.io.*
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern
import javax.imageio.ImageIO
import javax.servlet.ServletException
import javax.servlet.annotation.MultipartConfig
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private val log = getLogger()

private class Post(val id: Int, val timeStamp: Instant, val uploaded: List<Uploaded>) {
   val date: LocalDate? get() {
      val dates = uploaded.map {it.metaData?.originalDateTime}.filterNotNull().map {it.toLocalDate()}.toSet()
      return if (dates.size == 1) dates.first() else null
   }
   val voie: Voie? get() {
      val voies = uploaded.map {it.voie}.filterNotNull().toSet()
      return if (voies.size == 1) voies.first() else null
   }
   val district: Int? get() {
      val districts = uploaded.map {it.district}.filterNotNull().toSet()
      return if (districts.size == 1) districts.first() else null
   }
}

private class Uploaded(val id: Int, val file: File, val width: Int, val height: Int, val voie: Voie?, val district: Int?, val metaData: MetaData?) {
   fun writeImage(out: OutputStream, maxSize: Int) {
      if ((width <= maxSize) && (height <= maxSize)) FileInputStream(file).use {it.copyTo(out)}
      else {
         val targetWidth = if (height <= width) maxSize else maxSize * width / height
         val targetHeight = if (width <= height) maxSize else maxSize * height / width
         val full = readImage(file, metaData)
         val resizedImage = BufferedImage(targetWidth, targetHeight, full.type)
         resizedImage.createGraphics().drawImage(full, 0, 0, targetWidth, targetHeight, null)
         ImageIO.write(resizedImage, "JPG", out)
      }
   }
}

private val nextId = AtomicInteger(0)
private val postsList = mutableMapOf<Int, Post>()
private val uploadedList = mutableMapOf<Int, Uploaded>()

private fun cleanOldUploaded() {
   val timeOut = Duration.ofHours(1)
   val toRemove = postsList.filterValues {timeOut < Duration.between(it.timeStamp, Instant.now())}
   toRemove.keys.forEach {
      postKey->
      postsList[postKey]?.uploaded?.forEach {uploadedList.remove(it.id)}
      postsList.remove(postKey)
   }
}

private fun addUpload(file: File): Uploaded {
   fun district(point: Point, street: String): Int? {
      val districtsFromPoint = Arrondissements.search(point)
      val districtsFromStreet = VoiesArrondissements.search(street).map {it.district}
      val intersect = districtsFromPoint.intersect(districtsFromStreet)
      return intersect.firstOrNull() ?: districtsFromStreet.firstOrNull() ?: districtsFromPoint.firstOrNull()
   }

   val id = nextId.andIncrement
   val metaData = getMetaData(file)
   val image = readImage(file, metaData)
   val voie = if (metaData?.location == null) null else Voies.search(metaData?.location)
   val district = if ((metaData?.location == null) || (voie == null)) null else district(metaData?.location, voie.name)
   val uploaded = Uploaded(id, file, image.width, image.height, voie, district, metaData)
   uploadedList[id] = uploaded
   return uploaded
}

private fun addPost(uploaded: List<Uploaded>): Post {
   val id = nextId.andIncrement
   val post = Post(id, Instant.now(), uploaded)
   postsList[id] = post
   return post
}

@WebServlet(name = "Upload", value = "/upload")
@MultipartConfig
class Upload : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      log.info("upload post ${request.parts.size} files")
      cleanOldUploaded()
      val uploaded = request.parts.map {
         p->
         if (p.contentType != "image/jpeg") throw ServletException("Must be an image")
         val tmpFile = File.createTempFile("uploaded", "image")
         p.inputStream.use {i-> FileOutputStream(tmpFile).use {i.copyTo(it)}}
         addUpload(tmpFile)
      }
      val post = addPost(uploaded)
      return jsonSuccess {
         put("id", post.id)
         put("date", post.date?.format(DateTimeFormatter.ISO_DATE) ?: "unknown")
         put("street", post.voie?.name ?: "unknown")
         put("district", post.district ?: -1)
         put("uploaded", uploaded.map {
            p->
            sub {
               put("id", p.id)
               put("width", p.width)
               put("height", p.height)
               put("date", p.metaData?.originalDateTime?.format(DateTimeFormatter.ISO_DATE) ?: "unknown")
               put("time", p.metaData?.originalDateTime?.format(DateTimeFormatter.ISO_TIME) ?: "unknown")
               put("location", if (p.metaData?.location == null) "unknown" else "known")
               put("latitude", p.metaData?.location?.latitude ?: 0)
               put("longitude", p.metaData?.location?.longitude ?: 0)
               put("street", p.voie?.name ?: "unknown")
               put("district", p.district ?: -1)
            }
         })
      }
   }
}

@WebServlet(name = "GetUploadedPhoto", value = "/getUploadedPhoto")
class GetUploadedPhoto : HttpServlet() {
   @Throws(ServletException::class, IOException::class)
   override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
      request.characterEncoding = "UTF-8"
      val id = request.getInt("id")
      val maxSize = request.getIntOrNull("maxSize")
      val photo = uploadedList[id] ?: throw ServletException("Photo $id not found")
      response.contentType = "image/jpeg"
      response.outputStream.use {photo.writeImage(it, maxSize ?: Int.MAX_VALUE)}
   }
}

@WebServlet(name = "ReportUploaded", value = "/reportUploaded")
class ReportUploaded : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val id = request.getInt("id")
      val post = postsList[id] ?: return jsonError("Session perdue")
      val street = request.getString("street")
      val district = request.getString("district")
      val date = request.getString("date")

      if (Voies.get(street) == null) return jsonError("Mauvais nom de voie")

      val districtMatcher = Pattern.compile("(\\d+)e?.*").matcher(district)
      if (!districtMatcher.matches()) return jsonError("Mauvais nom d'arrondissement")
      val districtInt = districtMatcher.group(1).toInt()

      val dateMatcher = Pattern.compile("(20\\d{2})-(\\d{2})-(\\d{2})").matcher(date)
      if (!dateMatcher.matches()) return jsonError("Mauvaise date")
      val localDate = LocalDate.of(dateMatcher.group(1).toInt(), dateMatcher.group(2).toInt(), dateMatcher.group(3).toInt())

      Database.put(street, localDate, districtInt, post.uploaded.map {it.file})
      return jsonSuccess {}
   }
}