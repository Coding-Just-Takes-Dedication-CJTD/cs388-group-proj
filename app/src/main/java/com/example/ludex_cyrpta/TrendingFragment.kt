package com.example.ludex_cyrpta

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TrendingFragment : Fragment() {
    //standard function to create the fragment

    private val viewModel: GameViewModel by viewModels()
    private lateinit var adapter: GameAdapter
    private var gameSelectedListener: OnGameSelectedListener? = null

    // Attach listener so clicks work
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
        val view = inflater.inflate(R.layout.trending_screen, container, false)
        return view
    }

    //standard function to populate the fragment with the layout from the .xml file of the fragment
    //things that happen in the "Trending" page (like listeners) are called here
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val recyclerView = view.findViewById<RecyclerView>(R.id.trendingList)

        // 2. Setup the Adapter
        adapter = GameAdapter { game ->
            // Handle click: Go to details page
            gameSelectedListener?.onGameSelected(game)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // 3. Observe the ViewModel
        viewModel.gameList.observe(viewLifecycleOwner) { games ->
            adapter.submitList(games)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // 4. Trigger the fetch immediately
        // This ensures the list populates as soon as you open the tab
        viewModel.fetchTrendingGames()
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