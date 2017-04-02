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

private val log = getLogger()

object Database {

   val versionName = "0.9.19.2"
   val versionCode = 4

   val root: Path = Configuration.getPath("root")
   private val users = ConcurrentHashMap<String, User>()
   private val usersLock = ReentrantLock()
   private val autoLogin = ConcurrentHashMap<String, AutoLogin>()
   private val autoLoginLock = ReentrantLock()
   private val removeFromMailsCodes = ConcurrentHashMap<String, RemoveFromMail>()
   private val notifications = ConcurrentHashMap<String, Set<Notification>>()
   private val photos = ConcurrentHashMap<String, Photo>()
   private val points = ConcurrentHashMap<Point, ConcurrentLinkedQueue<String>>()
   private val photosLock = ReentrantLock()
   private val usersFileName = "users.csv"
   private val notificationsFileName = "notifications.csv"
   private val autoLoginFileName = "autoLogin.csv"
   private val removeFromMailsCodesFileName = "removeFromMailsCodes.csv"
   private val currentReleaseFileName = "currentRelease"
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
      val removeFromMailsCodesFile = root.resolve(removeFromMailsCodesFileName).toFile()
      if (removeFromMailsCodesFile.exists()) {
         val removeFromNewsCodesFileBrut = removeFromMailsCodesFile.inputStream().use(::readCsv)
         removeFromMailsCodes.putAll(removeFromNewsCodesFileBrut.map {line-> line[0] to RemoveFromMail(line[1], NotificationCause.valueOf(line[2]))})
      }
      val notificationsFile = root.resolve(notificationsFileName).toFile()
      if (notificationsFile.exists()) {
         val notificationsFileBrut = notificationsFile.inputStream().use(::readCsv)
         notifications.putAll(notificationsFileBrut.map {
            line->
            line[0] to Notification(NotificationCause.valueOf(line[1]), NotificationMedia.valueOf(line[2]))
         }.groupBy {it.first}.mapValues {it.value.map {it.second}.toSet()})
      } else {
         for (username in users.filterValues {it.email != null}.keys) {
            val allNotifications = getAllNotifications(NotificationMedia.Email)
            addNotifications(username, allNotifications)
         }
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

      mailNewReleases()
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
         if (email != null) addNotifications(username, getAllNotifications(NotificationMedia.Email))
      }
   }

   fun changeEmail(username: String, email: String?) {
      usersLock.withLock {
         val user = users[username] ?: throw UserDoesNotExistException(username)
         users.put(username, User(username, user.password, email, user.role))
         writeUsersFile()
         val allNotifications = getAllNotifications(NotificationMedia.Email)
         if (email != null) addNotifications(username, allNotifications)
         else removeNotifications(username, allNotifications)
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

   fun addNotifications(username: String, notifications: Set<Notification>) {
      val old = this.notifications[username]
      if ((old == null) || !old.containsAll(notifications)) {
         this.notifications[username] = old?.plus(notifications) ?: notifications
         writeNotificationsFiles()
      }
   }

   fun removeNotifications(username: String, notifications: Set<Notification>) {
      val old = this.notifications[username]
      if ((old != null) && old.any {notifications.contains(it)}) {
         this.notifications[username] = old.minus(notifications)
         writeNotificationsFiles()
      }
   }

   fun getNotifications(username: String, cause: NotificationCause): List<Notification> = notifications[username]?.filter {it.cause == cause} ?: emptyList()

   fun getNotified(cause: NotificationCause): Map<User, List<Notification>> = usersLock.withLock {
      notifications.mapValues {it.value.filter {it.cause == cause}}.filterValues {it.isNotEmpty()}.filterKeys {users.containsKey(it)}.mapKeys {users[it.key] ?: throw Exception("Missing ${it.key}")}
   }

   fun writeNotificationsFiles() = root.resolve(notificationsFileName).toFile().printWriter().use {
      writeCsv(it, notifications.flatMap {e-> e.value.map {listOf(e.key, it.cause.toString(), it.media.toString())}})
   }

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

   fun put(number: String?, street: String, date: LocalDate, time: LocalTime?, district: Int, point: Point?, username: String, images: List<ByteArray>) {
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
         val auxData = buildProperties(nextPhotoId.new(), imageFile, auxFile, district, voie, number, date, time, point, username)
         val photo = createPhoto(imageFile, auxData)
         add(photo)
         tweet(photo)
      }
   }

   fun toggleLike(photoId: String, username: String) {
      val photo = getPhoto(photoId) ?: throw PhotoNotFoundException(photoId)
      val auxDir = File(photo.file.parent, auxDirName)
      val auxFile = File(auxDir, photo.file.nameWithoutExtension + ".properties")
      val wasLiked = photo.likes.contains(username)
      val newLikes = if (wasLiked) photo.likes.minus(username) else photo.likes.plus(username)
      val newPhoto = Photo(photo.id, photo.moment, photo.location, photo.details, photo.username, newLikes, photo.file)
      photos[photoId] = newPhoto
      newPhoto.saveProperties(auxFile)
      if (photo.username != null) {
         getNotifications(photo.username, NotificationCause.Liked).map {it.media}.forEach {
            when (it) {
               NotificationMedia.Email-> {
                  val user = Database.getUser(photo.username)
                  if (user?.email != null) {
                     val subject = if (wasLiked) "$username n'aime plus votre photo" else "$username a aimé votre photo"
                     val body = if (wasLiked) "/gcum/db/DoesNotLike.html" else "/gcum/db/Like.html"
                     sendMail(listOf(user.email), subject, body, mapOf(
                        "username" to username,
                        "date" to photo.moment.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        "time" to (photo.moment.time?.format(DateTimeFormatter.ISO_LOCAL_TIME) ?: ""),
                        "street" to photo.location.address.street
                     ))
                  }
               }
            }
         }
      }
   }

   fun mailNewReleases() {
      val currentReleaseFile = root.resolve(currentReleaseFileName).toFile()
      val currentRelease = if (currentReleaseFile.exists()) currentReleaseFile.readText().trim().toInt() else 0
      val nextCode = SecretCode({code-> removeFromMailsCodes.containsKey(code)})
      for (r in (currentRelease + 1)..versionCode) {
         for (notificationUser in getNotified(NotificationCause.News)) for (notification in notificationUser.value) when (notification.media) {
            NotificationMedia.Email-> {
               val email = notificationUser.key.email
               if (email != null) {
                  val code = nextCode.new()
                  removeFromMailsCodes.put(code, RemoveFromMail(notificationUser.key.username, NotificationCause.News))
                  sendMail(listOf(email), "GCUM : New release", "/gcum/db/Release-$r.html", mapOf("code" to code))
               }
            }
         }
         currentReleaseFile.writeText(r.toString())
         root.resolve(removeFromMailsCodesFileName).toFile().printWriter().use {
            writeCsv(it, removeFromMailsCodes.map {listOf(it.key, it.value.username, it.value.cause.toString())})
         }
      }
   }

   fun getRemoveFromMails(code: String) = removeFromMailsCodes[code]

   fun getPhotos(number: Int, filter: ((Photo) -> Boolean)?, comparator: Comparator<Photo>, start: PhotosListStart): PhotosList {
      val filtered = photosLock.withLock {if (filter == null) photos.values.toList() else photos.values.filter(filter)}
      val sorted = filtered.sortedWith(comparator)
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

enum class NotificationCause {Liked, News }
enum class NotificationMedia {Email }
data class Notification(val cause: NotificationCause, val media: NotificationMedia)
data class RemoveFromMail(val username: String, val cause: NotificationCause)

fun getAllNotifications(media: NotificationMedia) = NotificationCause.values().map {cause-> Notification(cause, media)}.toSet()
val allNotifications = NotificationCause.values().flatMap {cause-> NotificationMedia.values().map {media-> Notification(cause, media)}}.toSet()

fun main(args: Array<String>) {
   log.info("Test de log")
   println("by Name ${Voies.searchBest("rue conte").name}")
   println("by Point ${Voies.searchClosest(Point(4883377, 238200)).name}")
   println("by Point ${Voies.searchClosest(Point(4883377, 238200), 10).map {it.name}}")
   println("by Point ${Voies.searchClosest2(Point(4883377, 238200), 10).map {it.name}}")
   println("by Name ${VoiesArrondissements.districts(Voies.searchBest("Renoir"))}")
   println("by Name ${Arrondissements.arrondissements.size}")
   println("by Name ${Arrondissements.search(Point(4887202, 235788))}")
   println("CHECK ALL ${Voies.voies.filter {VoiesArrondissements.districtsOrNull(it) == null}.map {it.name}.size}")
   println("CHECK ALL ${Voies.voies.filter {VoiesArrondissements.districtsOrNull(it) == null}.map {it.name}}")
   println("addresses ${Addresses.addresses.size}")
   println("address ${Addresses.getNumber("quai de jemmapes", Point(4886892, 236760))}")
   println("address ${Addresses.getNumber("quai de jemmapes", Point(4887000, 236760))}")
   /*time("Database search 1 closest") {for (i in 1..500) Voies.searchClosest(Point(4883377, 238200))}
    time("Database search closest") {for (i in 1..50) Voies.searchClosest(Point(4883377, 238200), 10)}
    time("Database search closest2") {for (i in 1..500) Voies.searchClosest2(Point(4883377, 238200), 10)}
    time("Database loading") {Database.getUser("paf")} */
}