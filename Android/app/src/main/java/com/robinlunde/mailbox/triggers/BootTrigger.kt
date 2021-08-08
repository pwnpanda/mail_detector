package com.robinlunde.mailbox.triggers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.Util

class BootTrigger : BroadcastReceiver() {
    val util: Util = MailboxApp.getUtil()
    private val prefs: SharedPreferences = MailboxApp.getPrefs()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action === "android.intent.action.BOOT_COMPLETED"){
            Log.d("BootTrigger", "Intent to set alarm sent!")
            util.activateAlarm(
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
}