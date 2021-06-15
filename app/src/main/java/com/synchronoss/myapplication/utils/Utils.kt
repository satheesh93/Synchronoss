package com.synchronoss.myapplication.utils

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.synchronoss.myapplication.R
import java.util.*

class Utils {

    object Util {

        val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence = "Verbose WorkManager Notifications"
        val NOTIFICATION_TITLE: CharSequence = "Weather Update"
        const val CHANNEL_ID = "NOTIFICATION"
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_DESCRIPTION =
            "Shows notifications whenever work starts"

        fun makeStatusNotification(
            notificationid: Int,
            message: String,
            context: Context,
            title: String
        ) {

            // Make a channel if necessary
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the NotificationChannel, but only on API 26+ because
                // the NotificationChannel class is new and not in the support library
                val name = VERBOSE_NOTIFICATION_CHANNEL_NAME
                val description = NOTIFICATION_CHANNEL_DESCRIPTION
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance)
                channel.description = description

                // Add the channel
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

                notificationManager?.createNotificationChannel(channel)
            }

            // Create the notification
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(LongArray(0))

            // Show the notification
            NotificationManagerCompat.from(context).notify(notificationid, builder.build())
        }

        fun isAllPermissionsGranted(
            activity: Activity,
            listOfPermissions: Array<String>
        ): Boolean {
            for (permission in listOfPermissions) {
                if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
            return true
        }

        fun requestPermissions(
            activity: Activity,
            listOfPermissions: Array<String>,
            permissionRequestCode: Int
        ) {
            ActivityCompat.requestPermissions(
                activity,
                listOfPermissions,
                permissionRequestCode
            )
        }
    }

}

