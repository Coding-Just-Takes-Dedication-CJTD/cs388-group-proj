package com.example.ludex_cyrpta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private var actvFrag: Fragment? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()

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
}
