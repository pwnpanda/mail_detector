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
import com.robinlunde.mailbox.databinding.FragmentPillUpdateBinding
import com.robinlunde.mailbox.datamodel.pill.Pill

// Update circle-ui to reflect current status
// Update alarm-setting logic (more / less pills needed for all to be taken)

class PillUpdateFragment : Fragment() {
    private lateinit var binding: FragmentPillUpdateBinding
    private val util: Util = MailboxApp.getUtil()
    val logTag = "PillUpdateFragment -"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Include top menu
        setHasOptionsMenu(true)

        // Watch data
        // TODO
        val observer = Observer<MutableList<Pill>> { newData ->
            binding.pillUpdateEntries.adapter = PillUpdateAdapter(newData, util, context)
            // Update layout manager
            binding.pillUpdateEntries.layoutManager = LinearLayoutManager(context)
            // Notify new data at end
            binding.pillUpdateEntries.adapter?.notifyDataSetChanged()
            // TODO make logic for changing and inserting data. This is not good enough and is wrong, but works ad hoc
            /**
             * val curSize = binding.pillLogEntries.adapter?.itemCount!!
             * val newItems = util.pillrepo.data.value?.size!!
             * if (curSize < newItems) binding.pillLogEntries.adapter?.notifyItemRangeInserted(curSize, newItems-curSize)
             */

        }
        util.pillrepo.data.observe(this, observer)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_pill_update,
            container,
            false
        )

        // If not logged in, jump to login view
        if (util.user == null)  util.moveToLoginFragment("pillUpdate",this)

        val adapter = util.pillrepo.data.value?.let { PillUpdateAdapter(it, util, context) }
        binding.pillUpdateEntries.adapter = adapter
        util.pillUpdateAdapter = adapter!!
        binding.pillUpdateEntries.layoutManager = LinearLayoutManager(context)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.alert -> {
                util.logButtonPress("PillUpdate - logo")
                NavHostFragment.findNavController(this)
                    .navigate(PillUpdateFragmentDirections.actionPillUpdateFragmentToAlertFragment())
                true
            }

            R.id.logs -> {
                util.logButtonPress("PillUpdate - logs")
                NavHostFragment.findNavController(this)
                    .navigate(PillUpdateFragmentDirections.actionPillUpdateFragmentToLogviewFragment())
                true
            }

            R.id.bluetooth -> {
                util.logButtonPress("PillUpdate - bt")
                if (MailboxApp.getClickCounter() >= 3) NavHostFragment.findNavController(this)
                    .navigate(PillUpdateFragmentDirections.actionPillUpdateFragmentToDebugFragment())
                super.onOptionsItemSelected(item)
            }

            R.id.pill -> {
                util.logButtonPress("PillUpdate - pill")
                NavHostFragment.findNavController(this)
                    .navigate(PillUpdateFragmentDirections.actionPillUpdateFragmentToPillFragment())
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}