package com.robinlunde.mailbox.logview

import android.os.Bundle
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
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.databinding.FragmentLogviewBinding
import com.robinlunde.mailbox.datamodel.PostLogEntry

class PostViewFragment : Fragment() {
    private val model: PostViewModel by viewModels()
    private val util = MailboxApp.getUtil()
    private lateinit var binding: FragmentLogviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // store model
        MailboxApp.setPostModel(model)
        // Enable menu buttons in this fragment
        setHasOptionsMenu(true)

        // Update UI if new data
        val postObserver = Observer<MutableList<PostLogEntry>> { newData ->
            //Log.d("Observer - PostView", newData.toString())
            // do something with new data
            // Update correct view with new data
            binding.postEntries.adapter = PostAdapter(newData)
            binding.postEntries.layoutManager = LinearLayoutManager(context)
            // Tel view it has changed
            binding.postEntries.adapter?.notifyDataSetChanged()
        }
        model.mutablePostEntries.observe(this, postObserver)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentLogviewBinding>(
            inflater,
            R.layout.fragment_logview,
            container,
            false
        )
        this.binding = binding
        val adapter = model.mutablePostEntries.value?.let { PostAdapter(it) }
        binding.postEntries.adapter = adapter
        binding.postEntries.layoutManager = LinearLayoutManager(context)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logo -> {
                // Try to fetch new data, if we fail we don't care
                util.tryRequest(getString(R.string.get_last_status_update_method), null, null, null)
                //Move to Alert fragment
                findNavController(this).navigate(
                    PostViewFragmentDirections.actionLogviewFragmentToAlertFragment()
                )
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