package com.robinlunde.mailbox.debug

import android.os.Bundle
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber


class DebugFragment : Fragment() {
    private lateinit var util: Util
    private lateinit var binding: FragmentDebugBinding
    private val model: DebugViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        Timber.d("OnCreate Called for Debug!")
        util = MailboxApp.getUtil()
        // Start collecting debug data
        MailboxApp.getBTConn().requestDebugData()
        // need a central store in mailbox app to update data for view model
        MailboxApp.setDebugViewModel(model)
        // Update UI if new data
        val statusObserver = Observer<MutableList<Double>> { newData ->
            // Just ignore issue since it is for debugging!
            try {
                Timber.d(newData.toString())
                // do something with new data
                updateFragment(newData)
            } catch (e: ConcurrentModificationException) {
                Timber.d("Got data too quickly, just error out and update next time")
            } catch (e: Exception) {
                Timber.e("Got some fatal error! Crash in order to debug further")
                throw e
            }
        }
        model.sensorData.observe(this, statusObserver)

        val rssiObserver = Observer<Int> { newRSSI ->
            Timber.d("New RSSI Value: $newRSSI")
            binding.rssi.text = getString(R.string.rssiSignal, newRSSI)
        }
        model.rssi.observe(this, rssiObserver)
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

        // If not logged in, jump to login view
        if (util.user == null)  util.moveToLoginFragment("debug",this)

        val sensorData = model.sensorData.value!!
        updateFragment(sensorData)

        val RSSI = model.rssi.value!!
        binding.rssi.text = getString(R.string.rssiSignal, RSSI)

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
            Timber.d("Index: $i  - Value: $newData[i]")
            data.appendData(DataPoint(i.toDouble(), newData[i]), true, 100)
        }

        // Add scroll on more than 6 datapoints, up to 30!
        if (newData.size > 30) graph.viewport.isScalable = true

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

        graph.addSeries(data)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.alert -> {
                // Move to alert view
                util.logButtonPress("Debug - logo")

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
                    .navigate(DebugFragmentDirections.actionDebugFragmentToAlertFragment())
                true
            }

            R.id.logs -> {
                util.logButtonPress("Debug - logs")
                // Try to fetch data to update logview - if we fail, we don't care
                CoroutineScope(Dispatchers.IO + Job()).launch {
                    util.doNetworkRequest(getString(R.string.get_logs), null, null, null).await()
                }
                // Go to logview (now named PostView)
                NavHostFragment.findNavController(this)
                    .navigate(DebugFragmentDirections.actionDebugFragmentToLogviewFragment())
                true
            }

            R.id.bluetooth -> {
                util.logButtonPress("Debug - bt")
                // Do nothing, we are in this view
                // Start collecting debug data           
                if (MailboxApp.getClickCounter() >= 3) MailboxApp.getBTConn().requestDebugData()
                super.onOptionsItemSelected(item)
            }

            R.id.pill -> {
                util.logButtonPress("Debug - pill")
                // Move to pill view
                NavHostFragment.findNavController(this)
                    .navigate(DebugFragmentDirections.actionDebugFragmentToPillFragment())
                true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}