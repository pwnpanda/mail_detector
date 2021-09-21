package com.robinlunde.mailbox.pills

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.datamodel.pill.Pill
import kotlinx.coroutines.*

class PillLogAdapter(private val dataEntries: MutableList<Pill>, val util: Util) :
    RecyclerView.Adapter<Util.PillItemViewHolder>() {
    val logTag = "PillLogAdapter -"

    override fun getItemCount(): Int = dataEntries.size

    override fun onBindViewHolder(holder: Util.PillItemViewHolder, position: Int) {
        // Render data if any
        if (itemCount > 0) {
            val pill = dataEntries[position]
            // Need to get color as correct value
            holder.constraintLayout.setBackgroundColor( pill.colorRes )
            // Set content of each UI element
            holder.constraintLayout.findViewById<TextView>(R.id.pill_user)
            holder.constraintLayout.findViewById<TextView>(R.id.pill_date)
            holder.constraintLayout.findViewById<TextView>(R.id.pill_taken)

            holder.constraintLayout.findViewById<ImageButton>(R.id.pill_delete_history_button)
                .setOnClickListener {
                    val errorHandler = CoroutineExceptionHandler { _, exception ->
                        Log.d("$logTag onBindViewHolder", "Received error: ${exception.message}!")
                        Log.e("$logTag onBindViewHolder", "Trace: ${exception.printStackTrace()}!")
                        Toast.makeText(
                            MailboxApp.getContext(),
                            "Failed to delete data $pill!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
                    coroutineScope.launch(errorHandler) {
                        util.pillrepo.deletePill(pill_id = pill.id!!)
                    }
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Util.PillItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(
            R.layout.recyclerview_row_pill,
            parent,
            false
        ) as ConstraintLayout
        return Util.PillItemViewHolder(view)
    }
}