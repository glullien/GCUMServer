@file:JvmName("Session")

package gcum.tld

import gcum.db.UserRole
import gcum.servlets.Sessions

fun isLogin(sessionId: Int) = Sessions.isLogin(sessionId)
fun isAdmin(sessionId: Int) = Sessions.user(sessionId)?.role == UserRole.Admin
fun username(sessionId: Int) = Sessions.username(sessionId)