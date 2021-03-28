package com.robinlunde.mailbox


import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.graphics.toColor
import androidx.recyclerview.widget.RecyclerView

class PostAdapter(private val postLogEntries: MutableList<PostLogEntry>): RecyclerView.Adapter<Util.LogItemViewHolder>()  {

    override fun getItemCount() = postLogEntries.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: Util.LogItemViewHolder, position: Int) {
        // If there is data, render it!
        if (itemCount > 0) {
            val postLogEntry = postLogEntries[position]

            // If post belongs to this user, change backgroundcolor to slight red
            if (MailboxApp.getUsername().equals(postLogEntry.username, ignoreCase = true)) {
                val color =
                    ContextCompat.getColor(MailboxApp.getInstance(), R.color.light_red).toColor()
                        .toArgb()
                holder.constraintLayout.setBackgroundColor(
                    Color.argb(
                        25,
                        color.red,
                        color.blue,
                        color.green
                    )
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
                    MailboxApp.getUtil().tryRequest(
                        MailboxApp.getInstance().getString(R.string.deleteLogsMethod),
                        null,
                        postLogEntry.id.toInt()
                    )
                }
        // If there is no data found, show error
        } else {
            holder.constraintLayout.findViewById<RecyclerView>(R.id.post_entries).visibility = View.INVISIBLE
            holder.constraintLayout.findViewById<TextView>(R.id.error_logs).visibility = View.INVISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Util.LogItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.recyclerview_row, parent, false) as ConstraintLayout
        return Util.LogItemViewHolder(view)
    }
}