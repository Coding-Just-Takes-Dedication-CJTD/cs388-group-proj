package com.example.ludex_cyrpta

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val email = view.findViewById<EditText>(R.id.regEmailInput)
        val pass = view.findViewById<EditText>(R.id.regPasswordInput)
        val confirm = view.findViewById<EditText>(R.id.regConfirmPasswordInput)
        val registerBtn = view.findViewById<Button>(R.id.registerBtn)
        val toLogin = view.findViewById<TextView>(R.id.goToLogin)

        // --- Register button ---
        registerBtn.setOnClickListener {
            val e = email.text.toString().trim()
            val p = pass.text.toString().trim()
            val c = confirm.text.toString().trim()

            if (e.isEmpty() || p.isEmpty() || c.isEmpty()) {
                Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (p != c) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(e, p)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Account created! Please log in.", Toast.LENGTH_SHORT).show()

                    // Sign out so user is not auto-logged in
                    auth.signOut()

                    // Clear input fields
                    email.text.clear()
                    pass.text.clear()
                    confirm.text.clear()

                    // --- Clear this fragment from back stack & navigate to LoginFragment ---
                    parentFragmentManager.popBackStack()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.mainScreen, LoginFragment())
                        .commit()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
        }

        // --- Go to login text ---
        toLogin.setOnClickListener {
            // Clear RegisterFragment from back stack and go back to login
            parentFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.mainScreen, LoginFragment())
                .commit()
        }
    }
}
