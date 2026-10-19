package com.example.livenotificationdemo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/*
Why use MyFirebaseMessagingService and MainActivity?

MyFirebaseMessagingService (message delivery focused) -
- getting messages
- getting refreshed FCM tokens
- reading message content
- creating notification channels
- building visible notifications
- handling messages when MainActivity is not visible

MainActivity (ui focused) -
- displaying the compose ui
- asking for notification permission
- showing permission status
- getting the current FCM token
- showing the token for testing
 */

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {

        //use this tag to filter service messages in logcat
        private const val TAG = "FCMService"

        /*
        unique ID for the notification channel

        this must match the value in strings.xml:
        default_notification_channel_id
         */
        private const val CHANNEL_ID = "live_notifications"
    }

    /*
    called when firebase creates or refreshes the FCM registration token

    the token identifies one installation of the app

    in production:
    - send the token to your backend
    - associate the token with the correct user/device

    in this demo:
    - print the token in logcat
    - paste it into firebase console for testing
     */
    override fun onNewToken(token: String) {

        super.onNewToken(token)

        //view the token in logcat for testing
        Log.d(
            TAG,
            "NEW FCM TOKEN: $token"
        )
    }

    /*
    called when the service receives an FCM message

    foreground:
    - this callback runs
    - the app creates the notification

    background notification message:
    - firebase/android may display the notification automatically

    data message:
    - custom data is delivered here when android permits it
     */
    override fun onMessageReceived(
        remoteMessage: RemoteMessage
    ) {

        super.onMessageReceived(remoteMessage)

        Log.d(
            TAG,
            "Message received from: ${remoteMessage.from}"
        )

        /*
        remoteMessage.notification contains the standard firebase
        notification payload

        common values:
        - title
        - body
        - image url
         */
        val notificationTitle =
            remoteMessage.notification?.title

        val notificationBody =
            remoteMessage.notification?.body

        /*
        remoteMessage.data contains custom key-value values

        example:
        title = "New practical"
        message = "Practical 3 has been uploaded"
        screen = "practicals"
         */
        val dataTitle =
            remoteMessage.data["title"]

        val dataMessage =
            remoteMessage.data["message"]

        /*
        use the notification payload first

        if it doesn't exist, use the data payload

        if neither exists, use a default value
         */
        val finalTitle =
            notificationTitle
                ?: dataTitle
                ?: "New Notification"

        val finalMessage =
            notificationBody
                ?: dataMessage
                ?: "You have received a new Firebase message."

        Log.d(
            TAG,
            "Notification title: $finalTitle"
        )

        Log.d(
            TAG,
            "Notification message: $finalMessage"
        )

        /*
        create the notification channel before displaying the notification

        calling this repeatedly is safe because android won't create
        duplicate channels with the same ID
         */
        createNotificationChannel()

        //build and display the actual android notification
        showNotification(
            title = finalTitle,
            message = finalMessage
        )
    }

    /*
    create the notification channel required on android 8.0+

    notification channels allow the user to control:
    - sound
    - vibration
    - importance
    - notification visibility
     */
    private fun createNotificationChannel() {

        /*
        notification channels were introduced in android 8.0

        older android versions don't use channels
         */
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        ) {

            //visible channel name shown in android settings
            val channelName =
                getString(
                    R.string.notification_channel_name
                )

            //visible channel description shown in android settings
            val channelDescription =
                getString(
                    R.string.notification_channel_description
                )

            /*
            high importance allows a prominent notification

            the final behaviour still depends on:
            - user settings
            - device sound settings
            - do not disturb mode
             */
            val importance =
                NotificationManager.IMPORTANCE_HIGH

            val notificationChannel =
                NotificationChannel(
                    CHANNEL_ID,
                    channelName,
                    importance
                ).apply {

                    description =
                        channelDescription

                    //enable vibration if the user/device permits it
                    enableVibration(true)

                    //enable notification lights on supported devices
                    enableLights(true)
                }

            /*
            get android's system notification manager

            this service manages notification channels and notifications
             */
            val notificationManager =
                getSystemService(
                    NotificationManager::class.java
                )

            /*
            register the notification channel with android

            re-registering an existing channel with the same ID
            doesn't create a duplicate
             */
            notificationManager
                .createNotificationChannel(
                    notificationChannel
                )
        }
    }

    /*
    build and display an android notification

    title = heading received from firebase
    message = body received from firebase
     */
    private fun showNotification(
        title: String,
        message: String
    ) {

        /*
        intent describes what should happen when the notification
        is tapped

        this intent opens MainActivity
         */
        val mainActivityIntent =
            Intent(
                this,
                MainActivity::class.java
            ).apply {

                /*
                reuse an existing MainActivity when possible

                CLEAR_TOP:
                remove activities above an existing MainActivity

                SINGLE_TOP:
                reuse MainActivity if it's already at the top
                 */
                flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP

                /*
                pass the notification contents to MainActivity

                a larger application could use these values to:
                - open a specific page
                - show a specific announcement
                - load a record from an API
                 */
                putExtra(
                    "notification_title",
                    title
                )

                putExtra(
                    "notification_message",
                    message
                )
            }

        /*
        PendingIntent allows android to perform the intent later

        the notification system can open MainActivity even if
        the app is not currently visible
         */
        val pendingIntent =
            PendingIntent.getActivity(
                this,

                //request code identifies this PendingIntent
                0,

                mainActivityIntent,

                /*
                UPDATE_CURRENT:
                update the existing PendingIntent with new values

                IMMUTABLE:
                prevent other applications from modifying the intent
                 */
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        /*
        build the visible notification

        NotificationCompat supports notifications consistently
        across different android versions
         */
        val notification =
            NotificationCompat.Builder(
                this,
                CHANNEL_ID
            )

                //every notification requires a small icon
                .setSmallIcon(
                    R.drawable.ic_notification
                )

                //main notification heading
                .setContentTitle(
                    title
                )

                //short message shown when notification is collapsed
                .setContentText(
                    message
                )

                /*
                allow longer notification text to expand instead of
                being cut off
                 */
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )

                /*
                priority mainly affects android versions before
                notification channels controlled importance
                 */
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )

                //open MainActivity when notification is tapped
                .setContentIntent(
                    pendingIntent
                )

                //remove notification after the user taps it
                .setAutoCancel(true)

                //use the normal device notification defaults
                .setDefaults(
                    NotificationCompat.DEFAULT_ALL
                )

                .build()

        /*
        every posted notification needs an integer ID

        using the current time produces a different ID for each
        notification so new messages don't replace previous messages
         */
        val notificationId =
            System.currentTimeMillis()
                .toInt()

        try {

            /*
            request android to display the notification

            this is the line that actually posts the notification
             */
            NotificationManagerCompat
                .from(this)
                .notify(
                    notificationId,
                    notification
                )

            Log.d(
                TAG,
                "Notification displayed successfully."
            )

        } catch (
            securityException: SecurityException
        ) {

            /*
            this may occur on android 13+ when notification
            permission hasn't been granted
             */
            Log.e(
                TAG,
                "Notification permission has not been granted.",
                securityException
            )
        }
    }
}