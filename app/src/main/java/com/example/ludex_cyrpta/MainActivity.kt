package com.example.ludex_cyrpta

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

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
        val prefs = TextSizePreferences(newBase)
        val config = newBase.resources.configuration
        config.fontScale = prefs.getScale()
        val newContext = newBase.createConfigurationContext(config)
        super.attachBaseContext(newContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val themePrefs = ThemePreferences(this)
        if (themePrefs.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) Log.d("FCM", "Token: ${task.result}")
        }

        auth = FirebaseAuth.getInstance()
        bottomNav = findViewById(R.id.bottomNav)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
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
            t.add(R.id.mainScreen, homeFrag, "HOME").hide(homeFrag)
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
            updateBottomNavVisibility(actvFrag)
        }

        fragMngr.addOnBackStackChangedListener {
            val curr = fragMngr.fragments.lastOrNull { it.isVisible }
            actvFrag = curr
            updateBottomNavVisibility(curr)
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

        if (auth.currentUser != null) bottomNav.selectedItemId = R.id.homePage
    }

    override fun onResume() {
        super.onResume()
        updateBottomNavVisibility(actvFrag)
    }

    private fun updateBottomNavVisibility(frag: Fragment?) {
        val shouldShow =
            frag is HomeFragment ||
                    frag is SearchFragment ||
                    frag is TrendingFragment ||
                    frag is VaultWishlistFragment ||
                    frag is ProfileSettingsFragment ||
                    frag is SettingsFragment

        bottomNav.visibility = if (shouldShow) View.VISIBLE else View.GONE
    }

    fun swapFrag(newFrag: Fragment) {
        if (newFrag == actvFrag) return
        val t = supportFragmentManager.beginTransaction()
        actvFrag?.let { t.hide(it) }
        t.show(newFrag)
        t.commit()
        actvFrag = newFrag
        updateBottomNavVisibility(newFrag)
    }

    override fun onGameSelected(game: Game) {
        bottomNav.visibility = View.GONE
        val detailsFrag = GameDetailsFragment.newInstance(game.name)
        val t = supportFragmentManager.beginTransaction()
        actvFrag?.let { t.hide(it) }
        t.add(R.id.mainScreen, detailsFrag, "DETAILS_${game.name.hashCode()}")
        t.addToBackStack(null)
        t.commit()
        actvFrag = detailsFrag
    }
}
