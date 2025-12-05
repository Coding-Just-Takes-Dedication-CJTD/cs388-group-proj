package com.example.ludex_cyrpta

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()


        val usernameInput = view.findViewById<EditText>(R.id.regUsernameInput)
        val email = view.findViewById<EditText>(R.id.regEmailInput)
        val pass = view.findViewById<EditText>(R.id.regPasswordInput)
        val confirm = view.findViewById<EditText>(R.id.regConfirmPasswordInput)
        val registerBtn = view.findViewById<Button>(R.id.registerBtn)
        val toLogin = view.findViewById<TextView>(R.id.goToLogin)

        // --- Register button ---
        registerBtn.setOnClickListener {
            val uName = usernameInput.text.toString().trim() // Get Username
            val e = email.text.toString().trim()
            val p = pass.text.toString().trim()
            val c = confirm.text.toString().trim()

            if (uName.isEmpty() || e.isEmpty() || p.isEmpty() || c.isEmpty()) {
                Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (p != c) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

// ... inside the registerBtn listener ...
            auth.createUserWithEmailAndPassword(e, p)
                .addOnSuccessListener { result -> // --- FIX 2: Added "result ->" so we can use it below

                    val userId = result.user?.uid

                    if (userId != null) {
                        val userMap = hashMapOf(
                            "username" to uName,
                            "email" to e
                        )

                        // Save the username to Firestore
                        db.collection("users").document(userId).set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Account created! Please log in", Toast.LENGTH_SHORT).show()

                                // Sign out so they aren't auto-logged in
                                auth.signOut()

                                // Restart the App cleanly
                                val intent = android.content.Intent(requireContext(), MainActivity::class.java)
                                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                            .addOnFailureListener { e ->
                                // If database save fails, tell the user
                                Toast.makeText(requireContext(), "Failed to save username: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    // If account creation fails, tell the user
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
        }

        // --- Go to login text ---
        toLogin.setOnClickListener {
            // Clear RegisterFragment from back stack and go back to login
            parentFragmentManager.popBackStack()
        }
    }
}
