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
import com.jjoe64.graphview.LegendRenderer
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
    private val logTag = "DebugFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        util = MailboxApp.getUtil()
        // need a central store in mailbox app to update data for view model
        MailboxApp.setDebugViewModel(model)
        // Update UI if new data
        val statusObserver = Observer<MutableList<Double>> { newData ->
            Log.d(logTag, newData.toString())
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

    private fun updateFragment(newData: MutableList<Double>) {
        /**
         * Add RSSI stats
         * Add Multiple screens for more debugging?
         */
        val graph: GraphView = binding.debugGraph

        val data: LineGraphSeries<DataPoint> = LineGraphSeries<DataPoint>()
        // Automatically sorted as first entry has lowest x value!
        for (i in newData.indices) {
            Log.d(logTag, "Index: $i - Value: ${newData[i]}")
            data.appendData(DataPoint(i.toDouble(), newData[i]), true, 100)
        }

        // Set data name
        data.title = "Sensor data"
        // Show data points and dots, and draw line
        data.isDrawDataPoints = true
        data.dataPointsRadius = 20F
        data.isDrawAsPath = true
        // Title for the x-axis and y-axis label
        graph.gridLabelRenderer.verticalAxisTitle = "Sensor data"
        graph.gridLabelRenderer.horizontalAxisTitle = "Seconds"

        // Set max values for graph
        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.setMaxY(1.1)
        graph.viewport.setMinX(0.0)

        // Show legend at top - Set title
        graph.title = "LDR Sensor data"
        graph.titleTextSize = 72F
        graph.legendRenderer.isVisible = true
        graph.legendRenderer.align = LegendRenderer.LegendAlign.TOP

        graph.addSeries(data)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
                // Go to logview (now named PostView)
                NavHostFragment.findNavController(this)
                    .navigate(DebugFragmentDirections.actionDebugFragmentToLogviewFragment())
                true
            }

            R.id.bluetooth -> {
                util.logButtonPress("Debug - bt")
                // Do nothing, we are in this view
                super.onOptionsItemSelected(item)
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}