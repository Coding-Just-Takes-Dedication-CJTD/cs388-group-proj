package com.example.ludex_cyrpta

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

//extends FirebaseMessagingService, allowing us to intercept messages sent from the console.
class MyFirebaseMessagingService : FirebaseMessagingService() {

    //  runs automatically if Firebase refreshes your unique ID
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if the message actually has a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            // manually trigger a notification to show up on the screen because Android doesn't do it automatically when the app is open.
            sendNotification(it.title, it.body)
        }
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        // Prepare an Intent to open MainActivity when the user clicks the notification.
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE // Required security flag for Android 12+.
        )

        val channelId = "ludex_notification_channel" // Unique ID for this channel.
        val defaultSoundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)

        // actual UI of the notification.
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Uses existing launcher icon.
            .setContentTitle(title ?: "Ludex Update") // Sets the title (defaults to "Ludex Update" if null).
            .setContentText(messageBody) //actual message text
            .setAutoCancel(true) // notification disappear when clicked.
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent) // Attaches the click action defined above.

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // REQUIRE a "Notification Channel" to display anything on Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Ludex Game Updates", // The name the user sees in their system settings.
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build()) // show the notification.
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}