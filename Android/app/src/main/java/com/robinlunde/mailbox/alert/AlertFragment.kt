package com.robinlunde.mailbox.alert

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.databinding.FragmentAlertBinding

class AlertFragment : Fragment() {
    val util = MailboxApp.getUtil()
    // private lateinit val timeStamp: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable menu buttons in this fragment
        setHasOptionsMenu(true)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentAlertBinding>(
            inflater,
            R.layout.fragment_alert,
            container,
            false
        )
        // TODO need data signalling about new post / other way to update
        // Observable.onChange(setNotificationValue(timestamp))
        // TODO remove - only temporary
        val timeStamp = "12.12.12"
        // Sense button presses
        binding.clearNotifyBtn.setOnClickListener { view: View ->
            Toast.makeText(context, "Trying to register post pickup!", Toast.LENGTH_SHORT).show()
            // val res = util.sendBTPostPickupAck()
            // if (res) {
            setNoResults(container, binding, timeStamp)
            // } else {
            //  Toast.makeText(context, "Could not acknowledge post pickup over BT!", Toast.LENGTH_SHORT).show()
            // }
        }
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logo -> {
                // Do nothing, we are in correct view
                util.logButtonPress("Alert - logo")
                true
            }

            R.id.logs -> {
                // go to logview
                NavHostFragment.findNavController(this)
                    .navigate(AlertFragmentDirections.actionAlertFragmentToLogviewFragment())
                util.logButtonPress("Alert - logs")
                true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    // Remove data and set basecase
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setNoResults(
        container: ViewGroup?,
        binding: FragmentAlertBinding,
        timeStamp: String
    ): Boolean {
        // Clear fragment data
        binding.clearNotifyBtn.visibility = View.INVISIBLE
        container!!.rootView.findViewById<ImageView>(R.id.post_box).visibility = View.VISIBLE
        container.rootView.findViewById<TextView>(R.id.timestamp_text).text =
            getString(R.string.no_new_post_message)
        container.rootView.findViewById<TextView>(R.id.timestamp_time).text =
            getString(R.string.nice_day_message)
        // Try to log to web
        val request: Boolean =
            util.tryRequest(getString(R.string.deleteLogsMethod), timeStamp, null)
        if (!request) {
            Toast.makeText(context, "Could not register post pickup over Web!", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(context, "Post pickup registered!", Toast.LENGTH_SHORT).show()
        }
        return request
    }

    // Update data and set relevant information
    private fun setNotificationValue(
        timeStamp: String,
        binding: FragmentAlertBinding,
        container: ViewGroup?
    ) {
        binding.clearNotifyBtn.visibility = View.VISIBLE
        container!!.rootView.findViewById<ImageView>(R.id.post_box).visibility = View.INVISIBLE
        container.rootView.findViewById<TextView>(R.id.timestamp_text).text =
            getString(R.string.timestamp_text)
        container.rootView.findViewById<TextView>(R.id.timestamp_time).text =
            util.getMyTime(timeStamp)
        container.rootView.findViewById<TextView>(R.id.timestamp_day).text =
            util.getMyDate(timeStamp)
    }
}