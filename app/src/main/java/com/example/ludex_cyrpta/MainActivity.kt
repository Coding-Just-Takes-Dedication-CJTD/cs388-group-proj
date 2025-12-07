package com.example.ludex_cyrpta

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), OnGameSelectedListener {

    private var actvFrag: Fragment? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNav: BottomNavigationView

    // Fragments
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

    // Handle user's answer to notification permission pop-up
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) Log.d(TAG, "Notification permission granted")
        else Log.w(TAG, "Notification permission denied")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        loadTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupFirebase()
        setupWindowInsets()
        setupFragments()
        setupBottomNav()

        if (savedInstanceState == null) {
            showInitialFragment()
        } else {
            restoreVisibleFragment()
        }

        listenForBackstackChanges()
        askNotificationPermission()
    }


    // -------------------------------
    // THEME
    // -------------------------------
    private fun loadTheme() {
        val themePrefs = ThemePreferences(this)
        val mode = if (themePrefs.isDarkModeEnabled())
            AppCompatDelegate.MODE_NIGHT_YES
        else
            AppCompatDelegate.MODE_NIGHT_NO

        AppCompatDelegate.setDefaultNightMode(mode)
    }


    // -------------------------------
    // FIREBASE & NOTIFICATION SETUP
    // -------------------------------
    private fun setupFirebase() {
        auth = FirebaseAuth.getInstance()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }
            Log.d(TAG, "FCM Token: ${task.result}")
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }


    // -------------------------------
    // FRAGMENTS
    // -------------------------------
    private fun setupFragments() {
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
    }

    private fun showInitialFragment() {
        val fm = supportFragmentManager
        val t = fm.beginTransaction()

        // Add all fragments once and hide them
        t.add(R.id.mainScreen, loginFrag, "LOGIN").hide(loginFrag)
            .add(R.id.mainScreen, registerFrag, "REGISTER").hide(registerFrag)
            .add(R.id.mainScreen, wishFrag, "WISHLIST").hide(wishFrag)
            .add(R.id.mainScreen, gvFrag, "GAME_VAULT").hide(gvFrag)
            .add(R.id.mainScreen, setFrag, "SETTINGS").hide(setFrag)
            .add(R.id.mainScreen, profFrag, "PROFILE").hide(profFrag)
            .add(R.id.mainScreen, psFrag, "PROFILE_SETTINGS").hide(psFrag)
            .add(R.id.mainScreen, trendingFrag, "TRENDS").hide(trendingFrag)
            .add(R.id.mainScreen, searchFrag, "SEARCH").hide(searchFrag)
            .add(R.id.mainScreen, vwFrag, "VAULT_WISH").hide(vwFrag)
            .add(R.id.mainScreen, homeFrag, "HOME").hide(homeFrag)

        t.commit()

        // Decide initial fragment based on auth state
        if (auth.currentUser == null) {
            supportFragmentManager.beginTransaction().show(loginFrag).commit()
            bottomNav.visibility = View.GONE
            actvFrag = loginFrag
        } else {
            supportFragmentManager.beginTransaction().show(homeFrag).commit()
            bottomNav.visibility = View.VISIBLE
            actvFrag = homeFrag
        }
    }

    private fun restoreVisibleFragment() {
        val fm = supportFragmentManager
        actvFrag = fm.fragments.lastOrNull { it.isVisible }
        bottomNav.visibility = if (isMainFragment(actvFrag)) View.VISIBLE else View.GONE
    }

    private fun listenForBackstackChanges() {
        supportFragmentManager.addOnBackStackChangedListener {
            val current = supportFragmentManager.fragments.lastOrNull { it.isVisible }
            actvFrag = current
            bottomNav.visibility = if (isMainFragment(current)) View.VISIBLE else View.GONE
        }
    }

    // Determines if fragment should show bottom nav
    private fun isMainFragment(frag: Fragment?): Boolean {
        return frag is HomeFragment ||
                frag is SearchFragment ||
                frag is TrendingFragment ||
                frag is VaultWishlistFragment ||
                frag is ProfileSettingsFragment
    }

    fun swapFrag(newFrag: Fragment) {
        if (newFrag == actvFrag) return
        val t = supportFragmentManager.beginTransaction()
        actvFrag?.let { t.hide(it) }
        t.show(newFrag).commit()
        actvFrag = newFrag
    }


    // -------------------------------
    // BOTTOM NAVIGATION
    // -------------------------------
    private fun setupBottomNav() {
        bottomNav = findViewById(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            val newFrag = when (item.itemId) {
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

        if (auth.currentUser != null) {
            bottomNav.selectedItemId = R.id.homePage
        }
    }


    // -------------------------------
    // WINDOW INSETS
    // -------------------------------
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }
    }


    // -------------------------------
    // GAME DETAILS (INTERFACE)
    // -------------------------------
    override fun onGameSelected(game: Game) {
        Log.d(TAG, "Game Selected: ${game.name}")

        bottomNav.visibility = View.GONE

        val detailsFrag = GameDetailsFragment.newInstance(game.name)
        val t = supportFragmentManager.beginTransaction()

        actvFrag?.let { t.hide(it) }
        t.add(R.id.mainScreen, detailsFrag, "GAME_DETAILS_${game.name.hashCode()}")
            .addToBackStack(null)
            .commit()

        actvFrag = detailsFrag
    }
}
