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

// use this and migrate to calendar? https://github.com/kizitonwose/CalendarView

class PillLogFragment : Fragment() {
    private lateinit var binding: FragmentPillLogBinding
    private val util: Util = MailboxApp.getUtil()
    val logTag = "PillLogFragment -"

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

        val adapter = util.recordrepo.data.value?.let { PillLogAdapter(it, util, binding) }
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