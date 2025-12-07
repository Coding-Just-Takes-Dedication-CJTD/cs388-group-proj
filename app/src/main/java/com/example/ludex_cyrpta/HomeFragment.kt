package com.example.ludex_cyrpta

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration



private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {
    private var greetingTextView: TextView? = null
    private var snapshotListener: ListenerRegistration? = null


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

        // Set greeting to user's email
        greetingTextView = view.findViewById<TextView>(R.id.homePageHdr)


        // Now it's safe to call this
        updateGreeting()

    }
    override fun onResume() {
        super.onResume()
        // call the helper function so the name stays correct even if the app resumes
        updateGreeting()
    }

    // NEW: Update data when swapping tabs/screens
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            // "hidden = false" means the fragment is now VISIBLE
            updateGreeting()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snapshotListener?.remove() // Stop listening to save battery/data
    }

    // Helper function to fetch the name
    private fun updateGreeting() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        snapshotListener?.remove()

        if (currentUser != null) {
            snapshotListener = db.collection("users").document(currentUser.uid)
                .addSnapshotListener { document, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    if (document != null && document.exists()) {
                        // Name user used when they registered their account
                        val savedName = document.getString("username")
                        greetingTextView?.text = "Hello, $savedName!"
                    } else {
                        // Fallback to the first part of the user's email
                        val email = currentUser.email ?: "User"
                        val fallbackName = email.substringBefore("@")
                        greetingTextView?.text = "Hello, $fallbackName!"
                    }
                }
        } else {
            greetingTextView?.text = "Hello user!"
        }
    }


    //necessary for initializing in MainActivity
    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}