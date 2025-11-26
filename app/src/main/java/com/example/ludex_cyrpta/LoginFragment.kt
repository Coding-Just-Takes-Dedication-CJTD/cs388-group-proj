package com.example.ludex_cyrpta

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val email = view.findViewById<EditText>(R.id.emailInput)
        val pass = view.findViewById<EditText>(R.id.passwordInput)
        val loginBtn = view.findViewById<Button>(R.id.loginBtn)
        val toRegister = view.findViewById<TextView>(R.id.goToRegister)

        // --- Login button ---
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

                    // --- Clear all previous fragments ---
                    parentFragmentManager.popBackStack(
                        null,
                        androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )

                    // --- Navigate to fresh HomeFragment ---
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.mainScreen, HomeFragment())
                        .commit()

                    // --- Show bottom navigation ---
                    val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
                    bottomNav.menu.setGroupVisible(0, true)
                    bottomNav.selectedItemId = R.id.homePage
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
        }

        // --- Go to RegisterFragment ---
        toRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainScreen, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
