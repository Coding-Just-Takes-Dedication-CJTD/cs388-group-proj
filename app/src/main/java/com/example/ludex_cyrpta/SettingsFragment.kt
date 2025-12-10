package com.example.ludex_cyrpta

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.core.content.ContextCompat
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private lateinit var themePrefs: ThemePreferences
    private lateinit var textPrefs: TextSizePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themePrefs = ThemePreferences(requireContext())
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

        val btnSmall = view.findViewById<Button>(R.id.btn_text_small)
        val btnMedium = view.findViewById<Button>(R.id.btn_text_medium)
        val btnLarge = view.findViewById<Button>(R.id.btn_text_large)

        val steamBtn = view.findViewById<Button>(R.id.steamLinkBtn)
        val steamStatus = view.findViewById<TextView>(R.id.steamStatusText)

        updateTextSizeButtons(btnSmall, btnMedium, btnLarge)

        darkSwitch.isChecked = themePrefs.isDarkModeEnabled()
        darkSwitch.setOnCheckedChangeListener { _, enabled ->
            themePrefs.setDarkMode(enabled)

            val act = requireActivity()

            act.finish()
            act.overridePendingTransition(0, 0)
            startActivity(act.intent)
            act.overridePendingTransition(0, 0)
        }


        notifSwitch.setOnCheckedChangeListener { _, _ -> }
        emailSwitch.setOnCheckedChangeListener { _, _ -> }

        btnSmall.setOnClickListener {
            textPrefs.setSize(TextSizePreferences.SIZE_SMALL)
            updateTextSizeButtons(btnSmall, btnMedium, btnLarge)
            requireActivity().recreate()
        }

        btnMedium.setOnClickListener {
            textPrefs.setSize(TextSizePreferences.SIZE_MEDIUM)
            updateTextSizeButtons(btnSmall, btnMedium, btnLarge)
            requireActivity().recreate()
        }

        btnLarge.setOnClickListener {
            textPrefs.setSize(TextSizePreferences.SIZE_LARGE)
            updateTextSizeButtons(btnSmall, btnMedium, btnLarge)
            requireActivity().recreate()
        }

        steamBtn.setOnClickListener {
            openSteamLogin()
        }
    }

    private fun updateTextSizeButtons(small: Button, medium: Button, large: Button) {
        val selected = textPrefs.getSize()
        val active = ContextCompat.getColor(requireContext(), R.color.purple_200)
        val inactive = ContextCompat.getColor(requireContext(), R.color.purple_700)

        small.setBackgroundColor(if (selected == TextSizePreferences.SIZE_SMALL) active else inactive)
        medium.setBackgroundColor(if (selected == TextSizePreferences.SIZE_MEDIUM) active else inactive)
        large.setBackgroundColor(if (selected == TextSizePreferences.SIZE_LARGE) active else inactive)
    }

    private fun openSteamLogin() {
        val steamUrl =
            "https://steamcommunity.com/openid/login" +
                    "?openid.ns=http://specs.openid.net/auth/2.0" +
                    "&openid.mode=checkid_setup" +
                    "&openid.return_to=ludex://steam-auth" +
                    "&openid.realm=ludex://steam-auth" +
                    "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select" +
                    "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(steamUrl))
        requireContext().startActivity(intent)
    }

    fun onSteamLinked(steamId: String) {
        view?.findViewById<TextView>(R.id.steamStatusText)?.text = "Steam ID: $steamId"
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}
