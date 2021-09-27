package com.robinlunde.mailbox.pills

import android.content.Context
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.datamodel.pill.Pill
import kotlinx.coroutines.*

class PillUpdateAdapter(
    private val dataEntries: MutableList<Pill>,
    val util: Util,
    val context: Context?
) :
RecyclerView.Adapter<Util.PillItemViewHolder>() {
    val logTag = "PillUpdateAdapter -"

    override fun getItemCount(): Int = dataEntries.size

    override fun onBindViewHolder(holder: Util.PillItemViewHolder, position: Int) {
        // Render data if any
        if (itemCount > 0) {
            // TODO this throws null object reference. Why?!? It is in the correct file
            /*holder.constraintLayout.findViewById<RecyclerView>(R.id.pill_update_entries).visibility =
                View.VISIBLE
            holder.constraintLayout.findViewById<TextView>(R.id.pill_update).visibility =
                View.INVISIBLE*/

            val adapter = this
            val pill = dataEntries[position]
            val oldPill = pill

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
            var newColor = pill.color
            // Set color clicklistener
            color.setOnClickListener {
                ColorPickerDialogBuilder
                    .with(context!!)
                    .setTitle("Choose color")
                    .initialColor(newColor)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setOnColorSelectedListener { selectedColor ->
                        newColor = selectedColor
                        Log.d("$logTag ColorSelector", "Temporary selected color $selectedColor")
                    }
                    .setPositiveButton("ok") { dialog, selectedColor, allColors ->
                        newColor = selectedColor
                        color.setBackgroundColor(selectedColor)
                        Log.d("$logTag ColorSelector", "Selected color $selectedColor")
                    }
                    .setNegativeButton("cancel") { dialog, which -> }
                    .build()
                    .show()
            }
            // Set UUID
            holder.constraintLayout.findViewById<TextView>(R.id.pill_uuid).text = pill.uuid.toString()
            // Set name
            val nameInput = holder.constraintLayout.findViewById<TextInputEditText>(R.id.update_pill_name_input)
            if (pill.name =="") pill.name = pill.getPillName()
            Log.d("$logTag onBindViewHolder","Pill name: ${pill.name}")
            nameInput.text = SpannableStringBuilder(pill.name)
            // Set Active clicklistener
            val active = holder.constraintLayout.findViewById<CheckBox>(R.id.active_pill)
            active.isChecked = pill.active
            var newChecked = pill.active
            active.setOnClickListener {
                newChecked = active.isChecked
            }
            // Set update clicklistener
            holder.constraintLayout.findViewById<AppCompatImageButton>(R.id.update_pill_button).setOnClickListener {

                coroutineScope.launch(errorHandler) {
                    pill.active = newChecked
                    pill.color = newColor
                    pill.name = nameInput.text.toString()

                    // Update shared preferences
                    val prefs = MailboxApp.getPrefs()
                    with(prefs.edit()) {
                        putString(pill.uuid!!.toString(), pill.name)
                        apply()
                    }

                    // TODO log action and data
                    Log.d("$logTag onBindViewHolder", "Updating pill to be: $pill")
                    val createdPill = util.pillrepo.updatePill(pill)
                    Log.d("$logTag onBindViewHolder", "Updated pill to be: $pill")

                    createdPill.name = oldPill.name
                    Toast.makeText(MailboxApp.getContext(),
                        "Updated pill!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            // Set delete clicklistener
            holder.constraintLayout.findViewById<AppCompatImageButton>(R.id.delete_pill_button).setOnClickListener {
                coroutineScope.launch(errorHandler) {
                    // TODO log action and data
                    util.pillrepo.deletePill(pill.id!!)
                    adapter.notifyItemRemoved(position)
                    Log.d("$logTag onBindViewHolder", "Deleted pill: $pill")
                }
            }

        } else {
            holder.constraintLayout.findViewById<RecyclerView>(R.id.pill_update_entries).visibility =
                View.INVISIBLE
            holder.constraintLayout.findViewById<TextView>(R.id.pill_update).visibility =
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