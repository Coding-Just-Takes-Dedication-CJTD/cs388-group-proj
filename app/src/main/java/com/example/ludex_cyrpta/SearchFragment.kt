package com.example.ludex_cyrpta

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
//import android.widget.SearchView
import androidx.appcompat.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.getValue

private const val TAG = "SearchFragment"

class SearchFragment : Fragment() {

    private var gameSelectedListener: OnGameSelectedListener? = null
    private val viewModel: GameViewModel by viewModels()
    private lateinit var adapter: GameAdapter
    private lateinit var gamesRV: RecyclerView
    private lateinit var errorPopUp: TextView
    private lateinit var progBar: ProgressBar
    private lateinit var searchBar: SearchView

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

        //restore scroll position
        if (savedInstanceState != null) recViewScrollPos = savedInstanceState.getInt("KEY_SCROLL_POSITION", 0)
    }

    //standard function to call the layout from the .xml file of the fragment
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInflater: Bundle?): View? {
        val view = inflater.inflate(R.layout.search_screen, container, false)
        return view
    }

    //standard function to populate the fragment with the layout from the .xml file of the fragment
    //things that happen in the "Search" page (like listeners) are called here
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progBar = view.findViewById(R.id.progressBar)
        errorPopUp = view.findViewById(R.id.errorPopUp)
        // Find the search bar in the layout
        searchBar = view.findViewById(R.id.searchBar)

        // Attach a listener that triggers every time the text changes
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            // This runs if the user presses the "Enter" or "Search" button on their keyboard
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Only search if the text isn't empty
                if (!query.isNullOrEmpty()) {
                    // Send the query to the ViewModel function we wrote above
                    viewModel.searchGames(query)
                    // Hide the keyboard so the user can see the results
                    searchBar.clearFocus()
                }
                return true // Return true to say "we handled this event"
            }

            // This runs INSTANTLY every time a letter is typed or deleted
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    // Check if the user has typed at least 3 characters
                    if (newText.length >= 3) {
                        // If yes, perform the search automatically
                        // This allows the user to see results as they type "Zel..." -> "Zelda"
                        viewModel.searchGames(newText)
                    } else if (newText.isEmpty()) {
                        // If the text box is empty (user deleted everything or clicked X),
                        // go back to the default list of games.
                        viewModel.resetToDefaultList()
                    }
                }
                return true
            }
        })






        //create adapter with empty list
        adapter = GameAdapter { game ->
            Log.d(TAG, "Game clicked: ${game.name}.\nCalling host Activity listener...")
            gameSelectedListener?.onGameSelected(game)
        }

        //reference the RecyclerView and populate with adapter
        gamesRV = view.findViewById(R.id.gameList)
        gamesRV.adapter = adapter

        //set layout manager to position the items
        val layoutMngr = LinearLayoutManager(view.context)
        gamesRV.layoutManager = layoutMngr

        //set scroll position after data is set, then reset after use
        if (recViewScrollPos != 0) {
            viewModel.gameList.observe(viewLifecycleOwner, object: Observer<List<Game>> {
                override fun onChanged(value: List<Game>) {
                    if (value.isNotEmpty()) {
                        layoutMngr.scrollToPosition(recViewScrollPos)
                        recViewScrollPos = 0
                        viewModel.gameList.removeObserver(this)
                    }
                }
            })
        }

        //INFINITE scrolling
        gamesRV.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return //don't occur when scrolling up

                val visElemNum = layoutMngr.childCount
                val totalElems = layoutMngr.itemCount
                val firstVisPos = layoutMngr.findFirstVisibleItemPosition()

                if (((visElemNum + firstVisPos) >= (totalElems - 5)) //if it scrolls past the threshold trigger
                    && (firstVisPos >= 0) && (totalElems > 0)) //while the first visible position is in physical possible range
                    viewModel.loadMoreGames()

            }
        })

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