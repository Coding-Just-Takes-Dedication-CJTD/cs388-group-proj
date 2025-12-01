package com.example.ludex_cyrpta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope // Required for database coroutines
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

// 1. Implement the listener interface so we can handle clicks
class WishlistFragment : Fragment(), OnListFragmentInteractionListener {

    private lateinit var gamesRV: RecyclerView
    private var wishedGames: List<Game> = emptyList() // Move variable inside class

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Ensure this layout exists (R.layout.wishlist_screen)
        return inflater.inflate(R.layout.wishlist_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gamesRV = view.findViewById(R.id.wishList)
        gamesRV.layoutManager = LinearLayoutManager(view.context)

        // 2. Load the data from the Database (Room) instead of GameFetcher
        loadWishlistFromDatabase()
    }

    private fun loadWishlistFromDatabase() {
        // We must launch a coroutine because database access cannot be on the main thread
        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(requireContext())

            // Get the list of saved games
            wishedGames = database.gameDao().getAllGames()

            // 3. Setup Adapter with the saved games AND 'this' as the listener
            val adapter = GameAdapter(wishedGames, this@WishlistFragment)
            gamesRV.adapter = adapter

            // Optional: Show a message if list is empty
            if (wishedGames.isEmpty()) {
                // You might want to handle an empty state here (e.g., show a TextView saying "No games saved")
            }
        }
    }

    // 4. Handle what happens when a user clicks a game in the wishlist
    override fun onItemClick(item: Game) {
        // Navigate to details (reuse the Fragment we made earlier)
        val detailsFrag = GameDetailsFragment.newInstance(item)

        parentFragmentManager.beginTransaction()
            .replace(R.id.mainScreen, detailsFrag)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        fun newInstance(): WishlistFragment = WishlistFragment()
    }
}