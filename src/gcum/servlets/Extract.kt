package gcum.servlets

import gcum.db.Database
import java.io.File
import java.nio.file.Path
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@WebServlet(name = "Extract", value = "/extract")
class Extract : HttpServlet() {

   override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
      request.characterEncoding = "UTF-8"
      val districtParameter = request.getString("district", Pattern.compile("(all|\\d+)"))
      val all = Database.allPhotos
      val photos = when (districtParameter) {
         "all"->all
         else-> {
            val district = districtParameter.toInt()
            all.filter {it.location.address.district == district}
         }
      }
      val size = photos.map {it.file.length()}.sum()
      response.contentType = "application/zip"
      response.setContentLength(size.toInt())
      ZipOutputStream(response.outputStream).use {
         zip->
         zip.setLevel(0)
         val root = Database.root
         val entryDirs = mutableListOf<Path>()
         for (photo in photos.sortedBy {it.file.absolutePath}) {
            fun putEntryDirs(path: Path) {
               if (path.count() > 1) putEntryDirs(path.parent)
               if (!entryDirs.contains(path)) {
                  zip.putNextEntry(ZipEntry(path.joinToString("/") + "/"))
                  entryDirs.add(path)
               }
            }

            val fileName = photo.file
            val pathFile = fileName.toPath()
            if (!pathFile.startsWith(root)) throw ServletException("Wrong file name $pathFile - $root")
            val entryPath = root.relativize(pathFile)
            putEntryDirs(entryPath.parent)
            val entry = ZipEntry(entryPath.joinToString("/"))
            zip.putNextEntry(entry)
            photo.file.inputStream().use {it.copyTo(zip)}
            zip.closeEntry()
         }
      }
   }
}