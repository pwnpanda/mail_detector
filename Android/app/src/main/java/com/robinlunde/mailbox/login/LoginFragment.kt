package com.robinlunde.mailbox.login

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputEditText
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
        // Check if key is in shared preferences
        val sharedPref = this.requireActivity().getPreferences(Context.MODE_PRIVATE)
        val prefString = getString(R.string.username_pref)
        Log.d("prefString", prefString)
        // If it is, then use that as our username
        if (sharedPref.contains(prefString)) {
            val username: String = sharedPref.getString(prefString, "") as String
            Log.d("unameFroMPrefs", username)
            // TODO Need to do this, but it throws error due to onCreateView not finishing
            this.requireView().findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToAlertFragment(username))
            return binding.root
        }

        binding.usernameButton.setOnClickListener { view: View ->
            // Hide keyboard
            val imm: InputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)

            // Get username from field
            val username = container?.rootView?.findViewById<TextInputEditText>(R.id.username_input)?.text.toString()
            // Store username for later, in sharedPrefs
            with (sharedPref!!.edit()) {
                putString(getString(R.string.username_pref), username)
                apply()
            }
            // Move - Send with username
            view.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToAlertFragment(username))
        }

        return binding.root
        //val application = requireNotNull(this.activity).application

        // Create instance of ViewModel Factory
        /*val dataSource = PostDatabase.getInstance(application).postDatabaseDao
        val viewModelFactory = PostViewModelFactory (dataSource, application)

        val postViewModel = ViewModelProvider(this, viewModelFactory).get(PostViewModel::class.java)

        binding.postViewModel = postViewModel

        binding.lifecycleOwner = this*/

    }
}