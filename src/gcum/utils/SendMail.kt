package gcum.utils

import gcum.conf.Configuration
import java.util.*
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

private val log = getLogger()

fun body(resourceName: String, variables: Map<String, String>): String {
   fun String.searchVar(varName: String): Int {
      tailrec fun String.searchVar(start: Int): Int {
         val index = this.indexOf("$" + varName, start)
         return if (index < 0) index
         else if (index + varName.length + 1 >= this.length) index
         else if (!get(index + varName.length + 1).isLetterOrDigit()) index
         else this.searchVar(index + 1)
      }
      return this.searchVar(0)
   }

   fun String.replaceVar(varName: String, value: String): String {
      val index = this.searchVar(varName)
      return if (index < 0) this
      else this.substring(0, index) + value + this.substring(index + varName.length + 1)
   }

   tailrec fun String.replaceVars(v: Map<String, String>): String {
      if (v.isEmpty()) return this
      else {
         val first = v.entries.first()
         val nextMap = HashMap<String, String>(v)
         nextMap.remove(first.key)
         return this.replaceVar(first.key, first.value).replaceVars(nextMap)
      }
   }

   val resourceLines = log.javaClass.getResourceAsStream(resourceName).bufferedReader().lineSequence()
   return resourceLines.joinToString("\n") {it.replaceVars(variables)}
}

fun sendMail(recipients: List<String>, subject: String, bodyResourceName: String, variables: Map<String, String>) {

   val properties = Properties()
   properties.put("mail.transport.protocol", "smtps")
   properties.put("mail.smtps.user", Configuration.getString("mail.user"))
   properties.put("mail.smtp.from", Configuration.getString("mail.from"))
   properties.put("mail.smtp.host", "smtp.gmail.com")
   properties.put("mail.smtp.port", "587")
   properties.put("mail.smtp.auth", "true")
   properties.put("mail.smtp.starttls.enable", "true")
   val session = Session.getDefaultInstance(properties, object : Authenticator() {
      override fun getPasswordAuthentication() = PasswordAuthentication(Configuration.getString("mail.user"), Configuration.getString("mail.password"))
   })

   val msg = MimeMessage(session)
   msg.setFrom(InternetAddress(Configuration.getString("mail.from")))
   for (to in recipients) msg.addRecipient(javax.mail.Message.RecipientType.TO, InternetAddress(to, to))
   msg.setSubject(subject, "UTF-8")
   val mp = MimeMultipart()
   val htmlPart = MimeBodyPart()
   val html = body(bodyResourceName, variables)
   htmlPart.setContent(html, "text/html")
   mp.addBodyPart(htmlPart)
   msg.setContent(mp)
   msg.sentDate = Date()
   Transport.send(msg)
}

fun main(args: Array<String>) {
   sendMail(listOf("gurvan.lullien@gmail.com"), "cocou", "/gcum/servlets/MailLoginIDs.html", mapOf(
      "username" to "caca",
      "password" to "cucucu"
   ))
}