package gcum.conf

import java.io.File

object Configuration {
   private val properties = KProperties(File(System.getProperty("user.home") + File.separator + ".gcum.conf"))

   fun getString(key: String): String = properties.getString(key)
   fun getPath(key: String) = properties.getPath(key)
}