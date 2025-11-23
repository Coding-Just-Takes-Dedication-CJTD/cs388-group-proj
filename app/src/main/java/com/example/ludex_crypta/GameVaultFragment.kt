package com.example.ludex_crypta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private lateinit var games: List<Game>

class GameVaultFragment : Fragment() {
    //standard function to create the fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    //standard function to call the layout from the .xml file of the fragment
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInflater: Bundle?): View? {
        return inflater.inflate(R.layout.game_vault_screen, container, false)
    }

    //standard function to populate the fragment with the layout from the .xml file of the fragment
    //things that happen in the "Game Vault" page (like listeners) are called here
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //reference the RecyclerView
        val gamesRV = view.findViewById<RecyclerView>(R.id.gameList)

        //get the list of games
        games = GameFetcher.getGames()

        //create the adapter
        val adapter = GameAdapter(games)

        //attach the adapter to the RecyclerView to populate items
        gamesRV.adapter = adapter

        //set layout manager to position the items
        gamesRV.layoutManager = LinearLayoutManager(view.context)
    }

    //necessary for initializing in MainActivity
    companion object {
        fun newInstance(): GameVaultFragment {
            return GameVaultFragment()
        }
    }
}