package com.robinlunde.mailbox.alert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.databinding.FragmentAlertBinding

// TODO: Rename parameter arguments, choose names that matchT
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlertFragment : Fragment() {
    val util = MailboxApp.getUtil()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable menu buttons in this fragment
        setHasOptionsMenu(true);
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?,  savedInstanceState: Bundle? ): View? {
        val binding = DataBindingUtil.inflate<FragmentAlertBinding>(inflater, R.layout.fragment_alert, container, false)
        // Get username from last intent
        val args = AlertFragmentArgs.fromBundle(requireArguments())
        val username = args.username
        //Log.d("Username in Alert", username)
        // TODO need data signalling about new post
        // Observable.onChange(setNotificationValue(timestamp))
        // setNoResults(container, binding)

        // Sense button presses
        binding.clearNotifyBtn.setOnClickListener{ view: View ->
            setNoResults(container, binding)
            Toast.makeText(context, "New post data cleared!", Toast.LENGTH_SHORT).show()
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
                NavHostFragment.findNavController(this).navigate(AlertFragmentDirections.actionAlertFragmentToLogviewFragment())
                util.logButtonPress("Alert - logs")
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun setNoResults(container: ViewGroup?, binding: FragmentAlertBinding) {
        // Clear fragment data
        binding.clearNotifyBtn.visibility = View.INVISIBLE
        container!!.rootView.findViewById<ImageView>(R.id.post_box).visibility = View.VISIBLE
        container.rootView.findViewById<TextView>(R.id.timestamp_text).text = getString(R.string.no_new_post_message)
        container.rootView.findViewById<TextView>(R.id.timestamp_time).text = getString(R.string.nice_day_message)
    }

    private fun setNotificationValue(timeStamp: String, binding: FragmentAlertBinding, container: ViewGroup?){
        binding.clearNotifyBtn.visibility = View.VISIBLE
        container!!.rootView.findViewById<ImageView>(R.id.post_box).visibility = View.INVISIBLE
        container.rootView.findViewById<TextView>(R.id.timestamp_text).text = getString(R.string.timestamp_text)
        container.rootView.findViewById<TextView>(R.id.timestamp_time).text =  util.getMyTime(timeStamp)
        container.rootView.findViewById<TextView>(R.id.timestamp_day).text = util.getMyDate(timeStamp)

    }
}