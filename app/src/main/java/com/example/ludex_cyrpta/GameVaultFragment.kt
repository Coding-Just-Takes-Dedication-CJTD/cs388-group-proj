package com.example.ludex_cyrpta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private lateinit var games: List<Game>

// Add the interface implementation to the class signature
class GameVaultFragment : Fragment(), OnListFragmentInteractionListener {

    // ... onCreate and onCreateView stay the same ...

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gamesRV = view.findViewById<RecyclerView>(R.id.gameList)
        games = GameFetcher.getGames() // Your data source

        // Pass 'this' as the listener
        val adapter = GameAdapter(games, this)

        gamesRV.adapter = adapter
        gamesRV.layoutManager = LinearLayoutManager(view.context)
    }

    // Handle the click event from the Adapter
    override fun onItemClick(item: Game) {
        // Create the details fragment with the clicked game
        val detailsFrag = GameDetailsFragment.newInstance(item)

        // Use the MainActivity's swapFrag function if available, or manual transaction
        parentFragmentManager.beginTransaction()
            .replace(R.id.mainScreen, detailsFrag) // Ensure R.id.mainScreen matches your activity_main.xml container
            .addToBackStack(null) // Allows user to press Back button to return to list
            .commit()
    }

    companion object {
        fun newInstance(): GameVaultFragment = GameVaultFragment()
    }
}