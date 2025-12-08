package com.example.ludex_cyrpta

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.getValue

private const val TAG = "SearchFragment"

class SearchFragment : Fragment(), FilterSelectionListener {

    private var gameSelectedListener: OnGameSelectedListener? = null
    private val viewModel: GameViewModel by viewModels()
    private lateinit var adapter: GameAdapter
    private lateinit var gamesRV: RecyclerView
    private lateinit var errorPopUp: TextView
    private lateinit var progBar: ProgressBar
    private lateinit var searchBar: SearchView
    private lateinit var filterBtn: Button

    private var currOtherStoreFilter: String? = null
    private var recViewScrollPos = 0 //save scroll position in Bundle (needed for infinite scroll)

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
        // Check if there's a saved scroll position
        if (savedInstanceState != null) {
            recViewScrollPos = savedInstanceState.getInt("KEY_SCROLL_POSITION", 0)
        }
    }

    //standard function to call the layout from the .xml file of the fragment
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInflater: Bundle?): View? {
        val view = inflater.inflate(R.layout.search_screen, container, false)
        return view
    }

    //standard function to populate the fragment with the layout from the .xml file of the fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progBar = view.findViewById(R.id.progressBar)
        errorPopUp = view.findViewById(R.id.errorPopUp)
        searchBar = view.findViewById(R.id.searchBar)
        filterBtn = view.findViewById(R.id.filtersBtn)
        gamesRV = view.findViewById(R.id.gameList)

        //sync local & viewModel states
        currOtherStoreFilter = viewModel.currFilter.value

        //create adapter with empty list
        adapter = GameAdapter { game ->
            Log.d(TAG, "Game clicked: ${game.name}.\nCalling host Activity listener...")
            gameSelectedListener?.onGameSelected(game)
        }

        //populate the RecyclerView with adapter
        gamesRV.adapter = adapter

        //set layout manager to position the items
        val layoutMngr = LinearLayoutManager(context)
        gamesRV.layoutManager = layoutMngr

        //reset the fragment to default if the gameList is empty (from anything besides an empty search result)
        if (viewModel.gameList.value.isNullOrEmpty() && viewModel.currQuery.value.isNullOrEmpty() && viewModel.currFilter.value.isNullOrEmpty()) {
            viewModel.resetToDefault()
        }

        //ensures infinite scrolling
        gamesRV.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (viewModel.isLoading.value == true || viewModel.isLastPage || dy <= 0 || !viewModel.currQuery.value.isNullOrEmpty() || !viewModel.currFilter.value.isNullOrEmpty()) {
                    return
                }

                val visElemNum = layoutMngr.childCount
                val totalElems = layoutMngr.itemCount
                val firstVisPos = layoutMngr.findFirstVisibleItemPosition()

                //if it scrolls past the threshold trigger, load more items
                if ((visElemNum + firstVisPos) >= (totalElems - 5)) viewModel.loadMoreGames()
            }
        })

        //whenever there is a change in the search bar
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            //runs whenever search/enter is hit on the keyboard
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) { //if the query has something in it
                    viewModel.searchGames(query, currOtherStoreFilter) //search for it
                    gamesRV.scrollToPosition(0) //scroll to top for new results
                } else {
                    viewModel.resetToDefault() //re-calls original search fragment list
                }
                searchBar.clearFocus() //hide the keyboard
                return true
            }

            //runs automatically as query is typed/deleted (does nothing since filters are being used)
            //but is being specifically used once everything has been deleted by user
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank() && (!viewModel.currQuery.value.isNullOrEmpty() || !viewModel.currFilter.value.isNullOrEmpty())) {
                    viewModel.resetToDefault()
                }
                return false
            }
        })

        filterBtn.setOnClickListener {
            val filterBottomSheet = FilterBottomSheet()
            filterBottomSheet.setFilterSelectionListener(this) //this fragment is target so BottomSheet can call onFilterApplied
            filterBottomSheet.show(childFragmentManager, FilterBottomSheet.TAG)
        }

        //ViewModel Observers
        viewModel.gameList.observe(viewLifecycleOwner) { games ->
            Log.i(TAG, "Received ${games.size} games from ViewModel. Updating...")
            adapter.submitList(games) {
                if (recViewScrollPos != 0) { //restore scroll position after update
                    layoutMngr.scrollToPosition(recViewScrollPos)
                    recViewScrollPos = 0 // Reset after use
                }

                val currentQuery = viewModel.currQuery.value
                val currentFilter = viewModel.currFilter.value

                if (games.isEmpty() && (!currentQuery.isNullOrEmpty() || !currentFilter.isNullOrEmpty())) { //show "no results" message iff a search was performed AND the list is empty
                    errorPopUp.text = "No Results Found :("
                    errorPopUp.visibility = View.VISIBLE
                } else if (games.isNotEmpty() || (currentQuery.isNullOrEmpty() && currentFilter.isNullOrEmpty())) { //otherwise, hide the error
                    errorPopUp.visibility = View.GONE
                }
            }
        }

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

    override fun onFilterApplied(selectedFilter: String?) {
        currOtherStoreFilter = selectedFilter
        Log.d(TAG, "Filter: \"$selectedFilter\" applied")
        viewModel.applyFilter(selectedFilter)
    }

    //functions dealing with infinite scrolling
    private fun getFirstVisibleItemPosition(): Int {
        return (gamesRV.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: 0
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("KEY_SCROLL_POSITION", getFirstVisibleItemPosition()) // Save the current scroll position before the fragment is destroyed
    }

    override fun onPause() {
        super.onPause()
        recViewScrollPos = getFirstVisibleItemPosition() // get the position on pause to handle back navigation/temporary destruction
    }

    //companion to onAttach; does opposite
    override fun onDetach() {
        super.onDetach()
        gameSelectedListener = null
    }

    //necessary for initializing in MainActivity
    companion object {
        fun newInstance(): SearchFragment {
            return SearchFragment()
        }
    }
}