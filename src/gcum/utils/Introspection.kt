package gcum.utils

import java.time.Duration
import java.time.Instant

private tailrec fun getClassName(exclude: Set<String>, stackTrace: List<StackTraceElement>): String {
   val className = stackTrace[0].className
   return if (className !in exclude) className
   else getClassName(exclude, stackTrace.subList(1, stackTrace.size))
}

fun getClassName(exclude: String) = getClassName(setOf(exclude, "java.lang.Thread"), Thread.currentThread().stackTrace.toList())

private val timerLog = getLogger("timer")
fun <R> time(name: String, u: () -> R): R {
   val t1 = Instant.now()
   val r = u()
   val t2 = Instant.now()
   timerLog.warning("$name: time=${Duration.between(t1, t2).toMillis()} ms")
   return r
}
