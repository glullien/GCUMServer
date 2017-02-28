package gcum.conf

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.FileSystems
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalTime
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
   private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

   fun getStringOrNull(key: String): String? = properties.getProperty(key)
   fun getString(key: String): String = getStringOrNull(key) ?: throw MissingKeyException(key)
   fun getPath(key: String): Path = FileSystems.getDefault().getPath(getString(key))
   fun getInt(key: String): Int = getString(key).toInt()
   fun getBoolean(key: String): Boolean = getString(key).toBoolean()
   fun getLong(key: String): Long = getString(key).toLong()
   fun getDouble(key: String): Double = getString(key).toDouble()
   fun getDate(key: String): LocalDate = LocalDate.parse(getString(key), dateFormatter)
   fun getTimeOrNull(key: String): LocalTime? = getStringOrNull(key).let {if (it ==null) null else LocalTime.parse(it, timeFormatter)}
   fun getTime(key: String): LocalTime = LocalTime.parse(getString(key), timeFormatter)
   inline fun <reified T : Enum<T>> getEnum(key: String): T = java.lang.Enum.valueOf(T::class.java, getString(key))

   fun setString(key: String, value: String) = properties.setProperty(key, value)
   fun setInt(key: String, value: Int) = setString(key, value.toString())
   fun setLong(key: String, value: Long) = setString(key, value.toString())
   fun setDouble(key: String, value: Double) = setString(key, value.toString())
   fun setDate(key: String, value: LocalDate) = setString(key, value.format(dateFormatter))
   fun setTime(key: String, value: LocalTime) = setString(key, value.format(timeFormatter))
   fun <T : Enum<T>> setEnum(key: String, value: T) = setString(key, value.name)

   override fun toString() = properties.map {e-> "${e.key}=${e.value}"}.joinToString()
}