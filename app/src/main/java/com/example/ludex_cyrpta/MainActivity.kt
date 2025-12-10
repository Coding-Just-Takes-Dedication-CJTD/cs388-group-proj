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
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity(), OnGameSelectedListener {

    private var actvFrag: Fragment? = null
    private lateinit var auth: FirebaseAuth
    lateinit var bottomNav: BottomNavigationView

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) Log.d("FCM", "Token: ${it.result}")
        }

        auth = FirebaseAuth.getInstance()

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

        if (auth.currentUser != null) bottomNav.selectedItemId = R.id.homePage
    }

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
        hideBottomNav()
        val detailsFrag = GameDetailsFragment.newInstance(game.name, game.id)
        val t = supportFragmentManager.beginTransaction()
        actvFrag?.let { t.hide(it) }
        t.add(R.id.mainScreen, detailsFrag)
        t.addToBackStack(null)
        t.commit()
        actvFrag = detailsFrag
    }

    fun showBottomNav() {
        bottomNav.visibility = View.VISIBLE
    }

    fun hideBottomNav() {
        bottomNav.visibility = View.GONE
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
