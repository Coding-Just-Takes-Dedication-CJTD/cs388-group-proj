package com.example.ludex_cyrpta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment() {
    //standard function to create the fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    //standard function to call the layout from the .xml file of the fragment
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInflater: Bundle?): View? {
        val view = inflater.inflate(R.layout.home_screen, container, false)
        return view
    }

    //standard function to populate the fragment with the layout from the .xml file of the fragment
    //things that happen in the "Home" page (like listeners) are called here
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the "boxes"
        val trendingBox = view.findViewById<View>(R.id.trendPlacehldr)
        val salesBox = view.findViewById<View>(R.id.salePlacehldr)
        val vaultBox = view.findViewById<View>(R.id.gvPlacehldr)
        val searchBar = view.findViewById<SearchView>(R.id.searchView)
        val act = activity as MainActivity
        val bottomNav = act.findViewById<BottomNavigationView>(R.id.bottomNav)

        // When clicked → tell MainActivity to swap fragments
        trendingBox.setOnClickListener {
            val act = activity as MainActivity
            val frag = act.supportFragmentManager.findFragmentByTag("TRENDS")
            act.swapFrag(frag!!)
            bottomNav.selectedItemId = R.id.trendingPage
        }

        salesBox.setOnClickListener {
            val act = activity as MainActivity
            val frag = act.supportFragmentManager.findFragmentByTag("SALES")
            act.swapFrag(frag!!)
            bottomNav.selectedItemId = R.id.trendingPage
        }

        vaultBox.setOnClickListener {
            val act = activity as MainActivity
            val frag = act.supportFragmentManager.findFragmentByTag("GAME_VAULT")
            act.swapFrag(frag!!)
            bottomNav.selectedItemId = R.id.vault_wishlistPage
        }

        searchBar.setOnClickListener {
            val act = activity as MainActivity
            val frag = act.supportFragmentManager.findFragmentByTag("SEARCH")
            act.swapFrag(frag!!)
            bottomNav.selectedItemId = R.id.searchPage

        }
    }

    //necessary for initializing in MainActivity
    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}