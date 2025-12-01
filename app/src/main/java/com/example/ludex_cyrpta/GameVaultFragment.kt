package com.example.ludex_cyrpta

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

private const val TAG = "GameVaultFragment"

class GameVaultFragment : Fragment() {
    private var gameSelectedListener: OnGameSelectedListener? = null
    private val viewModel: GameViewModel by viewModels()
    private lateinit var adapter: GameAdapter
    private lateinit var errorPopUp: TextView
    private lateinit var progBar: ProgressBar

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnGameSelectedListener) {
            gameSelectedListener = context
        } else {
            throw RuntimeException("$context must implement OnGameSelectedListener")
        }
    }

    //standard function to create the fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    //standard function to call the layout from the .xml file of the fragment
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.game_vault_screen, container, false)
        return view
    }

    //standard function to populate the fragment with the layout from the .xml file of the fragment
    //things that happen in the "Game Vault" page (like listeners) are called here
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progBar = view.findViewById(R.id.progressBar)
        errorPopUp = view.findViewById(R.id.errorPopUp)

        //create adapter with empty list
        adapter = GameAdapter { game ->
            Log.d(TAG, "Game clicked: ${game.name}.\nCalling host Activity listener...")

           gameSelectedListener?.onGameSelected(game)
        }

        //reference the RecyclerView and populate with adapter
        val gamesRV = view.findViewById<RecyclerView>(R.id.gameList)
        gamesRV.adapter = adapter

        //set layout manager to position the items
        gamesRV.layoutManager = LinearLayoutManager(view.context)

        setupObservers() //reacts to data changes from the ViewModel
    }

    private fun setupObservers() {
        viewModel.gameList.observe(viewLifecycleOwner, Observer { games ->
            Log.i(TAG, "Received ${games.size} from ViewModel. Updating...")
            adapter.submitList(games)
        })

        viewModel.errMsg.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                errorPopUp.text = error
                errorPopUp.visibility = View.VISIBLE
                progBar.visibility = View.GONE
                Log.e(TAG, "Error: $error")
            } else {
                errorPopUp.visibility = View.GONE
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progBar.visibility = if (isLoading && errorPopUp.visibility != View.VISIBLE) View.VISIBLE else View.GONE
        }
    }

    override fun onDetach() {
        super.onDetach()
        gameSelectedListener = null
    }

    //necessary for initializing in MainActivity
    companion object {
        fun newInstance(): GameVaultFragment {
            return GameVaultFragment()
        }
    }
}