package com.robinlunde.mailbox.alert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?,  savedInstanceState: Bundle? ): View? {
        val binding = DataBindingUtil.inflate<FragmentAlertBinding>(inflater, R.layout.fragment_alert, container, false)

        // Get username from last intent
        val args = AlertFragmentArgs.fromBundle(requireArguments())
        val username = args.username
        //Log.d("Username in Alert", username)

        // Sense button presses
        binding.clearNotifyBtn.setOnClickListener{ view: View ->
            // TODO - Move to generic function!
            binding.clearNotifyBtn.visibility = View.INVISIBLE
            container!!.rootView.findViewById<ImageView>(R.id.post_box).visibility = View.VISIBLE
            container.rootView.findViewById<TextView>(R.id.timestamp_text).text = "No new post detected!"
            container.rootView.findViewById<TextView>(R.id.timestamp_day).visibility = View.INVISIBLE
            container.rootView.findViewById<TextView>(R.id.timestamp_time).text = "Have a nice day!"
        }
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AlertFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    /*
    private fun trySendDataWeb(timestamp: String) {
        // Do async thread with network request
        var sent: Boolean = false
        var tries: Int = 1
        do {
            // Create thread
            val thread = Thread {
                // Try to send webrequest
                try {
                    Util.httpReq?.sendDataWeb(timestamp).also {
                        if (it != null) {
                            sent = it
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Log.d("Thread", "Sleeping")
            // 5 seconds
            var base: Double = 5000.0
            // Exponentially increase wait time between tries
            var time: Double = base.pow(tries)
            // Sleep
            Thread.sleep(time.toLong())
            // Log try
            Log.d("Thread", "Trying transmission $tries / 6")
            // Start above thread
            thread.start()
            // Increase try counter
            tries++
            // Check for giving up
            if (tries >= 7) {
                sent = true
                Log.d("Thread", "Tried 6 transmissions but failed - Giving up! ")
                val toast = Toast.makeText(applicationContext, "Failed to save timestamp! Giving up!", Toast.LENGTH_LONG)
                // Show toast
                toast.show()
            }
        } while (!sent)
    }*/
}