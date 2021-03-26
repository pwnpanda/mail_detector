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
import com.robinlunde.mailbox.OnFragmentInteractionListener
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.databinding.FragmentLoginBinding


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var myListener : OnFragmentInteractionListener

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = DataBindingUtil.inflate<FragmentLoginBinding>(inflater, R.layout.fragment_login, container, false)

        binding.usernameButton.setOnClickListener { view: View ->
            val imm: InputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.findNavController().navigate(R.id.action_loginFragment_to_alertFragment)
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            myListener = activity as OnFragmentInteractionListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener")
        }
    }
}