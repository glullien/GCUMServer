package gcum.servlets

import gcum.db.Database
import gcum.db.User
import gcum.db.UserExistsException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

object Sessions {
   private val sessionIdAttribute = "sessionId"
   private val nextSessionId = AtomicInteger(Random().nextInt(100000))
   private val sessions = ConcurrentHashMap<Int, String>()
   fun login(session: HttpSession, username: String): Int {
      val sessionId = nextSessionId.andIncrement
      session.setAttribute(sessionIdAttribute, sessionId)
      sessions[sessionId] = username
      return sessionId
   }

   fun logout(session: HttpSession) {
      val sessionId = session.getAttribute(sessionIdAttribute)
      if (sessionId is Int) sessions.remove(sessionId)
   }

   fun isLogin(sessionId: Int) = sessions.containsKey(sessionId)
   fun username(sessionId: Int) = sessions[sessionId]
   fun user(sessionId: Int): User? {
      val username = username(sessionId)
      return if (username == null) null else Database.getUser(username)
   }
}

@WebServlet(name = "Register", value = "/register")
class Register : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      val username = request.getString("username")
      val password = request.getString("password")
      val email = request.getStringOrNull("email")
      try {
         Database.addUser(username, password, email)
         return jsonSuccess {Sessions.login(request.session, username)}
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
      val user = Database.getUser(username)
      if ((user == null) || (user.password != password)) return jsonError("Les identifiants sont incorrects")
      return jsonSuccess {Sessions.login(request.session, username)}
   }
}

@WebServlet(name = "Logout", value = "/logout")
class Logout : JsonServlet() {
   override fun doPost(request: HttpServletRequest): Map<String, *> {
      Sessions.logout(request.session)
      return jsonSuccess {}
   }
}