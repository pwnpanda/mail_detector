package com.robinlunde.mailbox.debug

import androidx.annotation.StringRes
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R

enum class ScanType(@StringRes res: Int) {
    ACTIVE(R.string.activeScan),
    BACKGROUND(R.string.bgScan);

    fun toString(@StringRes res: Int): String {
        return MailboxApp.getInstance().getString(res)
    }
}
