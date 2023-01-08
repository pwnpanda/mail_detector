package com.robinlunde.mailbox.pills

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.databinding.FragmentPillLogBinding
import com.robinlunde.mailbox.datamodel.pill.Record
import kotlinx.coroutines.*
import timber.log.Timber

class PillLogAdapter(
    private val dataEntries: MutableList<Record>,
    val util: Util,
    val binding: FragmentPillLogBinding
) :
    RecyclerView.Adapter<Util.RecordItemViewHolder>() {

    override fun getItemCount(): Int = dataEntries.size

    override fun onBindViewHolder(holder: Util.RecordItemViewHolder, position: Int) {
        // Render data if any
        if (itemCount > 0) {
            binding.pillLogEntries.visibility = View.VISIBLE
            binding.pillLogs.visibility = View.INVISIBLE

            val record = dataEntries[position]
            // Need to get color as correct value
            holder.constraintLayout.setBackgroundColor(record.pill!!.color)

            // Set content of each UI element
            holder.constraintLayout.findViewById<TextView>(R.id.pill_name).text =
                record.pill.getPillName()
            holder.constraintLayout.findViewById<TextView>(R.id.pill_date).text =
                record.day!!.today
            holder.constraintLayout.findViewById<TextView>(R.id.pill_taken).text =
                record.taken.toString()

            holder.constraintLayout.findViewById<ImageButton>(R.id.pill_delete_history_button)
                .setOnClickListener {
                    val errorHandler = CoroutineExceptionHandler { _, exception ->
                        Timber.d("Received error: ${exception.message} !")
                        Timber.e("Trace: ${exception.printStackTrace()} !")
                        Toast.makeText(
                            MailboxApp.getContext(),
                            "Failed to delete data $record!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
                    Timber.d("Trying to delete Record $record")
                    coroutineScope.launch(errorHandler) {
                        util.recordrepo.deleteRecord(rec_id = record.id!!)
                    }
                }
        } else {
            binding.pillLogEntries.visibility = View.INVISIBLE
            binding.pillLogs.visibility = View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Util.RecordItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(
            R.layout.recyclerview_row_records,
            parent,
            false
        ) as ConstraintLayout
        return Util.RecordItemViewHolder(view)
    }
}