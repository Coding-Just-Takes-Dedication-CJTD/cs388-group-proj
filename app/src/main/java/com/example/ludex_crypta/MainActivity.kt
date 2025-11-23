package com.example.ludex_crypta

import androidx.fragment.app.Fragment
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit // Import this
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    // You can initialize these here
    private val homeFrag = HomeFragment()
    private val vwFrag = VaultWishlistFragment()
    private val searchFrag = SearchFragment()
    private val trendingFrag = TrendingFragment()
    private val psFrag = ProfileSettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set the initial fragment
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container, homeFrag) // Use the new container ID
                setReorderingAllowed(true)
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            val newFrag: Fragment = when (item.itemId) {
                R.id.homePage -> homeFrag
                R.id.searchPage -> searchFrag
                R.id.trendingPage -> trendingFrag
                R.id.vault_wishlistPage -> vwFrag
                R.id.profile_settingsPage -> psFrag
                else -> homeFrag // Default case
            }

            // This is the new navigation logic
            supportFragmentManager.commit {
                replace(R.id.fragment_container, newFrag) // Use replace
                setReorderingAllowed(true)
                // Do not add to back stack for bottom nav items
            }
            true
        }
    }
}