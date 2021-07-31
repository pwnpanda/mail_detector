package com.robinlunde.mailbox.triggers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.Util

class BootTrigger : BroadcastReceiver() {
    val util: Util = MailboxApp.getUtil()
    val prefs: SharedPreferences = MailboxApp.getPrefs()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action === "android.intent.action.BOOT_COMPLETED") util.activateAlarm(
            prefs.getInt(
                "alarm_hour",
                -1
            ),
            prefs.getInt(
                "alarm_minute",
                -1
            )
        )
    }
}