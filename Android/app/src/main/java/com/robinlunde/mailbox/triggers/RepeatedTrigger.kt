package com.robinlunde.mailbox.triggers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.datamodel.MyMessage
import java.util.*
import kotlin.concurrent.timerTask

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
            "You have not taken all your pills for today!! Taken them ASAP"
        )

        val timedTask = timerTask {
            Log.d(myTag, "Timer task triggering - sending push notification")
            util.pushNotification(msg)
            util.removeTask()
        }
        // Set notification to trigger at hh:mm - 1
        Timer().schedule(
            timedTask,
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.MINUTE, -1)
            }.time
        )
        util.setTask(timedTask)

        // Set alarm at hh:mm
        // TODO Test!
        // TODO maybe implement own alarm directly here, then cancelling the intent will be sufficient and notification does not need to be scheduled
        // Just set the god damn alarm for the right time, with the right message!
        val int = Intent(AlarmClock.ACTION_SET_ALARM)
        int.putExtra(AlarmClock.EXTRA_MESSAGE, "You have not taken all your pills for today!! Taken them ASAP")
        int.putExtra(AlarmClock.EXTRA_HOUR, hour)
        int.putExtra(AlarmClock.EXTRA_MINUTES, minute)
        startActivity(context, int,null)

        // Set same alarm for next day!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            util.activateAlarm(hour, minute, tomorrow = true)
        }
    }
}