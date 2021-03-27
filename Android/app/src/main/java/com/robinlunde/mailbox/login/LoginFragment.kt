package com.robinlunde.mailbox.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.databinding.FragmentLoginBinding


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = DataBindingUtil.inflate<FragmentLoginBinding>(inflater, R.layout.fragment_login, container, false)
        val username = MailboxApp.getUsername()
        if (username != "") {
            // TODO Need to do this, but it throws error due to onCreateView not finishing
            //this.requireView().findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToAlertFragment(username))
            //return binding.root
        }

        binding.usernameButton.setOnClickListener { view: View ->
            // Hide keyboard
            val imm: InputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)

            // Get username from field
            val username = container?.rootView?.findViewById<TextInputEditText>(R.id.username_input)?.text.toString()
            // Store username for later, in sharedPrefs
            val sharedPref = this.requireActivity().getSharedPreferences(getString(R.string.username_pref), Context.MODE_PRIVATE)
            with (sharedPref!!.edit()) {
                putString(getString(R.string.username_pref), username)
                apply()
            }
            // Move - Send with username
            view.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToAlertFragment(username))
        }

        return binding.root

    }
}