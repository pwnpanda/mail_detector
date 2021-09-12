package com.robinlunde.mailbox.login

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.databinding.FragmentLoginBinding
import com.robinlunde.mailbox.datamodel.pill.User
import kotlinx.coroutines.*

class LoginFragment : Fragment() {

    lateinit var binding: FragmentLoginBinding

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
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_login,
            container,
            false
        )
        val username = MailboxApp.getUsername()
        // Set text in field to username if previously stored
        if (username != "") binding.usernameInput.setText(username)

        val util = MailboxApp.getUtil()

        // If username is set and we have a valid user from previously, rock and roll
        if (username != "" && util.user != null) {
            /**
             * 1. Try access with token in user object
             * 2. if fail, bail out
             * 3. if success, continue
             */
            val frag = this
            CoroutineScope(Job() + Dispatchers.Main).launch(CoroutineExceptionHandler { _, exception ->
                Log.d("Login - Checking user", "Received error: ${exception.message}!")
                Log.e("Login - Checking user", "Trace: ${exception.printStackTrace()}!")
                Toast.makeText(
                    context,
                    "User-check failed! Please login or signup!",
                    Toast.LENGTH_LONG
                ).show()
            }) {
                // User is current user
                val user = util.user
                // If not null, check if we are logged in by accessing user
                if (user != null) {
                    val locUser = util.getUser(util.user!!.id!!)
                    // If result is there, move
                    if (locUser.id != null) {
                        // Move past login screen if username is registered
                        findNavController(frag).navigate(
                            LoginFragmentDirections.actionLoginFragmentToAlertFragment()
                        )
                    }
                }
            }
            return binding.root
        }

        // Login
        binding.loginButton.setOnClickListener { view: View ->
            doAuthentication("login", view)
        }

        // Signup
        binding.signupButton.setOnClickListener { view: View ->
            doAuthentication("signup", view)
        }

        return binding.root

    }

    private fun doAuthentication(func: String, view: View) {
        val util = MailboxApp.getUtil()

        // Hide keyboard
        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        // Get username from field
        val newUsername: String = binding.usernameInput.text.toString()
        val password: String = binding.passwordInput.text.toString()
        if (newUsername == "null" || password == "null") {
            Toast.makeText(
                context,
                "Please input a valid username or password!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // store username for later
            MailboxApp.setUsername(newUsername)

            // setup coroutine
            val mainActivityJob = Job()
            val errorHandler = CoroutineExceptionHandler { _, exception ->
                Log.d("Login - $func", "Received error: ${exception.message}!")
                Log.e("Login - $func", "Trace: ${exception.printStackTrace()}!")
                Toast.makeText(
                    context,
                    "Login or signup failed! Please try again",
                    Toast.LENGTH_LONG
                ).show()
            }

            val coroutineScope = CoroutineScope(mainActivityJob + Dispatchers.Main)

            /**
             * 1. Issue http request with pw and username
             * 2. Await call-back from request
             * 3A. If success, set Util.user and store token
             * 3B. If failure, stop here
             */

            val user = User(newUsername, password)
            Log.d("Login - $func", "$user ${user.password}")

            if (func == "signup") coroutineScope.launch(errorHandler) {
                val userLoc: User = util.signup(user)
                Log.d("Login - $func", "Returned $userLoc")

                util.user = userLoc
                util.user!!.password = user.password
                Log.d("Login - $func", "Final user: ${util.user}")
                util.authInterceptor.Token(util.user!!.token.toString())
                // Fetch data in the background
                util.fetchRepoData()
                moveUponResult()
            }
            if (func == "login") coroutineScope.launch(errorHandler) {
                val userLoc: User = util.login(user)
                Log.d("Login - $func", "Returned $userLoc")

                util.user = user
                Log.d("Login - $func", "Final user: ${util.user}")
                util.authInterceptor.Token(userLoc.token.toString())
                var tmpUser = util.getUsers()
                util.user = tmpUser
                util.user!!.password = user.password
                util.user!!.token = userLoc.token
                // fetch data in the background
                util.fetchRepoData()
                moveUponResult()
            }
        }
    }

    private fun moveUponResult() {
        Log.d("Login - moveUponResult", "Successful callback")
        if (MailboxApp.getUtil().user != null) {
            // Move to AlertFragment
            findNavController(this)
                .navigate(LoginFragmentDirections.actionLoginFragmentToAlertFragment())
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val util: Util = MailboxApp.getUtil()
        return when (item.itemId) {

            R.id.alert -> {
                util.logButtonPress("Login - logo")

                findNavController(this).navigate(
                    LoginFragmentDirections.actionLoginFragmentToAlertFragment()
                )
                true
            }
            R.id.logs -> {
                util.logButtonPress("Login - logs")

                // Show log screen
                findNavController(this).navigate(LoginFragmentDirections.actionLoginFragmentToLogviewFragment())
                // return true so that the menu pop up is opened
                true
            }

            R.id.bluetooth -> {
                util.logButtonPress("Login - bt")

                if (MailboxApp.getClickCounter() >= 3) findNavController(this).navigate(
                    LoginFragmentDirections.actionLoginFragmentToDebugFragment()
                )
                super.onOptionsItemSelected(item)
            }

            R.id.pill -> {
                util.logButtonPress("Login - pill")
                findNavController(this).navigate(LoginFragmentDirections.actionLoginFragmentToPillFragment())
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}