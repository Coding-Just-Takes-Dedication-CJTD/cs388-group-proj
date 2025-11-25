package com.example.ludex_cyrpta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class ProfileSettingsFragment : Fragment() {
    //standard function to create the fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    //standard function to call the layout from the .xml file of the fragment
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInflater: Bundle?): View? {
        val view = inflater.inflate(R.layout.profile_settings_screen, container, false)
        return view
    }

    //standard function to populate the fragment with the layout from the .xml file of the fragment
    //things that happen in the "Profile & Settings" page (like listeners) are called here
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get the values from the .xml file of the fragment
        val tabLayout = view.findViewById<TabLayout>(R.id.psTabLayout)
        val viewPager = view.findViewById<ViewPager2>(R.id.psViewPager)


        val logoutBtn: Button = view.findViewById(R.id.logoutBtn)

        logoutBtn.setOnClickListener {
            logout()
        }

        //setup viewpager2 adapter so that both fragments can appear in 1 page within their respective tabs
        val psPagerAdapter = ProfileSettingsPagerAdapter(this)
        viewPager.adapter = psPagerAdapter

        //sets the layout of the tabs and gives the names
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Profile"
                1 -> tab.text = "Settings"
            }
        }.attach()
    }

    //necessary for initializing in MainActivity
    companion object {
        fun newInstance(): ProfileSettingsFragment {
            return ProfileSettingsFragment()
        }
    }
    private fun logout() {
        // --- Sign out from Firebase ---
        FirebaseAuth.getInstance().signOut()

        // --- Clear all fragments from back stack ---
        parentFragmentManager.popBackStack(
            null,
            androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
        )

        // --- Navigate to LoginFragment ---
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.mainScreen, LoginFragment())
            .commit()

        // --- Hide bottom navigation ---
        val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
        bottomNav.menu.setGroupVisible(0, false)
    }

}