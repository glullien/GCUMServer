package gcum.conf

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.FileSystems
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class KProperties(val file: File) {
   private val properties = Properties()

   init {
      if (file.exists()) FileReader(file).use {properties.load(it)}
   }

   fun save() = FileWriter(file).use {properties.store(it, null)}

   class MissingKeyException(key: String) : Exception("Missing $key")

   private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

   fun getString(key: String): String = properties.getProperty(key) ?: throw MissingKeyException(key)
   fun getPath(key: String): Path = FileSystems.getDefault().getPath(getString(key))
   fun getInt(key: String): Int = getString(key).toInt()
   fun getLong(key: String): Long = getString(key).toLong()
   fun getDouble(key: String): Double = getString(key).toDouble()
   fun getDate(key: String): LocalDate = LocalDate.parse(getString(key), dateFormatter)
   inline fun <reified T : Enum<T>> getEnum(key: String): T = java.lang.Enum.valueOf(T::class.java, getString(key))

   fun setString(key: String, value: String) = properties.setProperty(key, value)
   fun setInt(key: String, value: Int) = setString(key, value.toString())
   fun setLong(key: String, value: Long) = setString(key, value.toString())
   fun setDouble(key: String, value: Double) = setString(key, value.toString())
   fun setDate(key: String, value: LocalDate) = setString(key, value.format(dateFormatter))
   fun <T : Enum<T>> setEnum(key: String, value: T) = setString(key, value.name)

   override fun toString() = properties.map {e-> "${e.key}=${e.value}"}.joinToString()
}