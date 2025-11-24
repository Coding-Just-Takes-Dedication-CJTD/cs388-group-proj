package com.example.ludex_cyrpta

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private var actvFrag: Fragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /* PROCESS FOR ADDING NEW SCREEN FRAGMENTS:
            // 1. Initialize your Fragment instances (in first TODO, like so...)
            val fragName: FragType = FragType()

            // 2. Initialize your Buttons <SINCE OTHER SCREENS WILL BE TRIGGERED BY BUTTON> (in second TODO, like so...)
            val btnVar: Button = findViewById(R.id.btnNAMEinFRAGxml)

            // 3. Set the Click Listeners (as needed, like so...)
            homeButton.setOnClickListener {
                switchFrag(fragToSwitchTo)
            }
         */

        // Bottom Nav & Fragment Manager
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)
        val fragMngr: FragmentManager = supportFragmentManager

        //Fragments that are accessible by Bottom Nav
        val homeFrag: HomeFragment = HomeFragment()
        val vwFrag: VaultWishlistFragment = VaultWishlistFragment() //has both "Game Vault" and "Wishlist" pages
        val searchFrag: SearchFragment = SearchFragment()
        val trendingFrag: TrendingFragment = TrendingFragment()
        val psFrag: ProfileSettingsFragment = ProfileSettingsFragment() //has both "Profile" and "Settings" pages

        //Other Screen Fragments (accessible by other means)
        //TODO: add the other screen fragments here  (login, registration pages 1 & 2, game info (and if each tab-divided page [Game Vault & Profile/Settings] needs a frag for their second tab))
        val salesFrag: OnSaleFragment = OnSaleFragment()
        val profFrag: ProfileFragment = ProfileFragment()
        val setFrag: SettingsFragment = SettingsFragment()
        val gvFrag: GameVaultFragment = GameVaultFragment()
        val wishFrag: WishlistFragment = WishlistFragment()

        //TODO: button creation here

        // set at beginning (should be login screen but might stay Home screen)
        if (savedInstanceState == null) {
            //TODO: add (and hide) the other screen frags on top as you add screens
            fragMngr.beginTransaction()
                .add(R.id.mainScreen, wishFrag, "WISHLIST")
                .hide(wishFrag)
                .add(R.id.mainScreen, gvFrag, "GAME_VAULT")
                .hide(gvFrag)
                .add(R.id.mainScreen, setFrag, "SETTINGS")
                .hide(setFrag)
                .add(R.id.mainScreen, profFrag, "PROFILE")
                .hide(profFrag)
                .add(R.id.mainScreen, psFrag, "PROFILE_SETTINGS")
                .hide(psFrag)
                .add(R.id.mainScreen, salesFrag, "SALES")
                .hide(salesFrag)
                .add(R.id.mainScreen, trendingFrag, "TRENDS")
                .hide(trendingFrag)
                .add(R.id.mainScreen, searchFrag, "SEARCH")
                .hide(searchFrag)
                .add(R.id.mainScreen, vwFrag, "VAULT_WISH")
                .hide(vwFrag)
                .add(R.id.mainScreen, homeFrag, "HOME") //will prolly have the login (as lowest) and registration pages (as 2nd lowest) below this one since they'll be seen first
                .commit()

            actvFrag = homeFrag
        }

        bottomNav.setOnItemSelectedListener { item ->
            lateinit var newFrag : Fragment
            when (item.itemId) {
                R.id.homePage -> newFrag = homeFrag
                R.id.searchPage -> newFrag = searchFrag
                R.id.trendingPage -> newFrag = trendingFrag
                R.id.vault_wishlistPage -> newFrag = vwFrag
                R.id.profile_settingsPage -> newFrag = psFrag
            }
            swapFrag(newFrag)
            true
        }

        bottomNav.selectedItemId = R.id.homePage //default screen is the home page
    }

    fun swapFrag(newFrag: Fragment) {
        val fragMngr = supportFragmentManager
        val fragTrnsctn = fragMngr.beginTransaction()

        actvFrag?.let {
            fragTrnsctn.hide(it)
        }
        fragTrnsctn.show(newFrag)
        fragTrnsctn.commit()
        actvFrag = newFrag
    }
}