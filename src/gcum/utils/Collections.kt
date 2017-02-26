package gcum.utils

import java.util.*

fun <T> Collection<T>.first(nb: Int, f: (T) -> Double): List<T> {
   class R(val t: T, val n: Double) : Comparable<R> {
      override fun compareTo(other: R) = n.compareTo(other.n)
   }

   val res = mutableListOf<R>()
   var leastN = Double.MAX_VALUE
   for (e in this) {
      val n = f(e)
      if (res.size < nb) {
         res.add(R(e, n))
         if (res.size == nb) {
            Collections.sort(res)
            leastN = res.last().n
         }
      } else if (n < leastN) {
         res.add(R(e, n))
         Collections.sort(res)
         while (nb < res.size) res.removeAt(res.size - 1)
         leastN = res.last().n
      }
   }

   if (res.size < nb) Collections.sort(res)

   return res.map {it.t}
}