package gcum.utils

enum class Level(val java: java.util.logging.Level) {
   SEVERE(java.util.logging.Level.SEVERE),
   WARNING(java.util.logging.Level.WARNING),
   INFO(java.util.logging.Level.INFO),
   CONFIG(java.util.logging.Level.CONFIG),
   FINE(java.util.logging.Level.FINE),
   FINER(java.util.logging.Level.FINER),
   FINEST (java.util.logging.Level.FINEST)
}

class Logger(val java: java.util.logging.Logger) {
   val name: String get () = java.name
   fun severe(message: String, thrown: Throwable? = null) = log(Level.SEVERE, message, thrown)
   fun warning(message: String, thrown: Throwable? = null) = log(Level.WARNING, message, thrown)
   fun info(message: String, thrown: Throwable? = null) = log(Level.INFO, message, thrown)
   fun log(level: Level, message: String, thrown: Throwable? = null) {
      if (thrown == null) java.log(level.java, message)
      else java.log(level.java, message, thrown)
   }
}

fun getLogger(name: String) = Logger(java.util.logging.Logger.getLogger(name))

fun getLogger() = getLogger(getClassName("fr.besoindunconseil.util.LogKt"))