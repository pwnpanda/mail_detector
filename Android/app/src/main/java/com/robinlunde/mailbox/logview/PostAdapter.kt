package com.robinlunde.mailbox.logview


import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.datamodel.PostLogEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PostAdapter(private val postLogEntries: MutableList<PostLogEntry>) :
    RecyclerView.Adapter<Util.LogItemViewHolder>() {

    override fun getItemCount() = postLogEntries.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: Util.LogItemViewHolder, position: Int) {
        // If there is data, render it!
        if (itemCount > 0) {
            val postLogEntry = postLogEntries[position]

            // If post belongs to this user, change background color of row
            if (MailboxApp.getUsername().equals(postLogEntry.username, ignoreCase = true)) {
                holder.constraintLayout.setBackgroundColor(
                    MailboxApp.getInstance().getColor(R.color.highlight)
                )
            }
            // Set content for each UI element to the respective part of the postLogEntry
            holder.constraintLayout.findViewById<TextView>(R.id.post_user).text =
                postLogEntry.username
            holder.constraintLayout.findViewById<TextView>(R.id.post_deliver_time).text =
                postLogEntry.deliveredTime
            holder.constraintLayout.findViewById<TextView>(R.id.post_deliver_date).text =
                postLogEntry.deliveredDate
            holder.constraintLayout.findViewById<TextView>(R.id.post_pickup_time).text =
                postLogEntry.pickupTime
            holder.constraintLayout.findViewById<TextView>(R.id.post_pickup_date).text =
                postLogEntry.pickupDate

            // For the button, add onClickListener to delete the current entry
            holder.constraintLayout.findViewById<ImageButton>(R.id.delete_button)
                .setOnClickListener {
                    //Log.e("DeletePress", "Pressed for ID ${item.id}")
                    // Delete the log with ID id
                    CoroutineScope(Dispatchers.IO + Job()).launch {
                        MailboxApp.getUtil().tryRequest(
                            MailboxApp.getInstance().getString(R.string.deleteLogsMethod),
                            null,
                            postLogEntry.id,
                            null
                        )
                    }
                }
            // If there is no data found, show error
        } else {
            holder.constraintLayout.findViewById<RecyclerView>(R.id.post_entries).visibility =
                View.INVISIBLE
            holder.constraintLayout.findViewById<TextView>(R.id.error_logs).visibility =
                View.INVISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Util.LogItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view =
            layoutInflater.inflate(
                R.layout.recyclerview_row_logview,
                parent,
                false
            ) as ConstraintLayout
        return Util.LogItemViewHolder(view)
    }
}