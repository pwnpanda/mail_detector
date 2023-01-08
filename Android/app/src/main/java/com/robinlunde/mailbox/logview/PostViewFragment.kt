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
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.databinding.FragmentLogviewBinding
import com.robinlunde.mailbox.datamodel.PostLogEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
            binding.postEntries.adapter = PostAdapter(newData, binding)
            binding.postEntries.layoutManager = LinearLayoutManager(context)
            // Tell view it has changed
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

        // If not logged in, jump to login view
        if (util.user == null)  util.moveToLoginFragment("postView",this)

        this.binding = binding
        val adapter = model.mutablePostEntries.value?.let { PostAdapter(it, binding) }
        binding.postEntries.adapter = adapter
        binding.postEntries.layoutManager = LinearLayoutManager(context)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.alert -> {
                util.logButtonPress("Logview - logo")
                // Try to fetch new data, if we fail we don't care
                CoroutineScope(Dispatchers.IO + Job()).launch {
                    util.doNetworkRequest(
                        getString(R.string.get_last_status_update_method),
                        null,
                        null,
                        null
                    ).await()
                }
                //Move to Alert fragment
                findNavController(this).navigate(
                    PostViewFragmentDirections.actionLogviewFragmentToAlertFragment()
                )

                true
            }
            R.id.logs -> {
                // Do nothing, in correct view
                util.logButtonPress("Logview - logs")
                true
            }

            R.id.bluetooth -> {
                util.logButtonPress("Logview - bt")
                // Move to debug view
                if (MailboxApp.getClickCounter() >= 3) findNavController(this).navigate(
                    PostViewFragmentDirections.actionLogviewFragmentToDebugFragment()
                )
                super.onOptionsItemSelected(item)
                //true
            }

            R.id.pill -> {
                util.logButtonPress("Logview - pill")
                // Move to pill view
                findNavController(this).navigate(PostViewFragmentDirections.actionLogviewFragmentToPillFragment())
                true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}