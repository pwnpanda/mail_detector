package com.robinlunde.mailbox.pills

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.databinding.FragmentPillLogBinding
import com.robinlunde.mailbox.datamodel.pill.Record
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

// use this and migrate to calendar? https://github.com/kizitonwose/CalendarView

class PillLogFragment : Fragment() {
    private lateinit var binding: FragmentPillLogBinding
    private val util: Util = MailboxApp.getUtil()
    private var first = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Include top menu
        setHasOptionsMenu(true)

        // Watch data
        val observer = Observer<MutableList<Record>> { newData ->
            // Update adapter
            binding.pillLogEntries.adapter = PillLogAdapter(newData, util, binding)
            // Update layout manager
            binding.pillLogEntries.layoutManager = LinearLayoutManager(context)

            // Strange hack as it is called twice, but works
            if (first++ < 2) {
                newData.reverse()
            }

            // Notify new data at end
            binding.pillLogEntries.adapter?.notifyDataSetChanged()
            // TODO make logic for changing and inserting data. This is not good enough and is wrong, but works ad hoc
            /**
             * val curSize = binding.pillLogEntries.adapter?.itemCount!!
             * val newItems = util.pillrepo.data.value?.size!!
             * if (curSize < newItems) binding.pillLogEntries.adapter?.notifyItemRangeInserted(curSize, newItems-curSize)
             */
        }

        // Update UI if new data!
        util.recordrepo.data.observe(this, observer)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_pill_log,
            container,
            false
        )

        // If not logged in, jump to login view
        if (util.user == null)  util.moveToLoginFragment("pillLog",this)

        binding.pillScreenTitle2.setOnClickListener{
            Timber.d("Text clicked to invert sorting")
            // Sort if set
            val newData = util.recordrepo.data.value!!
            // Sort newest first
            newData.reverse()
            util.recordrepo.data.postValue(newData)
            // Notify new data at end
            binding.pillLogEntries.adapter?.notifyDataSetChanged()
        }

        val adapter = util.recordrepo.data.value?.let { PillLogAdapter( it, util, binding) }
        binding.pillLogEntries.adapter = adapter
        util.pillLogAdapter = adapter!!
        binding.pillLogEntries.layoutManager = LinearLayoutManager(context)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.alert -> {
                util.logButtonPress("PillLog - logo")

                // Try to fetch new data, if we fail we don't care
                CoroutineScope(Dispatchers.IO + Job()).launch {
                    util.doNetworkRequest(
                        getString(R.string.get_last_status_update_method),
                        null,
                        null,
                        null
                    ).await()
                }

                NavHostFragment.findNavController(this)
                    .navigate(PillLogFragmentDirections.actionPillLogFragmentToAlertFragment())
                true
            }

            R.id.logs -> {
                util.logButtonPress("PillLog - logs")
                NavHostFragment.findNavController(this)
                    .navigate(PillLogFragmentDirections.actionPillLogFragmentToLogviewFragment())
                true
            }

            R.id.bluetooth -> {
                util.logButtonPress("PillLog - bt")
                if (MailboxApp.getClickCounter() >= 3) NavHostFragment.findNavController(this)
                    .navigate(PillLogFragmentDirections.actionPillLogFragmentToDebugFragment())
                super.onOptionsItemSelected(item)
            }

            R.id.pill -> {
                util.logButtonPress("PillLog - pill")
                NavHostFragment.findNavController(this)
                    .navigate(PillLogFragmentDirections.actionPillLogFragmentToPillFragment())
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}