package com.example.ludex_cyrpta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class GameVaultFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.game_vault_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTextSizeScaling(view)
    }

    private fun applyTextSizeScaling(root: View) {
        val scale = TextSizePreferences(requireContext()).getScale()

        val textViews = listOf(
            root.findViewById<TextView>(R.id.gvTitle),
            root.findViewById<TextView>(R.id.vaultContentPlacehldr)
        )

        textViews.forEach { tv ->
            tv?.let {
                it.textSize = it.textSize / resources.displayMetrics.scaledDensity * scale
            }
        }
    }

    companion object {
        fun newInstance(): GameVaultFragment = GameVaultFragment()
    }
}
