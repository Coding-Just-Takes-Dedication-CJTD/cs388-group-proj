package com.example.ludex_cyrpta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class VaultWishlistFragment : Fragment() {
    //standard function to create the fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    //standard function to call the layout from the .xml file of the fragment
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInflater: Bundle?): View? {
        val view = inflater.inflate(R.layout.vault_wishlist_screen, container, false)
        return view
    }

    //standard function to populate the fragment with the layout from the .xml file of the fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get the values from the .xml file of the fragment
        val tabLayout = view.findViewById<TabLayout>(R.id.vwTabLayout)
        val viewPager = view.findViewById<ViewPager2>(R.id.vwViewPager)

        //setup viewpager2 adapter so that both fragments can appear in 1 page within their respective tabs
        val vwPagerAdapter = VaultWishlistPagerAdapter(this)
        viewPager.adapter = vwPagerAdapter

        //sets the layout of the tabs and gives the names
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Game Vault"
                1 -> tab.text = "Wishlist"
            }
        }.attach()
    }

    //necessary for initializing in MainActivity
    companion object {
        fun newInstance(): VaultWishlistFragment {
            return VaultWishlistFragment()
        }
    }
}