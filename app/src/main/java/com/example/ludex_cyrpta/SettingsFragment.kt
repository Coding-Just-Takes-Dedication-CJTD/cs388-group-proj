package com.example.ludex_cyrpta

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private lateinit var themePrefs: ThemePreferences
    private lateinit var steamPrefs: SteamPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themePrefs = ThemePreferences(requireContext())
        steamPrefs = SteamPreferences(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val darkSwitch = view.findViewById<Switch>(R.id.switch_darkmode)
        val notifSwitch = view.findViewById<Switch>(R.id.switch_notifications)
        val emailSwitch = view.findViewById<Switch>(R.id.switch_email_updates)

        val steamStatusText = view.findViewById<TextView>(R.id.steamStatusText)
        val steamButton = view.findViewById<Button>(R.id.steamLinkBtn)

        darkSwitch.isChecked = themePrefs.isDarkModeEnabled()

        darkSwitch.setOnCheckedChangeListener { _, isChecked ->
            themePrefs.setDarkMode(isChecked, requireActivity())
        }

        notifSwitch.setOnCheckedChangeListener { _, _ -> }
        emailSwitch.setOnCheckedChangeListener { _, _ -> }

        fun updateSteamUI() {
            if (steamPrefs.isLinked()) {
                steamStatusText.text = "Steam: Linked"
                steamButton.text = "Unlink"
            } else {
                steamStatusText.text = "Steam: Not Linked"
                steamButton.text = "Link"
            }
        }

        updateSteamUI()

        steamButton.setOnClickListener {
            if (steamPrefs.isLinked()) {
                steamPrefs.setSteamId(null)
                updateSteamUI()
            } else {
                startSteamLogin()
            }
        }
    }

    private fun startSteamLogin() {
        val intent = Intent(requireContext(), SteamLoginActivity::class.java)
        startActivityForResult(intent, 5001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 5001 && resultCode == Activity.RESULT_OK) {
            val steamId = data?.getStringExtra("steam_id")
            if (steamId != null) {
                steamPrefs.setSteamId(steamId)
                view?.findViewById<TextView>(R.id.steamStatusText)?.text = "Steam: Linked"
                view?.findViewById<Button>(R.id.steamLinkBtn)?.text = "Unlink"
            }
        }
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}
