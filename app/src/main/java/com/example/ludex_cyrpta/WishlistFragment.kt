package com.example.ludex_cyrpta

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private lateinit var wishedGames: List<Game>

class WishlistFragment : Fragment() {
    //standard function to create the fragment


    //private val wishRepo = WishlistRepository()
    private val wishRepo by lazy { WishlistRepository(requireContext()) }
    private lateinit var adapter: GameAdapter
    private var gameSelectedListener: OnGameSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnGameSelectedListener) {
            gameSelectedListener = context
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    //standard function to call the layout from the .xml file of the fragment
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInflater: Bundle?): View? {
        val view = inflater.inflate(R.layout.wishlist_screen, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.wishList)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = GameAdapter { game ->
            gameSelectedListener?.onGameSelected(game)
        }
        recyclerView.adapter = adapter
        return view
    }

    override fun onResume() {
        super.onResume()
        loadWishlistGames()
    }

    private fun loadWishlistGames() {
        wishRepo.getWishlistGames(
            onResult = { games ->
                adapter.submitList(games)
            },
            onFailure = { error ->
                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onDetach() {
        super.onDetach()
        gameSelectedListener = null
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