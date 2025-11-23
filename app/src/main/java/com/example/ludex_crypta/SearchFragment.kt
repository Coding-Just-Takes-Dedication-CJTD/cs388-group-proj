package com.example.ludex_crypta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class SearchFragment : Fragment() {
    //standard function to create the fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    //standard function to call the layout from the .xml file of the fragment
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInflater: Bundle?): View? {
        return inflater.inflate(R.layout.search_screen, container, false)
    }

    //standard function to populate the fragment with the layout from the .xml file of the fragment
    //things that happen in the "Search" page (like listeners) are called here
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    //necessary for initializing in MainActivity
    companion object {
        fun newInstance(): SearchFragment {
            return SearchFragment()
        }
    }
}