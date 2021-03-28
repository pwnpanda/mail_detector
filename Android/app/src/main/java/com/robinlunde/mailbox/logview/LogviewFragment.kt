package com.robinlunde.mailbox.logview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.robinlunde.mailbox.*
import com.robinlunde.mailbox.databinding.FragmentLogviewBinding

class LogviewFragment: Fragment() {
    //private val model: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable menu buttons in this fragment
        setHasOptionsMenu(true);

      /*  // Update UI if new data
        val postObserver = Observer<MutableList<PostLogEntry>> {
            newData -> Log.d("Observer", newData.toString())//do something with new data
            // Update correct view with new data
        }
        MailboxApp.setPostViewModel(model)
        model.postEntries.observe(this, postObserver)
        /*arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }*/ */
    }


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentLogviewBinding>(inflater, R.layout.fragment_logview, container, false)
        val adapter = PostAdapter(MailboxApp.getPostEntries())
        binding.postEntries.adapter = adapter
        //val viewModel = ViewModelProvider(this, )
        binding.postEntries.layoutManager = LinearLayoutManager(context)
        return binding.root
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logo -> {
                val username = MailboxApp.getUsername()
                findNavController(this).navigate(LogviewFragmentDirections.actionLogviewFragmentToAlertFragment(username))
                MailboxApp.getUtil().logButtonPress("Logview - logo")
                true
            }
            R.id.logs -> {
                // Do nothing, in correct view
                MailboxApp.getUtil().logButtonPress("Logview - logs")
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}