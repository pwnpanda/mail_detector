package com.robinlunde.mailbox.pills

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.datamodel.pill.Pill
import kotlinx.coroutines.*

class PillUpdateAdapter (private val dataEntries: MutableList<Pill>, val util: Util) :
RecyclerView.Adapter<Util.PillItemViewHolder>() {
    val logTag = "PillUpdateAdapter -"

    override fun getItemCount(): Int = dataEntries.size

    override fun onBindViewHolder(holder: Util.PillItemViewHolder, position: Int) {
        // Render data if any
        if (itemCount > 0) {
            holder.constraintLayout.findViewById<RecyclerView>(R.id.pill_log_entries).visibility =
                View.VISIBLE
            holder.constraintLayout.findViewById<TextView>(R.id.pill_logs).visibility =
                View.INVISIBLE

            val pill = dataEntries[position]

            val errorHandler = CoroutineExceptionHandler { _, exception ->
                Log.d("$logTag onBindViewHolder", "Received error: ${exception.message}!")
                Log.e("$logTag onBindViewHolder", "Trace: ${exception.printStackTrace()}!")
                Toast.makeText(
                    MailboxApp.getContext(),
                    "Failed to change data $pill!",
                    Toast.LENGTH_LONG
                ).show()
            }
            val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

            // Set color
            val color = holder.constraintLayout.findViewById<Button>(R.id.update_pill_color)
            color.setBackgroundColor(pill.color)
            // Set color clicklistener TODO
            color.setOnClickListener {
                // TODO open color selector
            }
            // Set UUID
            holder.constraintLayout.findViewById<TextView>(R.id.pill_uuid).text = pill.uuid.toString()
            // Set name
            holder.constraintLayout.findViewById<TextInputEditText>(R.id.update_pill_name_input)
            // Set Active clicklistener TODO
            holder.constraintLayout.findViewById<CheckBox>(R.id.active_pill).setOnClickListener {
                // TODO change value of Active
            }
            // Set update clicklistener TODO
            holder.constraintLayout.findViewById<Button>(R.id.update_pill_button).setOnClickListener {

                coroutineScope.launch(errorHandler) {
                    // TODO launch update call
                }
            }
            // Set delete clicklistener TODO
            holder.constraintLayout.findViewById<Button>(R.id.delete_pill_button).setOnClickListener {
                coroutineScope.launch(errorHandler) {
                    // TODO launch delete call
                }
            }



        } else {
            holder.constraintLayout.findViewById<RecyclerView>(R.id.pill_log_entries).visibility =
                View.INVISIBLE
            holder.constraintLayout.findViewById<TextView>(R.id.pill_logs).visibility =
                View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Util.PillItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(
            R.layout.recyclerview_row_pills,
            parent,
            false
        ) as ConstraintLayout
        return Util.PillItemViewHolder(view)
    }
}