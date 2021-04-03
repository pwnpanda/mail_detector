package com.robinlunde.mailbox

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap

class MyNotificationManager(private val ctx: Context) {
    private val myNotificationManager: NotificationManager =
        ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @RequiresApi(Build.VERSION_CODES.O)
    private val channelId: String = createNotificationChannel()!!

    private var count: Int = 1337

    // Register the channel with the system
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(): String? {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId: String = ctx.getString(R.string.channel_id)
            val name = ctx.getString(R.string.channel_name)
            val descriptionText = ctx.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel =
                NotificationChannel(channelId, name, importance).apply {
                    description = descriptionText
                }
            channel.enableVibration(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            myNotificationManager.createNotificationChannel(channel)
            channelId
        } else {
            null
        }
    }

    fun createPush(
        message: MailboxApp.Companion.MyMessage,
        @DrawableRes smallIcon: Int = R.mipmap.ic_launcher,
        //@DrawableRes largeIcon: Int = R.mipmap.post_box,
    ) {
        // TODO replace with logo - icon_mailbox
        // TODO change message based on input. Not only timestamp
        //val largeIcon = BitmapFactory.decodeResource(MailboxApp.getInstance().resources, R.mipmap.post_box)

        val largeIcon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ResourcesCompat.getDrawable(
                MailboxApp.getInstance().resources,
                R.mipmap.ic_launcher,
                null
            ) as AdaptiveIconDrawable
        } else {
            Log.d("Notification", "Android version too old, ignoring push notifications!")
            null
        }

        // Final intent that will launch action if push notification is pressed
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        // Add in order to identify intent from our own app
        intent.putExtra( MailboxApp.getInstance().getString(R.string.app_name), true)

        // Create temporary intent for the push notification
        val notifyPendingIntent =
            PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Build intent data for the push notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val builder = NotificationCompat.Builder(ctx, channelId).apply {
                // Set small icon
                this.setSmallIcon(smallIcon)
                    // Set big icon
                    .setLargeIcon(largeIcon?.toBitmap())
                this.setContentTitle(message.title)
                this.setContentText(message.text)
                /*
                this.setContentTitle("New mail detected!")
                this.setContentText("The mail was delivered at: $timeStamp")
                */
                priority = NotificationCompat.PRIORITY_MAX
                // Removes notification when pressed
                this.setAutoCancel(true)
                //allow visibility on lock screen
                this.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                //Set following intent
                this.setContentIntent(notifyPendingIntent)
            }

            // notificationId is a unique int for each notification that you must define
            // If static, each notification will overwrite the previous one (PERFECT!! :) )
            // Send it!
            myNotificationManager.notify(count, builder.build())
        } else {
            Log.d("Notification", "Android version too old, ignoring push notifications!")
        }
    }
}