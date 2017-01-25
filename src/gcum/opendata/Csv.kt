package gcum.opendata

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

fun readCsv(input: InputStream): List<List<String>> {
   fun fields(line: String): List<String> {
      val res = mutableListOf<String>()
      var crt = StringBuilder()
      var inCommas = false
      var i = 0
      while (i < line.length) {
         val c = line[i]
         if (inCommas) when (c) {
            '"'->
               if ((i < line.length - 1) && (line[i + 1] == '"')) {
                  crt.append(c)
                  i++
               }
               else inCommas = false
            else->crt.append(c)
         }
         else when (c) {
            ';'-> {
               res.add(crt.toString())
               crt = StringBuilder()
            }
            '"'->inCommas = true
            else->crt.append(c)
         }
         i++
      }
      res.add(crt.toString())
      return res.toList()
   }
   return BufferedReader(InputStreamReader(input)).readLines().map(::fields).toList()
}