package com.example.ludex_cyrpta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private lateinit var themePrefs: ThemePreferences
    private lateinit var steamPrefs: SteamPreferences
    private lateinit var textPrefs: TextSizePreferences

    private var steamStatusText: TextView? = null
    private var steamButton: Button? = null

    private val steamLoginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val steamId = result.data?.getStringExtra("steam_id")

                if (steamId != null) {
                    lifecycleScope.launch {
                        val profile = SteamApi.getSteamProfile(steamId)
                        if (profile != null) {
                            steamPrefs.setSteamId(profile.steamId)
                            steamStatusText?.text = "Steam: ${profile.name}"
                            steamButton?.text = "Unlink"
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themePrefs = ThemePreferences(requireContext())
        steamPrefs = SteamPreferences(requireContext())
        textPrefs = TextSizePreferences(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val darkSwitch = view.findViewById<Switch>(R.id.switch_darkmode)
        val notifSwitch = view.findViewById<Switch>(R.id.switch_notifications)
        val emailSwitch = view.findViewById<Switch>(R.id.switch_email_updates)

        steamStatusText = view.findViewById(R.id.steamStatusText)
        steamButton = view.findViewById(R.id.steamLinkBtn)

        val smallBtn = view.findViewById<Button>(R.id.btn_text_small)
        val mediumBtn = view.findViewById<Button>(R.id.btn_text_medium)
        val largeBtn = view.findViewById<Button>(R.id.btn_text_large)

        updateSelectedSizeUI(smallBtn, mediumBtn, largeBtn)
        updateSteamUI()

        darkSwitch.isChecked = themePrefs.isDarkModeEnabled()
        darkSwitch.setOnCheckedChangeListener { _, isChecked ->
            themePrefs.setDarkMode(isChecked, requireActivity())
        }

        notifSwitch.setOnCheckedChangeListener { _, _ -> }
        emailSwitch.setOnCheckedChangeListener { _, _ -> }

        smallBtn.setOnClickListener {
            textPrefs.setSize(TextSizePreferences.SIZE_SMALL)
            updateSelectedSizeUI(smallBtn, mediumBtn, largeBtn)
            requireActivity().recreate()
        }

        mediumBtn.setOnClickListener {
            textPrefs.setSize(TextSizePreferences.SIZE_MEDIUM)
            updateSelectedSizeUI(smallBtn, mediumBtn, largeBtn)
            requireActivity().recreate()
        }

        largeBtn.setOnClickListener {
            textPrefs.setSize(TextSizePreferences.SIZE_LARGE)
            updateSelectedSizeUI(smallBtn, mediumBtn, largeBtn)
            requireActivity().recreate()
        }

        steamButton?.setOnClickListener {
            if (steamPrefs.isLinked()) {
                steamPrefs.setSteamId(null)
                updateSteamUI()
            } else {
                val intent = Intent(requireContext(), SteamLoginActivity::class.java)
                steamLoginLauncher.launch(intent)
            }
        }
    }

    private fun updateSteamUI() {
        val id = steamPrefs.getSteamId()
        if (id == null) {
            steamStatusText?.text = "Steam: Not Linked"
            steamButton?.text = "Link"
        } else {
            steamStatusText?.text = "Steam: Linked ($id)"
            steamButton?.text = "Unlink"
        }
    }

    private fun updateSelectedSizeUI(small: Button, medium: Button, large: Button) {
        val selected = textPrefs.getSize()

        val selectedColor = ContextCompat.getColor(requireContext(), R.color.purple_200)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.purple_700)

        small.setBackgroundColor(if (selected == TextSizePreferences.SIZE_SMALL) selectedColor else defaultColor)
        medium.setBackgroundColor(if (selected == TextSizePreferences.SIZE_MEDIUM) selectedColor else defaultColor)
        large.setBackgroundColor(if (selected == TextSizePreferences.SIZE_LARGE) selectedColor else defaultColor)
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}
