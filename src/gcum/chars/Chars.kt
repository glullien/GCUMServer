package gcum.chars

import java.util.concurrent.atomic.AtomicBoolean

fun Char.toStdChar() = when (this) {
   'à', 'â', 'ä'->'a'
   'é', 'è', 'ê', 'ë'->'e'
   'ï', 'î'->'i'
   'ö', 'ô'->'o'
   'ù', 'û', 'ü'->'u'
   'ç'->'c'
   'œ'->'o'
   'À', 'Â', 'Ä'->'A'
   'É', 'È', 'Ê', 'Ë'->'E'
   'Ï', 'Î'->'I'
   'Ö', 'Ô'->'O'
   'Ù', 'Û', 'Ü'->'U'
   'Ç'->'C'
   'Œ'->'O'
   else-> {
      val c = toInt()
      if (c in 32..127) this else '?'
   }
}

fun Char.toStdChars() = when (this) {
   'œ'->"oe"
   'Œ'->"OE"
   else->toStdChar().toString()
}

fun String.toStdChars() = toCharArray().map {it.toStdChars()}.joinToString(separator = "")
fun String.toStdLowerChars() = toLowerCase().toStdChars()

fun <T> bestLevenshteinIn(extract: String, source: List<T>, getString: (T) -> String, maxNumber: Int = 10, isCancelled: AtomicBoolean = AtomicBoolean(false)): List<T> {
   val stdExtract = extract.toStdLowerChars().split(' ', '-','\'')

   data class ResultSpotLevenshtein(val item: T, val levenshtein: Int)

   val rsl = mutableListOf<ResultSpotLevenshtein>()
   var i = 0
   while (i < source.size && (rsl.size < maxNumber || rsl[rsl.size - 1].levenshtein > 0) && !isCancelled.get()) {
      val item = source[i]
      val lenvenshtein = levenshteinWordsIn(stdExtract, getString(item).toStdLowerChars())
      if (rsl.size < maxNumber || rsl[rsl.size - 1].levenshtein > lenvenshtein) {
         rsl.add(ResultSpotLevenshtein(item, lenvenshtein))
         rsl.sortBy {it.levenshtein}
         while (rsl.size > maxNumber) rsl.removeAt(maxNumber)
      }
      i++
   }
   return rsl.map {it.item}
}

fun levenshteinWordsIn(extract: List<CharSequence>, text: CharSequence) = text.length + 30 * extract.sumBy {levenshteinIn(it, text)}

fun levenshteinIn(extract: CharSequence, text: CharSequence): Int {
   var res = Integer.MAX_VALUE
   if (text.length < extract.length) res = levenshteinSub(extract, text, 0, text.length) * 2
   else for (i in 0..text.length - extract.length) {
      val bonus = i == 0 || text[i - 1] == ' '
      val distance = levenshteinSub(extract, text, i, i + extract.length)
      res = Math.min(res, distance * 2 + if (bonus) 0 else 1)
   }
   return res
}

fun levenshteinSub(lhs: CharSequence, rhs: CharSequence, rhsStart: Int, rhsEnd: Int): Int {
   val lhsLength = lhs.length
   val rhsLength = rhsEnd - rhsStart

   var cost = IntArray(lhsLength + 1)
   for (i in cost.indices) cost[i] = i
   var newCost = IntArray(lhsLength + 1)

   for (i in 1..rhsLength) {
      newCost[0] = i

      for (j in 1..lhsLength) {
         val match = if (lhs[j - 1] == rhs[rhsStart + i - 1]) 0 else 1

         val costReplace = cost[j - 1] + match
         val costInsert = cost[j] + 1
         val costDelete = newCost[j - 1] + 1

         newCost[j] = Math.min(Math.min(costInsert, costDelete), costReplace)
      }

      val swap = cost
      cost = newCost
      newCost = swap
   }

   return cost[lhsLength]
}
