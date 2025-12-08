package com.example.ludex_cyrpta

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {
    private var greetingTextView: TextView? = null

    //standard function to create the fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    //standard function to call the layout from the .xml file of the fragment
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInflater: Bundle?): View? {
        val view = inflater.inflate(R.layout.home_screen, container, false)
        return view
    }

    //standard function to populate the fragment with the layout from the .xml file of the fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Set greeting to user's email ---
        val greetingTextView = view.findViewById<TextView>(R.id.homePageHdr)
        val currentUser = FirebaseAuth.getInstance().currentUser
        greetingTextView.text = if (currentUser != null) {
            val email = currentUser.email ?: "user"
            val username = email.substringBefore("@") //show only username from email
            "Hello $username!"
        } else {
            "Hello user!"
        }

        // Find the "boxes"
        val act = activity as MainActivity
        val bottomNav = act.findViewById<BottomNavigationView>(R.id.bottomNav)

    }
    override fun onResume() {
        super.onResume()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email ?: "user"
        val username = email.substringBefore("@")
        greetingTextView?.text = "Hello $username!"
    }

    //necessary for initializing in MainActivity
    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}