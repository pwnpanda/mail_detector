package com.robinlunde.mailbox.debug

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
import androidx.navigation.fragment.NavHostFragment
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.databinding.FragmentDebugBinding


class DebugFragment : Fragment() {
    private lateinit var util: Util
    private lateinit var binding: FragmentDebugBinding
    private val model: DebugViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        util = MailboxApp.getUtil()
        // need a central store in mailboxapp to update data for view model
        MailboxApp.setDebugViewModel(model)
        // Update UI if new data
        val statusObserver = Observer<MutableList<Float>> { newData ->
            Log.d("Observer - Debug", newData.toString())
            // do something with new data
            updateFragment(newData)
        }
        model.sensorData.observe(this, statusObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_debug,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner

        val sensorData = model.sensorData.value!!
        updateFragment(sensorData)

        return binding.root
    }

    private fun updateFragment(newData: MutableList<Float>) {
        var graph: GraphView = binding.debugGraph
        val data: LineGraphSeries<DataPoint> = LineGraphSeries<DataPoint>()
        // Automatically sorted as first entry has lowest x value!
        for (ent in newData) {
            data.appendData(DataPoint(newData.indexOf(ent).toDouble(), ent.toDouble()), true, 100)
        }
        graph.addSeries(data)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when (item.itemId) {

            R.id.logo -> {
                // Do nothing, we are in correct view
                util.logButtonPress("Debug - logo")
                NavHostFragment.findNavController(this)
                    .navigate(DebugFragmentDirections.actionDebugFragmentToAlertFragment())
                true
            }

            R.id.logs -> {
                util.logButtonPress("Debug - logs")
                // Try to fetch data to update logview - if we fail, we don't care
                util.tryRequest(getString(R.string.get_logs), null, null, null)
                // Go to logview (noew named PostView
                NavHostFragment.findNavController(this)
                    .navigate(DebugFragmentDirections.actionDebugFragmentToLogviewFragment())
                true
            }

            R.id.bluetooth -> {
                util.logButtonPress("Debug - bt")
                // Do nothing, we are in this view
                true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}