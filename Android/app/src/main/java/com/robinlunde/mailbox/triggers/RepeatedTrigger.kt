package com.robinlunde.mailbox.triggers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.robinlunde.mailbox.MailboxApp

class RepeatedTrigger : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // val message = "You have not taken all your pills for today!! Taken them ASAP"
        val hour = intent.getIntExtra("hour", -1)
        val minute = intent.getIntExtra("minute",-1)

        // Invalid values received. Just stop!
        if (hour == -1 || minute == -1) return

        Log.d("Repeating alarm trigger", "Setting alarm and notifications for: $hour:$minute")
        // TODO
        // Set notification at hh:mm + 4
        // Set alarm at hh:mm + 5min

        // Set same alarm for next day!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            MailboxApp.getUtil().activateAlarm(hour, minute, tomorrow = true)
        }
    }
}