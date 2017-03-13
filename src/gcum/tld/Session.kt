@file:JvmName("Session")

package gcum.tld

import gcum.db.*
import gcum.servlets.Sessions

fun isLogin(sessionId: String) = Sessions.isLogin(sessionId)
fun isAdmin(sessionId: String) = Sessions.user(sessionId)?.role == UserRole.Admin
fun username(sessionId: String) = Sessions.username(sessionId)
fun email(sessionId: String) = Sessions.email(sessionId)
fun hasEmail(sessionId: String) = Sessions.email(sessionId) != null
fun isReceivingMailFor(sessionId: String, cause: NotificationCause) = Database.getNotifications(username(sessionId) ?: throw Exception("Not connected"), cause).any {it.media == NotificationMedia.Email}
fun isReceivingMailForLiked(sessionId: String) = isReceivingMailFor(sessionId, NotificationCause.Liked)
fun isReceivingMailForNews(sessionId: String) = isReceivingMailFor(sessionId, NotificationCause.News)
fun versionName() = Database.versionName
fun getRemoveFromMailsUserName(code: String) = Database.getRemoveFromMails(code)?.username
fun removeFromNews(username: String): Boolean {
   Database.removeNotifications(username, setOf(Notification(NotificationCause.News, NotificationMedia.Email)))
   return true
}