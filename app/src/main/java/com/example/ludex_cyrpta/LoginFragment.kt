package com.example.ludex_cyrpta

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "LoginFragment"

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val email = view.findViewById<EditText>(R.id.emailInput)
        val pass = view.findViewById<EditText>(R.id.passwordInput)
        val loginBtn = view.findViewById<Button>(R.id.loginBtn)
        val toRegister = view.findViewById<TextView>(R.id.goToRegister)

        // Login button
        loginBtn.setOnClickListener {
            val e = email.text.toString().trim()
            val p = pass.text.toString().trim()

            if (e.isEmpty() || p.isEmpty()) {
                Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(e, p)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Logged in!", Toast.LENGTH_SHORT).show()
                    (activity as? MainActivity)?.sendLoginNotification()
                    val mainAct = requireActivity() as MainActivity
                    val fragMngr = mainAct.supportFragmentManager

                    // Clear all previous fragments
                    fragMngr.popBackStack(
                        null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )

                    // Navigate to existing HomeFragment so hidden fragments aren't destroyed
                    var homeFrag = fragMngr.findFragmentByTag("HOME")

                    if (homeFrag != null) {
                        mainAct.swapFrag(homeFrag)

                        val bottomNav = mainAct.findViewById<BottomNavigationView>(R.id.bottomNav)
                        bottomNav.visibility = View.VISIBLE
                        bottomNav.selectedItemId = R.id.homePage
                    } else {
                        Log.e(TAG, "Error: pre-initialized HomeFragment not found with tag: 'HOME'...\nMaking a new one...")
                        fragMngr.beginTransaction().replace(R.id.mainScreen, HomeFragment(), "HOME").commit()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
        }

        // Go to RegisterFragment
        toRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainScreen, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}