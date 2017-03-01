package gcum.servlets

import gcum.db.AutoLogin
import gcum.db.Database
import gcum.db.User
import gcum.db.UserExistsException
import gcum.utils.SecretCode
import gcum.utils.sendMail
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
            if (oldSession?.autoLogin != null) Database.removeAutoLoginCode(oldSession.autoLogin.code)
         }
      }
   }

   fun isLogin(sessionId: String) = sessions.containsKey(sessionId)
   fun username(sessionId: String) = sessions[sessionId]?.username
   fun email(sessionId: String): String? {
      val username = username(sessionId)
      return if (username == null) "" else Database.getUser(username)?.email
   }

   fun username(session: HttpSession): String? {
      val sessionId = session.getAttribute(sessionIdAttribute)
      return if (sessionId is String) username(sessionId) else null
   }

   fun user(sessionId: String): User? {
      val username = username(sessionId)
      return if (username == null) null else Database.getUser(username)
   }
}

fun username(request: HttpServletRequest): String? {
   fun autoLogin(request: HttpServletRequest): String? {
      val autoLogin = Database.getAutoLogin(request.getParameter("autoLogin") ?: return null)
      return if ((autoLogin != null) && autoLogin.isValid()) autoLogin.username else null
   }
   return Sessions.username(request.session) ?: autoLogin(request)
}

private fun jsonSuccess(autoLogin: AutoLogin) = jsonSuccess {
   put("autoLogin", autoLogin.code)
   put("validTo", autoLogin.validTo.format(DateTimeFormatter.ofPattern("EEE, dd MMM YYYY", Locale.US)))
}

private fun jsonSuccess(session: Session) = if (session.autoLogin != null) jsonSuccess(session.autoLogin) else jsonSuccess {}

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
      return if ((autoLogin == null) || !autoLogin.isValid()) jsonError("invalid cookie $cookie")
      else jsonSuccess(Sessions.login(request.session, autoLogin.username, false))
   }
}

@WebServlet(name = "GetAutoLogin", value = "/getAutoLogin")
class GetAutoLogin : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val username = request.getString("username")
      val password = request.getString("password")
      val user = Database.getUser(username)
      if ((user == null) || (user.password != password)) return jsonError("Les identifiants sont incorrects")
      return jsonSuccess(Database.generateAutoLoginCode(username))
   }
}

@WebServlet(name = "RegisterAutoLogin", value = "/registerAutoLogin")
class RegisterAutoLogin : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val username = request.getString("username")
      val password = request.getString("password")
      try {
         Database.addUser(username, password, null)
         return jsonSuccess(Database.generateAutoLoginCode(username))
      } catch (e: UserExistsException) {
         return jsonError("Le pseudo $username existe déjà")
      }
   }
}

@WebServlet(name = "TestAutoLogin", value = "/testAutoLogin")
class TestAutoLogin : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val autoLogin = Database.getAutoLogin(request.getString("autoLogin"))
      return jsonSuccess {put("valid", (autoLogin != null) && autoLogin.isValid())}
   }
}

@WebServlet(name = "ChangeEmail", value = "/changeEmail")
class ChangeEmail : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val email = request.getString("email")
      val username = Sessions.username(request.session) ?: return jsonError("Vous devez être connecté")
      Database.changeEmail(username, email)
      return jsonSuccess {}
   }
}

@WebServlet(name = "RemoveEmail", value = "/removeEmail")
class RemoveEmail : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val username = Sessions.username(request.session) ?: return jsonError("Vous devez être connecté")
      Database.changeEmail(username, null)
      return jsonSuccess {}
   }
}

@WebServlet(name = "ChangePassword", value = "/changePassword")
class ChangePassword : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val oldPassword = request.getString("oldPassword")
      val username = Sessions.username(request.session) ?: return jsonError("Vous devez être connecté")
      val user = Database.getUser(username)
      if ((user == null) || (user.password != oldPassword)) return jsonError("Les identifiants sont incorrects")
      val password = request.getString("password")
      Database.changePassword(username, password)
      return jsonSuccess {}
   }
}
