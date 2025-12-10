package com.example.ludex_cyrpta

import android.content.Context
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
import android.content.pm.PackageManager // <--- Needed to check permission status
import android.os.Build // <--- Needed to check Android version
import androidx.activity.result.contract.ActivityResultContracts // <--- Needed for the new way to ask permissions
import androidx.core.content.ContextCompat // <--- Helper for compatibility
import com.google.firebase.messaging.FirebaseMessaging // <--- The Firebase class to get the token

private const val TAG = "MainActivity"


class MainActivity : AppCompatActivity(), OnGameSelectedListener {

    private var actvFrag: Fragment? = null
    private lateinit var auth: FirebaseAuth
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

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            sendLoginNotification()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        val fragMngr = supportFragmentManager

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
            t.add(R.id.mainScreen, registerFrag).hide(registerFrag)
            t.add(R.id.mainScreen, loginFrag).hide(loginFrag)
            t.add(R.id.mainScreen, wishFrag).hide(wishFrag)
            t.add(R.id.mainScreen, gvFrag).hide(gvFrag)
            t.add(R.id.mainScreen, setFrag).hide(setFrag)
            t.add(R.id.mainScreen, profFrag).hide(profFrag)
            t.add(R.id.mainScreen, psFrag).hide(psFrag)
            t.add(R.id.mainScreen, trendingFrag).hide(trendingFrag)
            t.add(R.id.mainScreen, searchFrag).hide(searchFrag)
            t.add(R.id.mainScreen, vwFrag).hide(vwFrag)
            t.add(R.id.mainScreen, homeFrag).hide(homeFrag)
            t.commit()

            actvFrag = if (auth.currentUser == null) {
                fragMngr.beginTransaction().show(loginFrag).commit()
                bottomNav.visibility = View.GONE
                loginFrag
            } else {
                fragMngr.beginTransaction().show(homeFrag).commit()
                bottomNav.visibility = View.VISIBLE
                homeFrag
            }
        } else {
            actvFrag = fragMngr.fragments.lastOrNull { it.isVisible }
            updateNavVisibility(actvFrag)
        }

        fragMngr.addOnBackStackChangedListener {
            val curr = fragMngr.fragments.lastOrNull { it.isVisible }
            actvFrag = curr
            updateNavVisibility(curr)
        }

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
        if (newFrag == actvFrag) return
        val t = supportFragmentManager.beginTransaction()
        actvFrag?.let { t.hide(it) }
        t.show(newFrag)
        t.commit()
        actvFrag = newFrag
        updateNavVisibility(newFrag)
    }

    override fun onGameSelected(game: Game) {
        Log.d(TAG, "Game: ${game.name} has been selected...\nGoing to details page...")
        val detailsFrag = GameDetailsFragment.newInstance(game.name, game.id)
        val t = supportFragmentManager.beginTransaction()
        actvFrag?.let { t.hide(it) }
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

        val channelId = "ludex_login_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Login Updates",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your app icon
            .setContentTitle("Welcome to Ludex Crypta!")
            .setContentText("We hope you enjoy your experience.")
            .setAutoCancel(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)

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
