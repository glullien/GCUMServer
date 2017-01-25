package gcum.utils

private val basicExclude = setOf("java.lang.Thread", "fr.besoindunconseil.util.IntrospectionKt")
private tailrec fun getClassName(exclude: Set<String>, stackTrace: List<StackTraceElement>): String {
   val className = stackTrace[0].className
   return if (className !in exclude) className
   else getClassName(exclude, stackTrace.subList(1, stackTrace.size))
}

fun getClassName(exclude: String) = getClassName(setOf(exclude, "java.lang.Thread", "fr.besoindunconseil.util.IntrospectionKt"), Thread.currentThread().stackTrace.toList())
