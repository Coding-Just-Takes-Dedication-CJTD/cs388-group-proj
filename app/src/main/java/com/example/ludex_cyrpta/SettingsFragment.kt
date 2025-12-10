package com.example.ludex_cyrpta

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val steamBtn = view.findViewById<Button>(R.id.steamLinkBtn)
        val steamStatus = view.findViewById<TextView>(R.id.steamStatusText)

        steamBtn.setOnClickListener {
            openSteamLogin()
        }
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
}
