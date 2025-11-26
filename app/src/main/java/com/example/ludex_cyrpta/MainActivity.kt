package com.example.ludex_cyrpta

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private var actvFrag: Fragment? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token -> Log.d("FCM_TOKEN", "Token: $token") }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    // Check first-time login and send welcome
                    checkFirstTimeAndSendWelcome(userId, token)
                }
                // Optional: save token to Firestore for multiple devices
                saveTokenToFirestore(token)
            }
        }

        fun saveTokenToFirestore(token: String) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val db = Firebase.firestore
            db.collection("users").document(userId)
                .update("tokens", FieldValue.arrayUnion(token))
                .addOnSuccessListener { println("Token saved!") }
                .addOnFailureListener { e -> println("Failed to save token: $e") }
        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }

        // Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fragMngr: FragmentManager = supportFragmentManager

        // --- Fragments ---
        val loginFrag = LoginFragment()
        val registerFrag = RegisterFragment()

        val homeFrag = HomeFragment()
        val vwFrag = VaultWishlistFragment()
        val searchFrag = SearchFragment()
        val trendingFrag = TrendingFragment()
        val psFrag = ProfileSettingsFragment()

        val salesFrag = OnSaleFragment()
        val profFrag = ProfileFragment()
        val setFrag = SettingsFragment()
        val gvFrag = GameVaultFragment()
        val wishFrag = WishlistFragment()

        // --- Add & hide all fragments (only once) ---
        if (savedInstanceState == null) {
            val fragTransaction = fragMngr.beginTransaction()

            fragTransaction
                .add(R.id.mainScreen, registerFrag, "REGISTER").hide(registerFrag)
                .add(R.id.mainScreen, loginFrag, "LOGIN").hide(loginFrag)
                .add(R.id.mainScreen, wishFrag, "WISHLIST").hide(wishFrag)
                .add(R.id.mainScreen, gvFrag, "GAME_VAULT").hide(gvFrag)
                .add(R.id.mainScreen, setFrag, "SETTINGS").hide(setFrag)
                .add(R.id.mainScreen, profFrag, "PROFILE").hide(profFrag)
                .add(R.id.mainScreen, psFrag, "PROFILE_SETTINGS").hide(psFrag)
                .add(R.id.mainScreen, salesFrag, "SALES").hide(salesFrag)
                .add(R.id.mainScreen, trendingFrag, "TRENDS").hide(trendingFrag)
                .add(R.id.mainScreen, searchFrag, "SEARCH").hide(searchFrag)
                .add(R.id.mainScreen, vwFrag, "VAULT_WISH").hide(vwFrag)
                .add(R.id.mainScreen, homeFrag, "HOME").hide(homeFrag) // hide home initially
                .commit()

            // Decide first fragment based on login state
            val currentUser = auth.currentUser
            actvFrag = if (currentUser == null) {
                // Show login fragment
                fragMngr.beginTransaction().show(loginFrag).commit()
                loginFrag
            } else {
                // Show home fragment
                fragMngr.beginTransaction().show(homeFrag).commit()
                homeFrag
            }
        }

        // --- Bottom Navigation ---
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            val newFrag: Fragment = when (item.itemId) {
                R.id.homePage -> homeFrag
                R.id.searchPage -> searchFrag
                R.id.trendingPage -> trendingFrag
                R.id.vault_wishlistPage -> vwFrag
                R.id.profile_settingsPage -> psFrag
                else -> homeFrag
            }
            swapFrag(newFrag)
            true
        }

        // Set default bottom nav selection if user is logged in
        if (auth.currentUser != null) bottomNav.selectedItemId = R.id.homePage
    }

    // --- Swap fragments cleanly ---
    fun swapFrag(newFrag: Fragment) {
        val fragTrnsctn = supportFragmentManager.beginTransaction()
        actvFrag?.let { fragTrnsctn.hide(it) }
        fragTrnsctn.show(newFrag)
        fragTrnsctn.commit()
        actvFrag = newFrag
    }

    fun saveTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = Firebase.firestore
        db.collection("users").document(userId)
            .update("tokens", FieldValue.arrayUnion(token))
            .addOnSuccessListener { println("Token saved!") }
            .addOnFailureListener { e -> println("Failed to save token: $e") }
    }


    private fun sendWelcomeNotificationLocally() {
        val channelId = "default_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Welcome to Ludex Crypta!")
            .setContentText("We hope you enjoy your experience.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }


    private fun checkFirstTimeAndSendWelcome(userId: String, fcmToken: String) {
        val db = Firebase.firestore
        val userDoc = db.collection("users").document(userId)

        userDoc.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                // First time user -> send welcome notification
                sendWelcomeNotificationLocally()

                // Save that we sent the welcome message and token
                userDoc.set(mapOf(
                    "welcomeSent" to true,
                    "token" to fcmToken
                ))
            } else {
                // Not first time -> just update token in case it changed
                userDoc.update("token", fcmToken)
            }
        }
    }


}
