package com.example.ludex_cyrpta

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView // Import ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "ProfileFragment"

    // Define the ImageView so we can access it across functions
    private lateinit var profileImageView: ImageView

    // 1. Image Picker Launcher
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            safeToast("Processing image...")
            val imageString = encodeImage(uri)
            if (imageString != null) {
                saveStringToFirestore(imageString)
            } else {
                safeToast("Image too large or invalid!")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInflater: Bundle?): View? {
        return inflater.inflate(R.layout.profile_screen, container, false)
    }

    //standard function to populate the fragment with the layout from the .xml file of the fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- 1. Find All Views ---
        val emailDisplay = view.findViewById<TextView>(R.id.currentUserEmail)
        val btnUser = view.findViewById<Button>(R.id.changeUsernameBtn)
        val btnEmail = view.findViewById<Button>(R.id.changeEmailBtn)
        val btnPass = view.findViewById<Button>(R.id.changePasswordBtn)
        val logoutBtn: Button = view.findViewById(R.id.logoutBtn)
        val btnDelete = view.findViewById<Button>(R.id.deleteAccountBtn)

        // Find the Image Views
        profileImageView = view.findViewById(R.id.profileImageView)
        val imageCard = view.findViewById<View>(R.id.profilePicCard)

        // --- 2. Setup Logic ---
        emailDisplay.text = auth.currentUser?.email

        // Load existing image immediately
        loadProfileImage()

        // Set Click Listener to Open Gallery
        imageCard.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        logoutBtn.setOnClickListener { logout() }

        // --- 3. Change Handlers ---
        btnUser.setOnClickListener {
            showSimpleDialog("Change Username", "Enter new username:") { newName ->
                val uid = auth.currentUser?.uid
                if (uid != null && newName.isNotEmpty()) {
                    db.collection("users").document(uid)
                        .update("username", newName)
                        .addOnSuccessListener { safeToast("Username updated!") }
                        .addOnFailureListener { safeToast("Failed to update.") }
                }
            }
        }

        btnEmail.setOnClickListener {
            showReAuthDialog {
                showSimpleDialog("Change Email", "Enter new email:") { newEmail ->
                    val user = auth.currentUser
                    if (user != null) {
                        user.updateEmail(newEmail)
                            .addOnSuccessListener {
                                safeToast("Email updated! Verification sent.")
                                user.sendEmailVerification()
                                emailDisplay.text = newEmail
                            }
                            .addOnFailureListener { e ->
                                safeToast("Error: ${e.message}")
                                Log.e(TAG, "Update Email Failed", e)
                            }
                    }
                }
            }
        }

        btnPass.setOnClickListener {
            showReAuthDialog {
                showSimpleDialog("Change Password", "Enter new password:", isPassword = true) { newPass ->
                    val user = auth.currentUser
                    if (user != null) {
                        user.updatePassword(newPass)
                            .addOnSuccessListener { safeToast("Password updated!") }
                            .addOnFailureListener { e ->
                                safeToast("Error: ${e.message}")
                                Log.e(TAG, "Update Password Failed", e)
                            }
                    }
                }
            }
        }

        // NEW: Delete Account Listener
        btnDelete.setOnClickListener {
            // 1. Security Check: Password required first
            showReAuthDialog {
                // 2. Confirmation Dialog: Are you sure?
                showDeleteConfirmation()
            }
        }

    }





    // --- IMAGE HELPERS ---

    private fun encodeImage(imageUri: Uri): String? {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Compress to 300x300 to stay under Firestore 1MB limit
            val outputStream = ByteArrayOutputStream()
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)

            val imageBytes = outputStream.toByteArray()
            return Base64.encodeToString(imageBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun saveStringToFirestore(base64String: String) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .update("profileImageBase64", base64String)
            .addOnSuccessListener {
                safeToast("Profile picture updated!")
                // Update UI immediately
                val bitmap = decodeBase64(base64String)
                profileImageView.setImageBitmap(bitmap)
            }
            .addOnFailureListener {
                safeToast("Failed to save. Image might be too big.")
            }
    }

    private fun loadProfileImage() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val base64String = document.getString("profileImageBase64")
                if (!base64String.isNullOrEmpty()) {
                    val bitmap = decodeBase64(base64String)
                    profileImageView.setImageBitmap(bitmap)
                }
            }
    }

    private fun decodeBase64(input: String): Bitmap {
        val decodedByte = Base64.decode(input, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
    }

    // --- UTILITY HELPERS ---

    private fun safeToast(message: String) {
        if (isAdded && context != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSimpleDialog(title: String, hint: String, isPassword: Boolean = false, onConfirm: (String) -> Unit) {
        val context = context ?: return
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)

        val input = EditText(context)
        input.hint = hint
        if (isPassword) {
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val container = LinearLayout(context)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(50, 0, 50, 0)
        input.layoutParams = params
        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton("Update") { _, _ ->
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                onConfirm(text)
            } else {
                safeToast("Input cannot be empty")
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showReAuthDialog(onSuccess: () -> Unit) {
        val context = context ?: return
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Security Check")
        builder.setMessage("Please confirm your CURRENT password to continue.")

        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("Confirm") { _, _ ->
            val password = input.text.toString()
            val user = auth.currentUser

            if (user != null && user.email != null && password.isNotEmpty()) {
                val credential = EmailAuthProvider.getCredential(user.email!!, password)
                user.reauthenticate(credential)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> safeToast("Incorrect Password: ${e.message}") }
            } else {
                safeToast("Error: User not found or empty password.")
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        if (isAdded && context != null) {
            val intent = android.content.Intent(requireContext(), MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    // "Are you sure?"
    private fun showDeleteConfirmation() {
        val context = context ?: return
        AlertDialog.Builder(context)
            .setTitle("Delete Account?")
            .setMessage("WARNING: This action is permanent. All your data, vault, and wishlist will be lost forever.")
            .setPositiveButton("DELETE") { _, _ ->
                performAccountDeletion()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    //  Deletion Logic
    private fun performAccountDeletion() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        // Delete User Data from Firestore
        db.collection("users").document(uid).delete()
            .addOnSuccessListener {
                // Delete User from Authentication
                user.delete()
                    .addOnSuccessListener {
                        safeToast("Account successfully deleted.")
                        //Go to Login
                        if (isAdded && context != null) {
                            val intent = android.content.Intent(requireContext(), MainActivity::class.java)
                            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                    .addOnFailureListener { e ->
                        safeToast("Failed to delete account: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                safeToast("Failed to delete data: ${e.message}")
            }
    }

    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }
}