package gcum.db

import gcum.chars.toStdChars
import gcum.conf.Configuration
import gcum.conf.KProperties
import gcum.geo.Point
import gcum.opendata.Arrondissements
import gcum.opendata.Voies
import gcum.opendata.VoiesArrondissements
import gcum.utils.time
import java.io.File
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

object Database {

   val root: Path = Configuration.getPath("root")
   val photos = ConcurrentLinkedQueue<Photo>()
   val points = ConcurrentHashMap<Point, ConcurrentLinkedQueue<Photo>>()
   val auxDirName = "aux"

   //val addDicoveredPhotoExecPool = Executors.newSingleThreadExecutor()

   init {
      fun File.isImageFile() = name.toLowerCase().matches(Regex(".*\\.(jpg|jpeg)"))
      root.toFile().listFiles {file: File-> file.isDirectory}.forEach {
         districtDir->
         districtDir.listFiles {file: File-> file.isDirectory}.forEach {
            streetDir->
            streetDir.listFiles {file: File-> file.isDirectory}.forEach {
               dateDir->
               val auxDir = File(dateDir, auxDirName)
               if (!auxDir.exists()) auxDir.mkdir()
               dateDir.listFiles(File::isImageFile).forEach {
                  imageFile->
                  //addDicoveredPhotoExecPool.execute {
                  add(districtDir, streetDir, dateDir, auxDir, imageFile)
                  //}
               }
            }
         }
      }
   }

   fun shutdown() {
      //addDicoveredPhotoExecPool.shutdown()
      //addDicoveredPhotoExecPool.awaitTermination(1, TimeUnit.HOURS)
   }

   private fun add(photo: Photo) {
      photos.add(photo)
      points.computeIfAbsent(photo.location.coordinates.point, {p-> ConcurrentLinkedQueue()}).add(photo)
   }

   private fun add(districtDir: File, streetDir: File, dateDir: File, auxDir: File, imageFile: File) {
      val auxFile = File(auxDir, imageFile.nameWithoutExtension + ".properties")
      val auxData = if (auxFile.exists()) KProperties(auxFile) else buildProperties(imageFile, auxFile, districtDir, streetDir, dateDir)
      add(createPhoto(imageFile, auxData))
   }

   fun getPhotos(min: Point, max: Point) = photos.filter {it.inside(min, max)}
   fun getPoints(min: Point, max: Point) = points.filterKeys {it.inside(min, max)}.keys
   fun getPhoto(id: Long) = photos.find {it.id == id}

   private val random = Random()
   fun put(street: String, date: LocalDate, district: Int, files: List<File>) {
      fun imageFileName(dir: Path): String {
         val fileName = "GCUM${random.nextInt(100000)}.JPG"
         return if (!dir.resolve(fileName).toFile().exists()) fileName else imageFileName(dir)
      }

      fun String.replaceSpecialChars() = toStdChars().replace(' ', '_').replace('/', '_')
      fun String.firstCharToLowerCase() = substring(0, 1).toLowerCase() + substring(1)
      val streetDir = street.replaceSpecialChars().firstCharToLowerCase();
      val districtDir = "$district" + if (district == 1) "er" else "e"
      val dateDir = date.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))
      val path = root.resolve(districtDir).resolve(streetDir).resolve(dateDir)
      val aux = path.resolve(auxDirName)
      aux.toFile().mkdirs()
      for (file in files) {
         val fileName = imageFileName(path)
         val imageFile = path.resolve(fileName).toFile()
         val auxFile = aux.resolve(imageFile.nameWithoutExtension + ".properties").toFile()
         file.inputStream().use {i-> imageFile.outputStream().use {o-> i.copyTo(o)}}
         val voie = Voies.get(street) ?: throw IllegalArgumentException("Street $street does not exist")
         val auxData = buildProperties(imageFile, auxFile, district, voie, date)
         add(createPhoto(imageFile, auxData))
      }
   }
}

fun main(args: Array<String>) {
   println("by Name ${Voies.search("rue conte").name}")
   println("by Point ${Voies.search(Point(4883377, 238200)).name}")
   println("by Point ${Voies.search(Point(4887202, 235788)).name}")
   println("by Name ${VoiesArrondissements.search("Jemmapes")}")
   println("by Name ${Arrondissements.arrondissements.size}")
   println("by Name ${Arrondissements.search(Point(4887202, 235788))}")
   time("Database loading") {Database.shutdown()}
}