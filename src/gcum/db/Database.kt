package gcum.db

import gcum.chars.toStdChars
import gcum.conf.Configuration
import gcum.conf.KProperties
import gcum.geo.Point
import gcum.opendata.*
import gcum.utils.time
import java.io.File
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class UserExistsException : Exception("User exists")

object Database {

   val root: Path = Configuration.getPath("root")
   val users = ConcurrentHashMap<String, User>()
   val usersLock = ReentrantLock()
   val photos = ConcurrentLinkedQueue<Photo>()
   val points = ConcurrentHashMap<Point, ConcurrentLinkedQueue<Photo>>()
   val photosLock = ReentrantLock()
   val usersFileName = "users.csv"
   val auxDirName = "aux"

   init {
      val usersFile = root.resolve(usersFileName).toFile()
      if (usersFile.exists()) {
         val usersFileBrut = usersFile.inputStream().use(::readCsv)
         users.putAll(usersFileBrut.map {line-> User(line [0], line[1], if (line[2].isEmpty()) null else line[2], UserRole.valueOf(line[3]))}.associateBy {it.username})
      }
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
                  add(districtDir, streetDir, dateDir, auxDir, imageFile)
               }
            }
         }
      }
   }

   fun addUser(username: String, password: String, email: String?) {
      usersLock.withLock {
         if (users.contains(username)) throw UserExistsException()
         users.put(username, User(username, password, if (email.isNullOrEmpty()) null else email, UserRole.Regular))
         root.resolve(usersFileName).toFile().printWriter().use {
            writeCsv(it, users.values.map {listOf(it.username, it.password, it.email, it.role.toString())})
         }
      }
   }

   fun getUser(username: String): User? = users[username]

   private fun add(photo: Photo) {
      photosLock.withLock {
         if (photos.none {it.file.absolutePath == photo.file.absolutePath}) {
            photos.add(photo)
            points.computeIfAbsent(photo.location.coordinates.point, {p-> ConcurrentLinkedQueue()}).add(photo)
         }
      }
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
   fun put(street: String, date: LocalDate, district: Int, images: List<ByteArray>) {
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
      for (image in images) {
         val fileName = imageFileName(path)
         val imageFile = path.resolve(fileName).toFile()
         val auxFile = aux.resolve(imageFile.nameWithoutExtension + ".properties").toFile()
         imageFile.writeBytes(image)
         val voie = Voies.get(street) ?: throw IllegalArgumentException("Street $street does not exist")
         val auxData = buildProperties(imageFile, auxFile, district, voie, date)
         add(createPhoto(imageFile, auxData))
      }
   }
}

enum class UserRole {Regular, Admin }
data class User(val username: String, val password: String, val email: String?, val role: UserRole)

fun main(args: Array<String>) {
   println("by Name ${Voies.search("rue conte").name}")
   println("by Point ${Voies.search(Point(4883377, 238200)).name}")
   println("by Point ${Voies.search(Point(4887202, 235788)).name}")
   println("by Name ${VoiesArrondissements.search("Jemmapes")}")
   println("by Name ${Arrondissements.arrondissements.size}")
   println("by Name ${Arrondissements.search(Point(4887202, 235788))}")
   time("Database loading") {Database.getUser("paf")}
}