package gcum.db

import gcum.chars.toStdChars
import gcum.conf.Configuration
import gcum.conf.KProperties
import gcum.geo.Point
import gcum.opendata.*
import gcum.utils.*
import java.io.File
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class UserExistsException : Exception("User exists")
class UserDoesNotExistException(username: String) : Exception("User $username does not exist")
class PhotoNotFoundException(id: String) : Exception("Photo not found $id")

object Database {

   val root: Path = Configuration.getPath("root")
   private val users = ConcurrentHashMap<String, User>()
   private val usersLock = ReentrantLock()
   private val autoLogin = ConcurrentHashMap<String, AutoLogin>()
   private val autoLoginLock = ReentrantLock()
   private val photos = ConcurrentHashMap<String, Photo>()
   private val points = ConcurrentHashMap<Point, ConcurrentLinkedQueue<String>>()
   private val photosLock = ReentrantLock()
   private val usersFileName = "users.csv"
   private val autoLoginFileName = "autoLogin.csv"
   private val auxDirName = "aux"
   private val nextPhotoId = SecretCode({code-> photos.contains(code)})

   init {
      val usersFile = root.resolve(usersFileName).toFile()
      if (usersFile.exists()) {
         val usersFileBrut = usersFile.inputStream().use(::readCsv)
         users.putAll(usersFileBrut.map {
            line->
            User(line [0], line[1], if (line[2].isEmpty()) null else line[2], UserRole.valueOf(line[3]))
         }.associateBy {it.username})
      }
      val autoLoginFile = root.resolve(autoLoginFileName).toFile()
      if (autoLoginFile.exists()) {
         val autoLoginFileBrut = autoLoginFile.inputStream().use(::readCsv)
         autoLogin.putAll(autoLoginFileBrut.map {
            line->
            AutoLogin(line [0], line[1], LocalDate.parse(line[2], DateTimeFormatter.ISO_LOCAL_DATE))
         }.associateBy {it.code})
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

   private fun writeUsersFile() = root.resolve(usersFileName).toFile().printWriter().use {
      writeCsv(it, users.values.map {listOf(it.username, it.password, it.email, it.role.toString())})
   }

   private fun writeAutoLoginFile() = root.resolve(autoLoginFileName).toFile().printWriter().use {
      writeCsv(it, autoLogin.values.map {listOf(it.username, it.code, it.validTo.format(DateTimeFormatter.ISO_LOCAL_DATE))})
   }

   fun addUser(username: String, password: String, email: String?) {
      usersLock.withLock {
         if (users.containsKey(username)) throw UserExistsException()
         users.put(username, User(username, password, if (email.isNullOrEmpty()) null else email))
         writeUsersFile()
      }
   }

   fun changeEmail(username: String, email: String?) {
      usersLock.withLock {
         val user = users[username] ?: throw UserDoesNotExistException(username)
         users.put(username, User(username, user.password, email, user.role))
         writeUsersFile()
      }
   }

   fun changePassword(username: String, password: String) {
      usersLock.withLock {
         val user = users[username] ?: throw UserDoesNotExistException(username)
         users.put(username, User(username, password, user.email, user.role))
         writeUsersFile()
      }
   }

   private val autoLoginCode = SecretCode({code-> autoLogin.containsKey(code)})

   fun generateAutoLoginCode(username: String): AutoLogin {
      autoLoginLock.withLock {
         if (!users.containsKey(username)) throw UserDoesNotExistException(username)
         val code = autoLoginCode.new()
         val res = AutoLogin(username, code, LocalDate.now().plusYears(1))
         autoLogin[code] = res
         writeAutoLoginFile()
         return res
      }
   }

   fun getAutoLogin(code: String) = autoLogin[code]

   fun removeAutoLoginCode(code: String) {
      autoLoginLock.withLock {
         autoLogin.remove(code)
         writeAutoLoginFile()
      }
   }

   fun getUser(username: String): User? = users[username]
   fun getUserFromEmail(email: String): User? = users.values.firstOrNull {it.email == email}

   private fun add(photo: Photo) {
      if (photos.values.none {it.file.absolutePath == photo.file.absolutePath}) {
         photos[photo.id] = photo
         points.computeIfAbsent(photo.location.coordinates.point, {ConcurrentLinkedQueue()}).add(photo.id)
      }
   }

   private fun add(districtDir: File, streetDir: File, dateDir: File, auxDir: File, imageFile: File) {
      val auxFile = File(auxDir, imageFile.nameWithoutExtension + ".properties")
      val auxData = if (auxFile.exists()) KProperties(auxFile) else buildProperties(nextPhotoId.new(), imageFile, auxFile, districtDir, streetDir, dateDir)
      add(createPhoto(imageFile, auxData))
   }

   val allPhotos: Collection<Photo> get () = photos.values
   val allPoints: Map<Point, List<Photo>> get () = points.map {e-> e.key to e.value.map {photos[it] ?: throw Exception("Code error")}}.toMap()
   fun getPoints(username: List<String>?): Map<Point, List<Photo>> {
      val allPoints = allPoints
      return if (username == null) allPoints else allPoints.filterValues {it.any {username.contains(it.username)}}
   }

   //fun getPhotos(min: Point, max: Point) = photos.filterValues {it.inside(min, max)}
   //fun getPoints(min: Point, max: Point) = points.filterKeys {it.inside(min, max)}.keys
   fun getPhoto(id: String) = photos[id]

   private val gcumCode = SecretCode({code-> photos.values.any {it.file.name.contains(code)}}, 10)

   fun put(street: String, date: LocalDate, time: LocalTime?, district: Int, point: Point?, username: String, images: List<ByteArray>) {
      fun String.replaceSpecialChars() = toStdChars().replace(' ', '_').replace('/', '_').replace('.', '_')
      fun String.firstCharToLowerCase() = substring(0, 1).toLowerCase() + substring(1)
      val streetDir = street.replaceSpecialChars().firstCharToLowerCase()
      val districtDir = "$district" + if (district == 1) "er" else "e"
      val dateDir = date.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))
      val path = root.resolve(districtDir).resolve(streetDir).resolve(dateDir)
      val aux = path.resolve(auxDirName)
      aux.toFile().mkdirs()
      for (image in images) photosLock.withLock {
         val fileName = "GCUM${gcumCode.new()}.JPG"
         val imageFile = path.resolve(fileName).toFile()
         val auxFile = aux.resolve(imageFile.nameWithoutExtension + ".properties").toFile()
         imageFile.writeBytes(image)
         val voie = Voies.get(street) ?: throw IllegalArgumentException("Street $street does not exist")
         val auxData = buildProperties(nextPhotoId.new(), imageFile, auxFile, district, voie, date, time, point, username)
         val photo = createPhoto(imageFile, auxData)
         add(photo)
         tweet(photo)
      }
   }

