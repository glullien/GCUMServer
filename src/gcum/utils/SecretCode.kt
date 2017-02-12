package gcum.utils

import java.util.*

class SecretCode(val isUsed: (String) -> Boolean, val length: Int=20) {
   private val SECRET_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmlnopqrstuvwxyz0123456789"
   private val random = Random()

   fun new(): String {
      var res: String
      do {
         val sb = StringBuilder()
         for (i in 0..19) sb.append(SECRET_CODE_CHARS[random.nextInt(SECRET_CODE_CHARS.length)])
         res = sb.toString()
      } while (isUsed(res))
      return res
   }
}