package gcum.utils

import gcum.conf.Configuration
import gcum.db.Photo
import twitter4j.StatusUpdate
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import java.util.concurrent.Executors

private class PhotoTwitter {
   private val token = AccessToken(Configuration.getString("twitter_token"), Configuration.getString("twitter_token_secret"))
   private val twitter = TwitterFactory().getInstance(token)
   fun tweet(photo: Photo) {
      val statusUpdate = StatusUpdate(photo.location.address.text + " par " + photo.username + " #GCUM")
      statusUpdate.setMedia(photo.file)
      twitter.updateStatus(statusUpdate)
   }
}

private val photoTwitter = if (Configuration.getBoolean("twitter")) PhotoTwitter() else null
private val photoTwitterExecutor = if (photoTwitter == null) null else Executors.newSingleThreadExecutor {
   r->
   Thread(r).apply {isDaemon = true}
}

fun tweet(photo: Photo) {
   photoTwitterExecutor?.execute {
      photoTwitter?.tweet(photo)
   }
}
