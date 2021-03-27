package com.robinlunde.mailbox.alert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
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

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?,  savedInstanceState: Bundle? ): View? {
        val binding = DataBindingUtil.inflate<FragmentAlertBinding>(inflater, R.layout.fragment_alert, container, false)

        // Get username from last intent
        val args = AlertFragmentArgs.fromBundle(requireArguments())
        val username = args.username
        //Log.d("Username in Alert", username)

        // Sense button presses
        binding.clearNotifyBtn.setOnClickListener{ view: View ->
            // TODO This needs to be base case - swap case is if there is new data
            binding.clearNotifyBtn.visibility = View.INVISIBLE
            container!!.rootView.findViewById<ImageView>(R.id.post_box).visibility = View.VISIBLE
            container.rootView.findViewById<TextView>(R.id.timestamp_text).text = "No new post detected!"
            container.rootView.findViewById<TextView>(R.id.timestamp_time).text = "Have a nice day!"
            // TODO: put timestamp of last check
            // TODO checkTime / Timestamp should be LiveData instance
            // TODO Replace with actual data
            // TODO val checkTime = getLastBTComm()
            val checkTime = "12.12.12"
            container.rootView.findViewById<TextView>(R.id.timestamp_day).text = "Last check: kl.${checkTime}"
            Toast.makeText(context, "New post data cleared!", Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }
}