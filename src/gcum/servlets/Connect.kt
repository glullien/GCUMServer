package gcum.servlets

import gcum.db.AutoLogin
import gcum.db.Database
import gcum.db.User
import gcum.db.UserExistsException
import gcum.utils.SecretCode
import gcum.utils.sendMail
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import kotlin.concurrent.withLock

data class Session(val sessionId: String, val username: String, val autoLogin: AutoLogin?)
object Sessions {
   private val sessionIdAttribute = "sessionId"
   private val sessions = ConcurrentHashMap<String, Session>()
   private val sessionsLock = ReentrantLock()
   private val nextSessionId = SecretCode({code-> sessions.containsKey(code)})
   fun login(session: HttpSession, username: String, remindMe: Boolean): Session {
      sessionsLock.withLock {
         val sessionId = nextSessionId.new()
         session.setAttribute(sessionIdAttribute, sessionId)
         val autoLoginCode = if (remindMe) Database.generateAutoLoginCode(username) else null
         val res = Session(sessionId, username, autoLoginCode)
         sessions[sessionId] = res
         return res
      }
   }

   fun logout(session: HttpSession) {
      sessionsLock.withLock {
         val sessionId = session.getAttribute(sessionIdAttribute)
         if (sessionId is String) {
            val oldSession = sessions.remove(sessionId)
            if (oldSession?.autoLogin != null) Database.removeAutoLoginCode(oldSession?.autoLogin.code)
         }
      }
   }

   fun isLogin(sessionId: String) = sessions.containsKey(sessionId)
   fun username(sessionId: String) = sessions[sessionId]?.username
   fun user(sessionId: String): User? {
      val username = username(sessionId)
      return if (username == null) null else Database.getUser(username)
   }
}

private fun jsonSuccess(session: Session) = if (session.autoLogin != null) jsonSuccess {
   put("autoLogin", session.autoLogin.code)
   //  Sun, 28 Feb 2020
   put("validTo", session.autoLogin.validTo.format(DateTimeFormatter.ofPattern("EEE, dd MMM YYYY", Locale.US)))
} else jsonSuccess {}

@WebServlet(name = "Register", value = "/register")
class Register : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val username = request.getString("username")
      val password = request.getString("password")
      val email = request.getStringOrNull("email")
      val remindMe = request.getBoolean("remindMe")
      try {
         Database.addUser(username, password, email)
         return jsonSuccess(Sessions.login(request.session, username, remindMe))
      } catch (e: UserExistsException) {
         return jsonError("Le pseudo $username existe déjà")
      }
   }
}

@WebServlet(name = "Login", value = "/login")
class Login : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val username = request.getString("username")
      val password = request.getString("password")
      val remindMe = request.getBoolean("remindMe")
      val user = Database.getUser(username)
      if ((user == null) || (user.password != password)) return jsonError("Les identifiants sont incorrects")
      return jsonSuccess(Sessions.login(request.session, username, remindMe))
   }
}

@WebServlet(name = "Logout", value = "/logout")
class Logout : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      Sessions.logout(request.session)
      return jsonSuccess {}
   }
}

@WebServlet(name = "SendID", value = "/sendID")
class SendID : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val email = request.getString("email")
      val user = Database.getUserFromEmail(email) ?: return jsonError("Aucun pseudo n'a été enregistré avec cet Email")
      sendMail(listOf("gurvan.lullien@gmail.com"), "Rappel des identifiants", "/gcum/servlets/MailLoginIDs.html", mapOf(
         "username" to user.username,
         "password" to user.password
      ))
      return jsonSuccess {}
   }
}


@WebServlet(name = "AutoLogin", value = "/autoLogin")
class AutoLogin : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val cookie = request.getString("cookie")
      val autoLogin = Database.getAutoLogin(cookie)
      return if ((autoLogin == null) || (autoLogin.validTo.isBefore(LocalDate.now()))) jsonError("invalid cookie $cookie")
      else jsonSuccess(Sessions.login(request.session, autoLogin.username, false))
   }
}