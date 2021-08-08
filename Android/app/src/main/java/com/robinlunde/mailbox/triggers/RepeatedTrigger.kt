package com.robinlunde.mailbox.triggers

import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.datamodel.MyMessage


class RepeatedTrigger : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val myTag = "Repeating alarm trigger"
        val util: Util = MailboxApp.getUtil()
        val hour = intent.getIntExtra("hour", -1)
        val minute = intent.getIntExtra("minute", -1)

        // Invalid values received. Just stop!
        if (hour == -1 || minute == -1) return

        Log.d(myTag, "Setting alarm and notifications for: $hour:$minute")

        val msg = MyMessage(
            "PILL ALERT!",
            "You have not taken all your pills for today!"
        )

        util.pushNotification(msg, true)

        // Custom alarm
        var alert: Uri? =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + R.raw.storm_ambulance)
        if (alert == null) {
            // alert is null, using backup
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (alert == null) {
                // alert is null, using backup
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                if (alert == null) {
                    // alert backup is null, using 2nd backup
                    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                }
            }
        }
        val ringtone =
            RingtoneManager.getRingtone(context, alert)
        ringtone.audioAttributes =
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
        ringtone!!.play()
    }
}