package com.example.ludex_cyrpta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private lateinit var wishedGames: List<Game>

class WishlistFragment : Fragment() {
    //standard function to create the fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    //standard function to call the layout from the .xml file of the fragment
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInflater: Bundle?): View? {
        val view = inflater.inflate(R.layout.wishlist_screen, container, false)
        return view
    }

    //standard function to populate the fragment with the layout from the .xml file of the fragment
    //things that happen in the "Wishlist" page (like listeners) are called here
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    //necessary for initializing in MainActivity
    companion object {
        fun newInstance(): WishlistFragment {
            return WishlistFragment()
        }
    }
}