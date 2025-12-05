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
import com.google.firebase.firestore.FirebaseFirestore

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
    //things that happen in the "Home" page (like listeners) are called here
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Set greeting to user's email ---
        greetingTextView = view.findViewById<TextView>(R.id.homePageHdr)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        updateGreeting()


        // Find the "boxes"
        val trendingBox = view.findViewById<View>(R.id.trendPlacehldr)
        val vaultBox = view.findViewById<View>(R.id.gvPlacehldr)
        val searchBar = view.findViewById<SearchView>(R.id.searchView)
        val act = activity as MainActivity
        val bottomNav = act.findViewById<BottomNavigationView>(R.id.bottomNav)

        // When clicked → tell MainActivity to swap fragments
        trendingBox.setOnClickListener {
            val frag = act.supportFragmentManager.findFragmentByTag("TRENDS")

            frag?.let {
                act.swapFrag(it)
                bottomNav.selectedItemId = R.id.trendingPage
                Log.d(TAG, "Swapped to TrendingFragment successfully!")
            } ?: run {
                Log.e(TAG, "TrendingFragment not found with tag TRENDS")
            }
        }

        vaultBox.setOnClickListener {
            val frag = act.supportFragmentManager.findFragmentByTag("VAULT_WISH")

            frag?.let {
                act.swapFrag(it)
                bottomNav.selectedItemId = R.id.vault_wishlistPage
                Log.d(TAG, "Swapped to VaultWishlistFragment successfully!")
            } ?: run {
                Log.e(TAG, "VaultWishlistFragment not found with tag VAULT_WISH")
            }
        }

        searchBar.setOnClickListener {
            val frag = act.supportFragmentManager.findFragmentByTag("SEARCH")

            frag?.let {
                act.swapFrag(it)
                bottomNav.selectedItemId = R.id.searchPage
                Log.d(TAG, "Swapped to SearchFragment successfully!")
            } ?: run {
                Log.e(TAG, "SearchFragment not found with tag SEARCH")
            }
        }
    }
    override fun onResume() {
        super.onResume()
        // call the helper function so the name stays correct even if the app resumes
        updateGreeting()
    }

    // --- NEW: Helper function to fetch the name ---
    // This keeps our code clean and reusable
    private fun updateGreeting() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Try to get the real username
                        val savedName = document.getString("username")
                        // Note: We use greetingTextView? because we are in a callback
                        greetingTextView?.text = "Hello $savedName!"
                    } else {
                        // Fallback to email if database fetch fails/is empty
                        val email = currentUser.email ?: "User"
                        val fallbackName = email.substringBefore("@")
                        greetingTextView?.text = "Hello $fallbackName!"
                    }
                }
                .addOnFailureListener {
                    // Safety fallback if internet fails
                    val email = currentUser.email ?: "User"
                    val fallbackName = email.substringBefore("@")
                    greetingTextView?.text = "Hello $fallbackName!"
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