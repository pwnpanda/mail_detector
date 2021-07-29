package com.robinlunde.mailbox.pills

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.databinding.FragmentPillBinding

class PillFragment : Fragment() {
    private lateinit var binding: FragmentPillBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_pill,
            container,
            false
        )

        // Set 24 hour display
        binding.setAlarm.setIs24HourView(true);

        return super.onCreateView(inflater, container, savedInstanceState)



    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}
