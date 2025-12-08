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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "TrendingFragment"

class TrendingFragment : Fragment() {
    private var gameSelectedListener: OnGameSelectedListener? = null
    private val viewModel: GameViewModel by viewModels()
    private lateinit var adapter: TrendingGameAdapter
    private lateinit var trendRV: RecyclerView
    private lateinit var errorPopUp: TextView
    private lateinit var progBar: ProgressBar

    //standard function to attach to host; necessary for click to work
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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInflater: Bundle?): View? {
        val view = inflater.inflate(R.layout.trending_screen, container, false)
        return view
    }

    //standard function to populate the fragment with the layout from the .xml file of the fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trendRV = view.findViewById(R.id.trendList)
        errorPopUp = view.findViewById(R.id.errorPopUp)
        progBar = view.findViewById(R.id.progressBar)

        adapter = TrendingGameAdapter { game ->
            Log.d(TAG, "Game clicked: ${game.name}.\nCalling host Activity listener...")
            gameSelectedListener?.onGameSelected(game)
        }

        trendRV.adapter = adapter
        val layoutMngr = LinearLayoutManager(view.context)
        trendRV.layoutManager = layoutMngr

        setupObservers()
        viewModel.fetchTrendingGames()
    }

    private fun setupObservers() {
        viewModel.gameList.observe(viewLifecycleOwner, Observer { games ->
            Log.i(TAG, "Received 20 games from ViewModel. Generating...")
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
        fun newInstance(): TrendingFragment {
            return TrendingFragment()
        }
    }
}