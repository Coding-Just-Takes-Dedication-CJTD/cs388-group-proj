package com.example.ludex_cyrpta

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import android.Manifest // <--- Needed for permission constants
import android.content.Context
import android.content.pm.PackageManager // <--- Needed to check permission status
import android.os.Build // <--- Needed to check Android version
import androidx.activity.result.contract.ActivityResultContracts // <--- Needed for the new way to ask permissions
import androidx.core.content.ContextCompat // <--- Helper for compatibility

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), OnGameSelectedListener {

    private var actvFrag: Fragment? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNav: BottomNavigationView

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


    override fun attachBaseContext(newBase: Context) {
        val textPrefs = TextSizePreferences(newBase)
        val config = newBase.resources.configuration
        config.fontScale = textPrefs.getScale()
        val newContext = newBase.createConfigurationContext(config)
        super.attachBaseContext(newContext)
    }

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

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) Log.d("FCM", "Token: ${it.result}")
            else {
                Log.e(TAG, "Fetching FCM registration token failed", it.exception)
                return@addOnCompleteListener
            }
        }

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            sendLoginNotification()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (auth.currentUser != null) {
            sendLoginNotification()
        }

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

        if (savedInstanceState == null) {
            val t = fragMngr.beginTransaction()

            t.add(R.id.mainScreen, registerFrag, "REGISTER").hide(registerFrag)
            t.add(R.id.mainScreen, loginFrag, "LOGIN").hide(loginFrag)
            t.add(R.id.mainScreen, wishFrag, "WISHLIST").hide(wishFrag)
            t.add(R.id.mainScreen, gvFrag, "GAME_VAULT").hide(gvFrag)
            t.add(R.id.mainScreen, setFrag, "SETTINGS").hide(setFrag)
            t.add(R.id.mainScreen, profFrag, "PROFILE").hide(profFrag)
            t.add(R.id.mainScreen, psFrag, "PROFILE_SETTINGS").hide(psFrag)
            t.add(R.id.mainScreen, trendingFrag, "TRENDS").hide(trendingFrag)
            t.add(R.id.mainScreen, searchFrag, "SEARCH").hide(searchFrag)
            t.add(R.id.mainScreen, vwFrag, "VAULT_WISH").hide(vwFrag)
            t.add(R.id.mainScreen, homeFrag, "HOME").hide(homeFrag) // hide home initially
            t.commit()

            // Decide first fragment based on login state
            actvFrag = if (auth.currentUser == null) {
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
            updateNavVisibility(actvFrag)
        }

        //ensures clicking the back button does the correct navigation
        // (and that bottom Nav is only visible on main pages)
        fragMngr.addOnBackStackChangedListener {
            val curr = fragMngr.fragments.lastOrNull { it.isVisible }
            actvFrag = curr
            updateNavVisibility(curr)
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

        if (auth.currentUser != null) bottomNav.selectedItemId = R.id.homePage
    }

    fun swapFrag(newFrag: Fragment) {
        if (newFrag == actvFrag) return //to not swap with self
        val t = supportFragmentManager.beginTransaction()
        actvFrag?.let { fragTrnsctn.hide(it) }
        t.show(newFrag)
        t.commit()
        actvFrag = newFrag
        updateNavVisibility(newFrag)
    }

    override fun onGameSelected(game: Game) {
        val detailsFrag = GameDetailsFragment.newInstance(game.name, game.id)
        val t = supportFragmentManager.beginTransaction()
        actvFrag?.let { fragTransaction.hide(it) }
        t.add(R.id.mainScreen, detailsFrag)
        t.addToBackStack(null)
        t.commit()
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

    private fun sendLoginNotification() {
        //Check Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                return // If we don't have permission, stop here.
            }
        }

        // 2. Define the ID and Channel
        val channelId = "ludex_login_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        // 3. Create Channel (Required for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Login Updates",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 4. Build the Notification
        val notificationBuilder = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your app icon
            .setContentTitle("Welcome to Ludex Crypta!")
            .setContentText("We hope you enjoy your experience.")
            .setAutoCancel(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)

        // 5. Show it! (ID = 777 is just a random number to identify this message)
        notificationManager.notify(777, notificationBuilder.build())
    }
    private fun updateNavVisibility(f: Fragment?) {
        val show = f is HomeFragment ||
                f is SearchFragment ||
                f is TrendingFragment ||
                f is VaultWishlistFragment ||
                f is ProfileSettingsFragment
        bottomNav.visibility = if (show) View.VISIBLE else View.GONE
    }
}
