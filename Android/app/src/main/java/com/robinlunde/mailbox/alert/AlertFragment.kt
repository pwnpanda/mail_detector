package com.robinlunde.mailbox.alert

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.databinding.FragmentAlertBinding
import com.robinlunde.mailbox.datamodel.PostUpdateStatus
import com.robinlunde.mailbox.logview.PostViewFragmentDirections

class AlertFragment : Fragment() {
    private lateinit var util: Util
    private lateinit var binding: FragmentAlertBinding
    private val model: AlertViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable menu buttons in this fragment
        setHasOptionsMenu(true)
        MailboxApp.setAlertModel(model)
        util = MailboxApp.getUtil()

        // Update UI if new data
        val statusObserver = Observer<PostUpdateStatus> { newData ->
            Log.d("Observer - Alert", newData.toString())
            // do something with new data
            updateFragment(newData)

            // Update correct view with new data
            /*binding.status.adapter = AlertAdapter(newData)
            binding.status.layoutManager = LinearLayoutManager(context)
            // Tel view it has changed
            binding.status.adapter?.notifyDataSetChanged()*/
        }
        model.currentStatus.observe(this, statusObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_alert,
            container,
            false
        )
        // set adapter
        // val adapter = model.getStatus().value?.let { AlertAdapter(it) }
        binding.lifecycleOwner = viewLifecycleOwner

        // This happens every time the fragment is re-rendered, which is perfect
        // Incl. pressing of notifications

        val status = model.currentStatus.value!!
        // Update fragment based on status
        updateFragment(status)

        // Sense button presses
        binding.clearNotifyBtn.setOnClickListener {
            Toast.makeText(context, "Trying to register post pickup!", Toast.LENGTH_SHORT).show()
            // Ack to device
            // val res = util.sendBTPostPickupAck()
            // if (res) {
            noNewMail(status)
            // } else {
            //  Toast.makeText(context, "Could not acknowledge post pickup over BT!", Toast.LENGTH_SHORT).show()
            // }

            val timestamp = model.currentStatus.value!!.timestamp
            // Try to log to web
            val request1: Boolean =
                util.tryRequest(getString(R.string.sendLogsMethod), timestamp, null, null)
            if (!request1) makeToast("Could not register post pickup over Web!")
            else makeToast("Post pickup registered!")

            val request2: Boolean = util.tryRequest(
                getString(R.string.set_last_status_update_method), null, null, newMail = false
            )
            if (!request2) makeToast("Could not send latest Status Update over web")
            else makeToast("New status registered!")

        }

        return binding.root
    }

    private fun makeToast(msg: String) {
        Toast.makeText(
            context, msg, Toast.LENGTH_SHORT
        ).show()
    }

    // Updates fragment with new data from Status API
    private fun updateFragment(status: PostUpdateStatus) {
        if (status.newMail) {
            newMail(status)
        } else {
            noNewMail(status)
        }
    }

    // Remove data and set base case
    private fun noNewMail(
        status: PostUpdateStatus
    ) {
        Log.d("AlertFragment", "No new mail")
        // Clear fragment data
        binding.clearNotifyBtn.visibility = View.INVISIBLE
        binding.timestampTime.visibility = View.INVISIBLE
        binding.postBox.visibility = View.VISIBLE
        binding.status.visibility = View.VISIBLE
        // If you got the last message from the BT device
        when (status.username) {
            MailboxApp.getUsername() -> binding.status.text =
                HtmlCompat.fromHtml(
                    getString(
                        R.string.last_update_not_new_string,
                        status.time,
                        status.date,
                        "You"
                    ), HtmlCompat.FROM_HTML_MODE_COMPACT
                )
            // If the username is set to the one indicating no data is available yet!
            getString(R.string.no_status_yet_username) -> binding.status.text =
                getString(R.string.no_status_yet_string)

            // If someone else got the last message from the BT device
            else -> binding.status.text =
                HtmlCompat.fromHtml(
                    getString(
                        R.string.last_update_not_new_string,
                        status.time,
                        status.date,
                        status.username
                    ), HtmlCompat.FROM_HTML_MODE_COMPACT
                )
        }

        binding.timestampText.text = getString(R.string.no_new_post_message)
        binding.timestampDay.text = getString(R.string.nice_day_message)
    }

    // Got new mail! Update data and set relevant information
    private fun newMail(
        status: PostUpdateStatus
    ) {
        Log.d("AlertFragment", "New mail!")
        binding.clearNotifyBtn.visibility = View.VISIBLE
        binding.timestampTime.visibility = View.VISIBLE
        // If you got the last message from the BT device
        if (status.username == MailboxApp.getUsername()) binding.status.text =
            getString(R.string.last_update_new_string, "You")
        // If someone else got the last message from the BT device
        else binding.status.text =
            getString(R.string.last_update_new_string, status.username)

        binding.postBox.visibility = View.INVISIBLE
        binding.timestampText.text = getString(R.string.timestamp_text)
        binding.timestampTime.text = status.time
        binding.timestampDay.text = status.date
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when (item.itemId) {

            R.id.logo -> {
                // Do nothing, we are in correct view
                util.logButtonPress("Alert - logo")
                true
            }

            R.id.logs -> {
                util.logButtonPress("Alert - logs")
                // Try to fetch data to update logview - if we fail, we don't care
                util.tryRequest(getString(R.string.get_logs), null, null, null)
                // Go to logview (noew named PostView
                NavHostFragment.findNavController(this)
                    .navigate(AlertFragmentDirections.actionAlertFragmentToLogviewFragment())
                true
            }

            R.id.bluetooth -> {
                util.logButtonPress("Alert - bt")
                // Move to debug view
                if (MailboxApp.getClickCounter() >= 3)  NavHostFragment.findNavController(this)
                    .navigate(AlertFragmentDirections.actionAlertFragmentToDebugFragment())
                true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}