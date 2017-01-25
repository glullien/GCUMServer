package gcum.db

import gcum.conf.Configuration
import gcum.conf.KProperties
import gcum.geo.Point
import java.io.File
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object Database {

   val root: Path = Configuration.getPath("root")
   val photos = ConcurrentLinkedQueue<Photo>()
   val points = ConcurrentHashMap<Point, ConcurrentLinkedQueue<Photo>>()

   //val addDicoveredPhotoExecPool = Executors.newSingleThreadExecutor()

   init {
      fun File.isImageFile() = name.toLowerCase().matches(Regex(".*\\.(jpg|jpeg)"))
      root.toFile().listFiles {file: File-> file.isDirectory}.forEach {
         districtDir->
         districtDir.listFiles {file: File-> file.isDirectory}.forEach {
            streetDir->
            streetDir.listFiles {file: File-> file.isDirectory}.forEach {
               dateDir->
               val auxDir = File(dateDir, "aux")
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
}

fun main(args: Array<String>) {
   Database.shutdown()
}