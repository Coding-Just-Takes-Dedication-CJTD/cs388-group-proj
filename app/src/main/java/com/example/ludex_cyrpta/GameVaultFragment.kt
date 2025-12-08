package com.example.ludex_cyrpta

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "GameVaultFragment"

class GameVaultFragment : Fragment() {
    //standard function to create the fragment
    // Helper class to talk to Firebase
    //private val vaultRepo = VaultRepository()
    // Adapter to handle the list items
    private val vaultRepo by lazy { VaultRepository(requireContext()) }
    private lateinit var adapter: GameAdapter
    // Interface to handle clicks (navigating to details)
    private var gameSelectedListener: OnGameSelectedListener? = null
    private lateinit var progressBar: ProgressBar


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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.game_vault_screen, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.vaultList)
        progressBar = view.findViewById(R.id.vaultProgressBar)

        adapter = GameAdapter { game ->
            gameSelectedListener?.onGameSelected(game)
        }
        recyclerView.adapter = adapter
        return view
    }

    //standard function to populate the fragment with the layout from the .xml file of the fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    override fun onResume() {
        super.onResume()
        loadVaultGames() // We load the games here to ensure the list is always fresh
    }

    private fun loadVaultGames() {
        progressBar.visibility = View.VISIBLE

        // Find the views we need to toggle
        val emptyMsg = view?.findViewById<android.view.View>(R.id.emptyVaultMsg)
        val listView = view?.findViewById<android.view.View>(R.id.vaultList)

        vaultRepo.getVaultGames(
            onResult = { games ->
                progressBar.visibility = View.GONE

                if (games.isEmpty()) {
                    // List is empty -> Hide list, Show message
                    listView?.visibility = View.GONE
                    emptyMsg?.visibility = View.VISIBLE
                } else {
                    // List has games -> Show list, Hide message
                    listView?.visibility = View.VISIBLE
                    emptyMsg?.visibility = View.GONE
                    adapter.submitList(games)
                }
            },
            onFailure = { error ->
                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error loading vault: $error", Toast.LENGTH_SHORT).show()
                }
            }
        )
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
