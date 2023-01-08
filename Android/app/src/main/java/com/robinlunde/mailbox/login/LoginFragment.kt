package com.robinlunde.mailbox.login

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.databinding.FragmentLoginBinding
import com.robinlunde.mailbox.datamodel.pill.User
import kotlinx.coroutines.*
import timber.log.Timber

class LoginFragment : Fragment() {

    lateinit var binding: FragmentLoginBinding

    lateinit var prefs: SharedPreferences

    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, exception ->
        val name = coroutineContext[CoroutineName].toString() //Thread.currentThread().name
        Timber.d("Received error: " + exception.message + "!")
        Timber.e("Trace: " + exception.printStackTrace() + "!")
        if (name != "token") {
            val text =
                if (name == "") "User-check failed! Please login or signup!" else "Login or signup failed! Please try again"
            Toast.makeText(
                context,
                text,
                Toast.LENGTH_LONG
            ).show()
        }
    }

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
        var username = MailboxApp.getUsername()
        if (username == "") {
            MainScope().launch {
                username = MailboxApp.getPrefs().getString(getString(R.string.username_pref), "")
                    .toString()
                Timber.d("Async Username is: $username")
                setUsername(username)
            }
        } else {
            Timber.d("Username is: $username")
            setUsername(username)
        }

        val util = MailboxApp.getUtil()
        val frag = this

        CoroutineScope(Job() + Dispatchers.Main + CoroutineName("LoginToken")).launch(
            exceptionHandler
        ) {
            prefs = MailboxApp.getPrefs()
            // If username is set and we have a valid user from previously, rock and roll
            // check if token is in sharedprefs and working, if it is, use it to log in
            val token = prefs.getString("Token", "")
            Timber.d("Token from sharedPrefs: $token")
            if (token != null && token != "") {
                util.authInterceptor.Token(token)
                val user = util.getUsers()
                Timber.d("Get user from API: $user")
                if (user.id != null) {

                    util.user = user
                    util.user!!.token = token
                    // fetch data in the background
                    util.fetchRepoData()
                    moveUponResult()
                } else {
                    Timber.d("No userid in response! $user")
                }
            } else {
                Timber.d("Token not found stored - fallback!")
            }
        }

        if (username != "" && util.user != null) {
            /**
             * 1. Try access with token in user object
             * 2. if fail, bail out
             * 3. if success, continue
             */
            CoroutineScope(Job() + Dispatchers.Main + CoroutineName("LoggedInUser")).launch(
                exceptionHandler
            ) {
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

            // Get prefs
            val prefs = MailboxApp.getPrefs()

            /**
             * 1. Issue http request with pw and username
             * 2. Await call-back from request
             * 3A. If success, set Util.user and store token
             * 3B. If failure, stop here
             */

            val user = User(newUsername, password)
            Timber.d("$user  " + user.password)

            val coroutineScope = CoroutineScope(Job() + Dispatchers.Main + CoroutineName(func))

            if (func == "signup") coroutineScope.launch(exceptionHandler) {
                val userLoc: User = util.signup(user)
                Timber.d("Returned $userLoc")

                util.user = userLoc
                util.user!!.password = user.password
                Timber.d("Final user: " + util.user)
                util.authInterceptor.Token(util.user!!.token.toString())

                // Store token for later user!
                with(prefs.edit()) {
                    putString("Token", util.user!!.token.toString())
                    putInt("user_id", util.user!!.id!!)
                    apply()
                }
                // Fetch data in the background
                util.fetchRepoData()
                moveUponResult()
            }
            if (func == "login") coroutineScope.launch(exceptionHandler) {
                val userLoc: User = util.login(user)
                Timber.d("Returned $userLoc")

                util.authInterceptor.Token(userLoc.token.toString())

                val tmpUser = util.getUsers()
                Timber.d("Tmp user: $tmpUser")

                util.user = tmpUser
                util.user!!.password = user.password
                util.user!!.token = userLoc.token
                Timber.d("Final user: ${util.user}")

                with(prefs.edit()) {
                    putString("Token", userLoc.token.toString())
                    putInt("user_id", util.user!!.id!!)
                    apply()
                }

                // fetch data in the background
                util.fetchRepoData()
                moveUponResult()
            }
        }
    }

    private fun moveUponResult() {
        Timber.d("Successful callback")
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

    private fun setUsername(username: String) {
        Timber.d("Set Username to: $username")
        // Set text in field to username if previously stored
        if (username != "") binding.usernameInput.setText(username)
    }
}