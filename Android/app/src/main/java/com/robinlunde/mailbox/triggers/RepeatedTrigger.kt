package com.robinlunde.mailbox.triggers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RepeatedTrigger : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // val message = "You have not taken all your pills for today!! Taken them ASAP"
        val hour = intent.getStringExtra("hour")!!.toInt()
        val minute = intent.getStringExtra("minute")!!.toInt()

        // TODO
        // Set notification at hh:mm - 1 min
        // Set alarm at hh:mm
    }
}