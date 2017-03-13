package gcum.utils

import gcum.conf.Configuration
import gcum.db.Photo
import twitter4j.StatusUpdate
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.conf.PropertyConfiguration
import java.util.*
import java.util.concurrent.Executors

private class PhotoTwitter {
   private val token: AccessToken = AccessToken(Configuration.getString("twitter.token"), Configuration.getString("twitter.tokenSecret"))
   private val twitter: Twitter

   init {
      val properties = Properties(System.getProperties())
      properties.setProperty("oauth.consumerKey", Configuration.getString("twitter.consumerKey"))
      properties.setProperty("oauth.consumerSecret", Configuration.getString("twitter.consumerSecret"))
      val configuration = PropertyConfiguration(properties)
      twitter = TwitterFactory(configuration).getInstance(token)
   }

   fun tweet(photo: Photo) {
      val statusUpdate = StatusUpdate(photo.location.address.text + " par " + photo.username + " #GCUM")
      statusUpdate.setMedia(photo.resizedFile(1600))
      twitter.updateStatus(statusUpdate)
   }

   fun tweetTest() {
      val statusUpdate = StatusUpdate("coucou")
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

fun main(args: Array<String>) {
   photoTwitter?.tweetTest()
}