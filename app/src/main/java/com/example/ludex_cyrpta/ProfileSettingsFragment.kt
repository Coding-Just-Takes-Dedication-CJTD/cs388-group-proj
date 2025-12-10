package com.example.ludex_cyrpta

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ProfileSettingsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInflater: Bundle?): View? {
        return inflater.inflate(R.layout.profile_settings_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = view.findViewById<TabLayout>(R.id.psTabLayout)
        val viewPager = view.findViewById<ViewPager2>(R.id.psViewPager)

        val psPagerAdapter = ProfileSettingsPagerAdapter(this)
        viewPager.adapter = psPagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Profile"
                1 -> "Settings"
                else -> null
            }
        }.attach()
    }

    fun onSteamLinked(steamId: String) {
        Log.d("STEAM_LINK", "Steam ID received in fragment: $steamId")
        // Add UI update or saving logic here
    }

    companion object {
        fun newInstance(): ProfileSettingsFragment {
            return ProfileSettingsFragment()
        }
    }
}
