package com.robinlunde.mailbox.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable menu buttons in this fragment
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentLoginBinding>(
            inflater,
            R.layout.fragment_login,
            container,
            false
        )
        val username = MailboxApp.getUsername()
        if (username != "") {
            // Move past login screen if username is registered
            findNavController(this).navigate(
                LoginFragmentDirections.actionLoginFragmentToAlertFragment()
            )
            return binding.root
        }

        binding.usernameButton.setOnClickListener { view: View ->
            // Hide keyboard
            val imm: InputMethodManager =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)

            // Get username from field
            val newUsername: String =
                container?.rootView?.findViewById<TextInputEditText>(R.id.username_input)?.text.toString()
            // store username for later
            MailboxApp.setUsername(newUsername)
            // Move - Send with username
            view.findNavController()
                .navigate(LoginFragmentDirections.actionLoginFragmentToAlertFragment())
        }

        return binding.root

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logo -> {
                findNavController(this).navigate(
                    LoginFragmentDirections.actionLoginFragmentToAlertFragment()
                )
                MailboxApp.getUtil().logButtonPress("Login - logo")
                true
            }
            R.id.logs -> {
                // Show log screen
                findNavController(this).navigate(LoginFragmentDirections.actionLoginFragmentToLogviewFragment())
                MailboxApp.getUtil().logButtonPress("Login - logs")
                // return true so that the menu pop up is opened
                true
            }

            R.id.bluetooth -> {
                MailboxApp.getUtil().logButtonPress("Login - bt")
                if (MailboxApp.getClickCounter() >= 3)  findNavController(this).navigate(
                    LoginFragmentDirections.actionLoginFragmentToDebugFragment())
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}