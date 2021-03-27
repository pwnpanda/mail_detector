package com.robinlunde.mailbox.logview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.robinlunde.mailbox.*
import com.robinlunde.mailbox.databinding.FragmentLogviewBinding
/*
// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
*/
/**
 * A simple [Fragment] subclass.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LogviewFragment: Fragment() {
    /*// TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }*/

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentLogviewBinding>(inflater, R.layout.fragment_logview, container, false)
        val adapter = PostAdapter(MailboxApp.getPostEntries())
        binding.postEntries.adapter = adapter

        binding.postEntries.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    /*
    //  Create adapter and click handler for recycler view                                       This gives position of clicked item
    binding.postEntries.adapter = PostRecyclerViewAdapter(dataParsed, this){ position: Int ->
        Log.e("List clicked", "Clicked on item at position $position")
    }*/

/*
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
    }*/
}