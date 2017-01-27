package gcum.servlets

enum class Zone {All, Inside }
enum class TimeFrame {All, LastDay, LastWeek, LastMonth }

fun htmlEncode(source: String): String {
   val res = StringBuilder()
   for (i in 0..source.length - 1) {
      val c = source[i]
      when (c) {
         '\n'->res.append("<br/>")
         '<'->res.append("&lt;")
         '>'->res.append("&gt;")
         '&'->res.append("&amp;")
         '"'->res.append("&quot;")
         '\''->res.append("&#39;")
         else->if (c > '~') res.append("&#").append(c.toInt()).append(";") else if (c >= ' ') res.append(c)
      }
   }
   return res.toString()
}

fun htmlDecode(source: String): String {
   val res = StringBuilder()
   var i = 0
   while (i < source.length) {
      val c = source[i]
      when (c) {
         '\n'->res.append("\n")
         '<'-> {
            val e = source.indexOf('>', i + 1)
            val balise = source.substring(i, e + 1)
            if (balise == "<br/>") res.append('\n')
            i = e
         }
         '&'-> {
            val j = source.indexOf(';', i + 1)
            val escaped = source.substring(i, j + 1)
            when (escaped) {
               "&amp;"->res.append('&')
               "&lt;"->res.append('<')
               "&gt;"->res.append('>')
               "&agrave;"->res.append(224.toChar())
               "&ccedil;"->res.append(231.toChar())
               "&egrave;"->res.append(232.toChar())
               "&eacute;"->res.append(233.toChar())
               "&ecirc;"->res.append(234.toChar())
               "&quot;"->res.append('"')
               "&#39;"->res.append('\'')
               else->res.append(Integer.parseInt(escaped.substring(2, escaped.length - 1)).toChar())
            }
            i = j
         }
         else->res.append(c)
      }
      i++
   }
   return res.toString()
}
