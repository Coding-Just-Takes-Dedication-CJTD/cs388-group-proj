package com.example.ludex_cyrpta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    //standard function to create the fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    //standard function to call the layout from the .xml file of the fragment
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInflater: Bundle?): View? {
        val view = inflater.inflate(R.layout.profile_screen, container, false)
        return view
    }

    //standard function to populate the fragment with the layout from the .xml file of the fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logoutBtn: Button = view.findViewById(R.id.logoutBtn)
        logoutBtn.setOnClickListener {
            logout()
        }

    }

    //necessary for initializing in MainActivity
    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

    /*private fun logout() {
        // --- Sign out from Firebase ---
        FirebaseAuth.getInstance().signOut()

        // --- Clear all fragments from back stack ---
        parentFragmentManager.popBackStack(
            null,
            androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
        )

        // --- Navigate to LoginFragment ---
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.mainScreen, LoginFragment())
            .commit()

        // --- Hide bottom navigation ---
       // val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
       // bottomNav.menu.setGroupVisible(0, false)
    }*/
    private fun logout() {
        // --- Sign out from Firebase ---
        FirebaseAuth.getInstance().signOut()



        // Restart the Activity
        // clears everything and starts fresh
        val intent = android.content.Intent(requireContext(), MainActivity::class.java)
        //  clear the history so the user can't "back" into the logged-in app
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        // --------------------------------------
    }

}
