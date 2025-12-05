package com.example.ludex_cyrpta

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer

private const val TAG = "GameDetailsFrag"

class GameDetailsFragment : Fragment() {
    private val viewModel: GameViewModel by viewModels()

    //initialize textViews
    private lateinit var title: TextView
    private lateinit var rating: TextView
    private lateinit var debutDate: TextView
    private lateinit var genres: TextView
    private lateinit var themes: TextView
    private lateinit var gameModes: TextView
    private lateinit var platforms: TextView
    private lateinit var otherServices: TextView
    private lateinit var url: TextView
    private lateinit var trendBox: TextView
    private lateinit var description: TextView
    private lateinit var errorMsg: TextView

    //initialize other views
    private lateinit var trailerVid: WebView
    private lateinit var backButton: Button
    private lateinit var addToListBtnGroup: RadioGroup
    private lateinit var progBar: ProgressBar


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.game_details, container, false)

        title = view.findViewById(R.id.gameTitleTV)
        rating = view.findViewById(R.id.gameRatingTV)
        debutDate = view.findViewById(R.id.gameReleaseDateTV)
        genres = view.findViewById(R.id.genresListTV)
        themes = view.findViewById(R.id.themeListTV)
        gameModes = view.findViewById(R.id.gameModesTV)
        platforms = view.findViewById(R.id.platformListTV)
        otherServices = view.findViewById(R.id.otherServicesTV)
        url = view.findViewById(R.id.gameLinkTV)
        trendBox = view.findViewById(R.id.trendingView)
        trailerVid = view.findViewById(R.id.trailerView)
        description = view.findViewById(R.id.descriptionTV)
        backButton = view.findViewById(R.id.gameBackBtn)
        progBar = view.findViewById(R.id.progressBar)

        //add the RadioGroup for selecting which list to add to
        addToListBtnGroup = view.findViewById(R.id.listAdd_RG)

        errorMsg = view.findViewById(R.id.errorPopUp)

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        //what to do if either RadioButton is clicked
        addToListBtnGroup.setOnCheckedChangeListener { radGroup, checkedBtn ->
            if (checkedBtn == R.id.AddToGV_rb) {
                Toast.makeText(requireContext(), "Added to Game Vault", Toast.LENGTH_SHORT).show()
                //TODO: add the other stuff clicking "Add to Game Vault" is supposed to do
                // (add to the list (make sure their placement in the list is saved into the Game instance w/ the list name
                // (it's the listBelong map parameter)) and do a push notif)
            } else if (checkedBtn == R.id.AddToWL_rb) {
                Toast.makeText(requireContext(), "Added to WishList", Toast.LENGTH_SHORT).show()
                //TODO: add the other stuff clicking "Add to Wishlist" is supposed to do
                // (add to the list (make sure their placement in the list is saved into the Game instance w/ the list name
                // (it's the listBelong map parameter)) and do a push notif)
            }

        }

        //webView setup
        trailerVid.settings.javaScriptEnabled = true
        trailerVid.settings.domStorageEnabled = true
        trailerVid.webChromeClient = WebChromeClient()
        trailerVid.webViewClient = WebViewClient()

        url.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.text.toString()))
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open URL: ${url.text}")
                errorMsg.text = "Couldn't open site link :("
                errorMsg.visibility = View.VISIBLE
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //make sure all conditional views are gone at start
        errorMsg.visibility = View.GONE
        trendBox.visibility = View.GONE //TODO: update this once TRENDING ALGO is figured out
        progBar.visibility = View.GONE

        setupObservers()

        val gameName = arguments?.getString("ARG_GAME_NAME")
        if (gameName.isNullOrEmpty()) {
            errorMsg.text = "No game name provided!"
            errorMsg.visibility = View.VISIBLE
            Log.e(TAG, "UI Error: ${errorMsg.text}")
            return
        }
        viewModel.fetchGameDetails(gameName)
    }

    override fun onPause() {
        super.onPause()
        trailerVid.onPause()
    }

    override fun onResume() {
        super.onResume()
        trailerVid.onResume()
    }

    override fun onDestroyView() { //to prevent memory leaks
        trailerVid.loadUrl("about:blank")
        trailerVid.clearHistory()
        trailerVid.removeAllViews()
        trailerVid.destroy()
        super.onDestroyView()
    }

    private fun setupObservers() {
        // Observe game object for content updates
        viewModel.gameObj.observe(viewLifecycleOwner, Observer { game ->
            if (game != null) {
                //display game details
                title.text = game.name
                rating.text = String.format("Rating: %s", game.rating.toString())
                debutDate.text = String.format("Release Date: %s", game.releaseDate)
                genres.text = String.format("Genres: %s", game.genreTag.joinToString(", "))
                themes.text = String.format("Themes: %s", game.themeTag.joinToString(", "))
                gameModes.text = String.format("Game Mode(s): %s", game.gameModeTag.joinToString(", "))
                platforms.text = String.format("Platform(s): %s", game.platformTag.joinToString(", "))
                otherServices.text = String.format("Also Available at: %s", game.otherServicesTag.joinToString(", "))
                url.text = game.website
                description.text = game.descr
                trendBox.visibility = if (game.trending) View.VISIBLE else View.GONE

                //display video YouTube embed
                if (game.trailerLink.isNotEmpty()) {
                    trailerVid.loadUrl(game.trailerLink)
                    trailerVid.visibility = View.VISIBLE
                } else {
                    val vidLayout = trailerVid.layoutParams as ConstraintLayout.LayoutParams
                    vidLayout.verticalBias = 0.toFloat()
                    trailerVid.visibility = View.GONE
                }

                //hide progress bar & clear lingering errors
                errorMsg.visibility = View.GONE
                progBar.visibility = View.GONE
            } else if (viewModel.isLoading.value == false) {
                errorMsg.text = "Game details couldn't be loaded or found."
                errorMsg.visibility = View.VISIBLE
                progBar.visibility = View.GONE
            }
        })

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            // Use the errorPopUp view to indicate loading
            progBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) errorMsg.visibility = View.GONE
        })

        // Observe error messages
        viewModel.errMsg.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                progBar.visibility = View.GONE
                errorMsg.text = errorMessage
                errorMsg.visibility = View.VISIBLE
                Log.e(TAG, "UI Error: ${errorMsg.text}")
            } else if (viewModel.gameObj.value == null && viewModel.isLoading.value == false) {
                // Clear the error view if data is available or loading is active
                errorMsg.visibility = View.GONE
            }
        })
    }

    // Companion object for creating the fragment instance with arguments
    companion object {
        const val ARG_GAME_NAME = "ARG_GAME_NAME" // Define constant for argument key
        fun newInstance(gameName: String) = GameDetailsFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_GAME_NAME, gameName)
            }
        }
    }
}