   fun toggleLike(photoId: String, username: String) {
      val photo = getPhoto(photoId) ?: throw PhotoNotFoundException(photoId)
      val auxDir = File(photo.file.parent, auxDirName)
      val auxFile = File(auxDir, photo.file.nameWithoutExtension + ".properties")
      val newLikes = if (photo.likes.contains(username)) photo.likes.minus(username) else photo.likes.plus(username)
      val newPhoto = Photo(photo.id, photo.moment, photo.location, photo.details, photo.username, newLikes, photo.file)
      photos[photoId] = newPhoto
      newPhoto.saveProperties(auxFile)
   }

   fun getPhotos(number: Int, district: Int?, start: PhotosListStart): PhotosList {
      val filtered = photosLock.withLock {if (district == null) photos.values.toList() else photos.values.filter {it.location.address.district == district}}
      val sorted = filtered.sortedBy {it.moment}.reversed()
      val fromIndex = (if (start.id == null) 0 else sorted.indexOfFirst {it.id == start.id}.max(0)) + start.offset
      val toIndex = sorted.size.min(fromIndex + number)
      val list = sorted.subList(fromIndex, toIndex)
      return PhotosList(list, sorted.size - toIndex)
   }
}

data class PhotosList(val list: List<Photo>, val nbAfter: Int)
data class PhotosListStart(val id: String?, val offset: Int)

val firstPhoto = PhotosListStart(null, 0)

enum class UserRole {Regular, Admin }
data class User(val username: String, val password: String, val email: String?, val role: UserRole = UserRole.Regular)
data class AutoLogin(val username: String, val code: String, val validTo: LocalDate) {
   fun isValid() = !validTo.isBefore(LocalDate.now())
}

fun main(args: Array<String>) {
   println("by Name ${Voies.searchBest("rue conte").name}")
   println("by Point ${Voies.searchClosest(Point(4883377, 238200)).name}")
   println("by Point ${Voies.searchClosest(Point(4883377, 238200), 10).map {it.name}}")
   println("by Point ${Voies.searchClosest2(Point(4883377, 238200), 10).map {it.name}}")
   println("by Name ${VoiesArrondissements.districts(Voies.searchBest("Renoir"))}")
   println("by Name ${Arrondissements.arrondissements.size}")
   println("by Name ${Arrondissements.search(Point(4887202, 235788))}")
   println("CHECK ALL ${Voies.voies.filter {VoiesArrondissements.districtsOrNull(it) == null}.map {it.name}.size}")
   println("CHECK ALL ${Voies.voies.filter {VoiesArrondissements.districtsOrNull(it) == null}.map {it.name}}")
   time("Database search 1 closest") {for (i in 1..500) Voies.searchClosest(Point(4883377, 238200))}
   time("Database search closest") {for (i in 1..50) Voies.searchClosest(Point(4883377, 238200), 10)}
   time("Database search closest2") {for (i in 1..500) Voies.searchClosest2(Point(4883377, 238200), 10)}
   time("Database loading") {Database.getUser("paf")}
}