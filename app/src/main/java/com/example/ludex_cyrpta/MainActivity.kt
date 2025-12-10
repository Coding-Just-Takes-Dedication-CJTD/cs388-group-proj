package com.example.ludex_cyrpta

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import android.Manifest // <--- Needed for permission constants
import android.content.Context
import android.content.pm.PackageManager // <--- Needed to check permission status
import android.os.Build // <--- Needed to check Android version
import androidx.activity.result.contract.ActivityResultContracts // <--- Needed for the new way to ask permissions
import androidx.core.content.ContextCompat // <--- Helper for compatibility
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging // <--- The Firebase class to get the token

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), OnGameSelectedListener {

    private var actvFrag: Fragment? = null
    private lateinit var auth: FirebaseAuth
    private var snapshotListener: ListenerRegistration? = null
    private lateinit var bottomNav: BottomNavigationView

    // --- Fragments ---
    private lateinit var loginFrag: LoginFragment
    private lateinit var registerFrag: RegisterFragment
    private lateinit var homeFrag: HomeFragment
    private lateinit var vwFrag: VaultWishlistFragment
    private lateinit var searchFrag: SearchFragment
    private lateinit var trendingFrag: TrendingFragment
    private lateinit var psFrag: ProfileSettingsFragment
    private lateinit var profFrag: ProfileFragment
    private lateinit var setFrag: SettingsFragment
    private lateinit var gvFrag: GameVaultFragment
    private lateinit var wishFrag: WishlistFragment


    // --- NEW: This handles the USER'S CHOICE when the permission pop-up appears ---
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
        } else {
            Log.w(TAG, "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askNotificationPermission()



        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d(TAG, "FCM Token: $token") // <--- LOOK IN LOGCAT FOR THIS TAG TO COPY YOUR TOKEN
        }


        auth = FirebaseAuth.getInstance()

        // Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

       /* // --- NEW: Trigger Notification if User is Logged In ---
        if (auth.currentUser != null) {
            sendLoginNotification()
        }*/

        val fragMngr: FragmentManager = supportFragmentManager


        loginFrag = LoginFragment()
        registerFrag = RegisterFragment()

        homeFrag = HomeFragment()
        vwFrag = VaultWishlistFragment()
        searchFrag = SearchFragment()
        trendingFrag = TrendingFragment()
        psFrag = ProfileSettingsFragment()

        profFrag = ProfileFragment()
        setFrag = SettingsFragment()
        gvFrag = GameVaultFragment()
        wishFrag = WishlistFragment()

        bottomNav = findViewById(R.id.bottomNav)

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
                bottomNav.visibility = View.GONE
                loginFrag
            } else {
                // Show home fragment
                fragMngr.beginTransaction().show(homeFrag).commit()
                bottomNav.visibility = View.VISIBLE
                homeFrag
            }
        } else { //else find currently visible fragment
            actvFrag = fragMngr.fragments.lastOrNull() { it.isVisible }

            val isMainFrag = actvFrag is HomeFragment || actvFrag is SearchFragment ||
                             actvFrag is TrendingFragment || actvFrag is VaultWishlistFragment ||
                             actvFrag is ProfileSettingsFragment
            bottomNav.visibility = if (isMainFrag) View.VISIBLE else View.GONE
        }

        //ensures clicking the back button does the correct navigation
        // (and that bottom Nav is only visible on main pages)
        fragMngr.addOnBackStackChangedListener {
            val currFrag = fragMngr.fragments.lastOrNull { it.isVisible }
            actvFrag = currFrag

            val isMainFrag = currFrag is HomeFragment || currFrag is SearchFragment ||
                             currFrag is TrendingFragment || currFrag is VaultWishlistFragment ||
                             currFrag is ProfileSettingsFragment

            if (isMainFrag) {
                bottomNav.visibility = View.VISIBLE
            } else {
                bottomNav.visibility = View.GONE
            }
        }

        // --- Bottom Navigation ---
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
        if (newFrag == actvFrag) return //to not swap with self

        val fragTrnsctn = supportFragmentManager.beginTransaction()
        actvFrag?.let { fragTrnsctn.hide(it) }
        fragTrnsctn.show(newFrag)
        fragTrnsctn.commit()
        actvFrag = newFrag
    }

    override fun onGameSelected(game: Game) {
        Log.d(TAG, "Game: ${game.name} has been selected...\nGoing to details page...")

        bottomNav.visibility = View.GONE

        val detailsFrag = GameDetailsFragment.newInstance(game.name, game.id)

        val fragTransaction = supportFragmentManager.beginTransaction()
        actvFrag?.let { fragTransaction.hide(it) }

        //unique id to differentiate fragments
        fragTransaction.add(R.id.mainScreen, detailsFrag, "GAME_DETAILS_${game.id}")
        fragTransaction.addToBackStack(null)
        fragTransaction.commit()

        actvFrag = detailsFrag
    }

    // Helper function to check Android version and permission status
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is already granted; do nothing.
            } else {
                // Permission is NOT granted; show the system pop-up to the user.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // NEW: Function to send the "Welcome" notification
    fun sendLoginNotification() {

        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()


        snapshotListener?.remove()

        //Check Permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return // If we don't have permission, stop here.
            }
        }

        // 2. Define the ID and Channel
        val channelId = "ludex_login_channel_high"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        // 3. Create Channel (Required for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Login Updates",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

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
                        // 4. Build the Notification
                        val notificationBuilder =
                            androidx.core.app.NotificationCompat.Builder(this, channelId)
                                .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your app icon
                                .setContentTitle("Welcome to Ludex Crypta, ${savedName}!")
                                .setContentText("We hope you enjoy your experience.")
                                .setAutoCancel(true)
                                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)

                        // 5. Show it! (ID = 777 is just a random number to identify this message)
                        notificationManager.notify(777, notificationBuilder.build())
                    } else {
                        // Fallback to the first part of the user's email
                        val email = currentUser.email ?: "User"
                        val fallbackName = email.substringBefore("@")
                        val notificationBuilder =
                            androidx.core.app.NotificationCompat.Builder(this, channelId)
                                .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your app icon
                                .setContentTitle("Welcome to Ludex Crypta, ${fallbackName}!")
                                .setContentText("We hope you enjoy your experience.")
                                .setAutoCancel(true)
                                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                    }

                }
        }
        else {
            val notificationBuilder =
                androidx.core.app.NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your app icon
                    .setContentTitle("Welcome to Ludex Crypta!")
                    .setContentText("We hope you enjoy your experience.")
                    .setAutoCancel(true)
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)

            notificationManager.notify(777, notificationBuilder.build()) // 5. Show it! (ID = 777 is just a random number to identify this message)
        }

    }
}
