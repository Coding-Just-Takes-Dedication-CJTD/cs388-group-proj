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
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer

private const val TAG = "GameDetailsFrag"

class GameDetailsFragment : Fragment() {

    private val viewModel: GameViewModel by viewModels()
    private lateinit var textPrefs: TextSizePreferences

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

    private lateinit var trailerVid: WebView
    private lateinit var backButton: Button
    private lateinit var addToListBtnGroup: RadioGroup
    private lateinit var progBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textPrefs = TextSizePreferences(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
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
        description = view.findViewById(R.id.descriptionTV)
        errorMsg = view.findViewById(R.id.errorPopUp)

        trailerVid = view.findViewById(R.id.trailerView)
        backButton = view.findViewById(R.id.gameBackBtn)
        addToListBtnGroup = view.findViewById(R.id.listAdd_RG)
        progBar = view.findViewById(R.id.progressBar)

        applyTextSizeScaling()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        errorMsg.visibility = View.GONE
        trendBox.visibility = View.GONE
        progBar.visibility = View.GONE

        setupObservers()

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        addToListBtnGroup.setOnCheckedChangeListener { _, checkedBtn ->
            if (checkedBtn == R.id.AddToGV_rb) {
                Toast.makeText(requireContext(), "Added to Game Vault", Toast.LENGTH_SHORT).show()
            } else if (checkedBtn == R.id.AddToWL_rb) {
                Toast.makeText(requireContext(), "Added to Wishlist", Toast.LENGTH_SHORT).show()
            }
        }

        trailerVid.settings.javaScriptEnabled = true
        trailerVid.settings.domStorageEnabled = true
        trailerVid.webChromeClient = WebChromeClient()
        trailerVid.webViewClient = WebViewClient()

        url.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.text.toString()))
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open URL")
                errorMsg.text = "Couldn't open site link :("
                errorMsg.visibility = View.VISIBLE
            }
        }

        val gameName = arguments?.getString("ARG_GAME_NAME")
        if (gameName.isNullOrEmpty()) {
            errorMsg.text = "No game name provided!"
            errorMsg.visibility = View.VISIBLE
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

    override fun onDestroyView() {
        trailerVid.loadUrl("about:blank")
        trailerVid.clearHistory()
        trailerVid.removeAllViews()
        trailerVid.destroy()
        super.onDestroyView()
    }

    private fun setupObservers() {
        viewModel.gameObj.observe(viewLifecycleOwner, Observer { game ->
            if (game != null) {
                title.text = game.name
                rating.text = "Rating: ${game.rating}"
                debutDate.text = "Release Date: ${game.releaseDate}"
                genres.text = "Genres: ${game.genreTag.joinToString(", ")}"
                themes.text = "Themes: ${game.themeTag.joinToString(", ")}"
                gameModes.text = "Game Mode(s): ${game.gameModeTag.joinToString(", ")}"
                platforms.text = "Platform(s): ${game.platformTag.joinToString(", ")}"
                otherServices.text = "Also Available at: ${game.otherServicesTag.joinToString(", ")}"
                url.text = game.website
                description.text = game.descr
                trendBox.visibility = if (game.trending) View.VISIBLE else View.GONE

                if (game.trailerLink.isNotEmpty()) {
                    trailerVid.loadUrl(game.trailerLink)
                    trailerVid.visibility = View.VISIBLE
                } else {
                    val params = trailerVid.layoutParams as ConstraintLayout.LayoutParams
                    params.verticalBias = 0f
                    trailerVid.visibility = View.GONE
                }

                errorMsg.visibility = View.GONE
                progBar.visibility = View.GONE
            } else if (viewModel.isLoading.value == false) {
                errorMsg.text = "Game details couldn't be loaded."
                errorMsg.visibility = View.VISIBLE
                progBar.visibility = View.GONE
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            progBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) errorMsg.visibility = View.GONE
        })

        viewModel.errMsg.observe(viewLifecycleOwner, Observer { msg ->
            if (!msg.isNullOrEmpty()) {
                progBar.visibility = View.GONE
                errorMsg.text = msg
                errorMsg.visibility = View.VISIBLE
            }
        })
    }

    private fun applyTextSizeScaling() {
        val factor = textPrefs.getScale()

        val textViews = listOf(
            title, rating, debutDate, genres, themes,
            gameModes, platforms, otherServices, url,
            trendBox, description, errorMsg
        )

        textViews.forEach { tv ->
            tv.textSize = tv.textSize * factor / resources.displayMetrics.scaledDensity
        }
    }

    companion object {
        const val ARG_GAME_NAME = "ARG_GAME_NAME"
        fun newInstance(gameName: String) = GameDetailsFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_GAME_NAME, gameName)
            }
        }
    }
}
