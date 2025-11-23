package com.example.ludex_crypta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace

class HomeFragment : Fragment() {
    //standard function to create the fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    //standard function to call the layout from the .xml file of the fragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInflater: Bundle?
    ):View? {
        return inflater.inflate(R.layout.home_screen, container, false)
    }

    //standard function to populate the fragment with the layout from the .xml file of the fragment
    //things that happen in the "Home" page (like listeners) are called here
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.searchView).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, SearchFragment())
                addToBackStack(null) //allow back button to return home
            }
        }

        view.findViewById<View>(R.id.trendPlacehldr).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, TrendingFragment())
                addToBackStack(null)
            }
        }

        view.findViewById<View>(R.id.salePlacehldr).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, OnSaleFragment())
                addToBackStack(null)
            }
        }

        view.findViewById<View>(R.id.gvPlacehldr).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, GameVaultFragment())
                addToBackStack(null)

            }
        }
    }

    //necessary for initializing in MainActivity
    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}