@file:JvmName("Session")

package gcum.tld

import gcum.db.UserRole
import gcum.servlets.Sessions

fun isLogin(sessionId: String) = Sessions.isLogin(sessionId)
fun isAdmin(sessionId: String) = Sessions.user(sessionId)?.role == UserRole.Admin
fun username(sessionId: String) = Sessions.username(sessionId)
fun email(sessionId: String) = Sessions.email(sessionId)