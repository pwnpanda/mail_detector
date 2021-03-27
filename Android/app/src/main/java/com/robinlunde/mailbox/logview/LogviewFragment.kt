package com.robinlunde.mailbox.logview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.robinlunde.mailbox.*
import com.robinlunde.mailbox.databinding.FragmentLogviewBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LogviewFragment: Fragment() {
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

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentLogviewBinding>(inflater, R.layout.fragment_logview, container, false)
        //binding.set(PostEntry)
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
            LogviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    /*
    private fun showLogs(): Boolean {
        // Show a different view!
        setContentView(R.layout.activity_log)
        val res = runBlocking{
            // Create thread
            var res = ""
            val thread = Thread {
                // Try to send webrequest
                try {
                    httpReq?.getDataWeb().also {
                        if (it != null) {
                            res = it
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            thread.start()
            thread.join()
            return@runBlocking res
        }
        Log.d("test", "res")
        if (res != "") {
            renderRecyclerView(res)
        } else {
            // Set error message in activity_log!
            findViewById<RecyclerView>(R.id.post_entries).visibility = View.INVISIBLE
            var error = findViewById<TextView>(R.id.error_logs)
            error.visibility = View.VISIBLE
        }
        return true
    }

    // Move to fragment?
    private fun renderRecyclerView(data: String): Boolean {
        // Show a different view!
        setContentView(R.layout.activity_log)

        // data to populate the RecyclerView with
        // Convert data to ArrayList using jackson
        val mapper = jacksonObjectMapper()
        val dataParsed: List<PostEntry> = mapper.readValue(data)
        Log.e("Data", dataParsed.toString())

        val adapter = PostAdapter()
        binding.dataParsed.adapter = adapter
        // --------------------------------

        // set up the RecyclerView
        val postEntries = findViewById<RecyclerView>(R.id.post_entries)
        postEntries.layoutManager = LinearLayoutManager(this)




        //  Create adapter and click handler for recycler view                                       This gives position of clicked item
        postEntries.adapter = PostRecyclerViewAdapter(dataParsed, this){ position: Int ->
            Log.e("List clicked", "Clicked on item at position $position")
        }

        return true
    }*/
}