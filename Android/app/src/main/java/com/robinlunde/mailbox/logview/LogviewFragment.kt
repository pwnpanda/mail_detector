package com.robinlunde.mailbox.logview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.robinlunde.mailbox.*
import com.robinlunde.mailbox.databinding.FragmentLogviewBinding

class LogviewFragment: Fragment() {
    private val model: PostViewModel by viewModels()
    private val util = MailboxApp.getUtil()
    private lateinit var binding: FragmentLogviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable menu buttons in this fragment
        setHasOptionsMenu(true);

        // Update UI if new data
        val postObserver = Observer<MutableList<PostLogEntry>> {
            newData -> Log.d("Observer", newData.toString())//do something with new data
            // Update correct view with new data
            binding.postEntries.adapter?.notifyDataSetChanged()
        }
        model.mutablePostEntries.observe(this, postObserver)
    }


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentLogviewBinding>(inflater, R.layout.fragment_logview, container, false)
        this.binding = binding
        val adapter = model.getPostEntries().value?.let { PostAdapter(it) }
        binding.postEntries.adapter = adapter
        binding.postEntries.layoutManager = LinearLayoutManager(context)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logo -> {
                val username = MailboxApp.getUsername()
                findNavController(this).navigate(LogviewFragmentDirections.actionLogviewFragmentToAlertFragment(username))
                util.logButtonPress("Logview - logo")
                true
            }
            R.id.logs -> {
                // Do nothing, in correct view
                util.logButtonPress("Logview - logs")
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}