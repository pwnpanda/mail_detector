package com.robinlunde.mailbox

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class MyNotificationManager {
    // Register the channel with the system
    private val myNotificationManager: NotificationManager =
        MailboxApp.getInstance().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val fullScreenIntent = Intent(MailboxApp.getInstance(), MainActivity::class.java)
    private val fullScreenPendingIntent: PendingIntent = PendingIntent.getActivity(MailboxApp.getInstance(), 0,
        fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    private var count: Int = 0

    fun createPush(
        timeStamp: String,
        //@DrawableRes smallIcon: Int = R.drawable.ic_stat_mailbox,
        //@DrawableRes largeIcon: Int = R.mipmap.icon_mailbox
        @DrawableRes smallIcon: Int = R.mipmap.ic_launcher,
        @DrawableRes largeIcon: Int = R.mipmap.ic_launcher
    ) : Int {
        val builder = NotificationCompat.Builder(MailboxApp.getInstance(), MailboxApp.getInstance().getString(R.string.channel_id))
            // Set small icon
            .setSmallIcon(smallIcon)
            // Set big icon
            .setLargeIcon(BitmapFactory.decodeResource(MailboxApp.getInstance().resources, largeIcon))
            .setContentTitle("The mailman has been here!")
            .setContentText("The mailman delivered the mail at: $timeStamp")
            .setPriority(NotificationCompat.PRIORITY_MAX)
                // Removes notification when pressed
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)

            // notificationId is a unique int for each notification that you must define
            myNotificationManager.notify(count, builder.build())
            return count++
        }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(channel: NotificationChannel){
        myNotificationManager.createNotificationChannel(channel)
    }
}