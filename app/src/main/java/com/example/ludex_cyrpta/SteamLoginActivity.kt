package com.example.ludex_cyrpta

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class SteamLoginActivity : AppCompatActivity() {

    private val redirectUrl = "myapp://steam/callback"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true

        val openIdUrl =
            "https://steamcommunity.com/openid/login" +
                    "?openid.ns=http://specs.openid.net/auth/2.0" +
                    "&openid.mode=checkid_setup" +
                    "&openid.return_to=$redirectUrl" +
                    "&openid.realm=$redirectUrl" +
                    "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select" +
                    "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select"

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {

                if (url != null && url.startsWith(redirectUrl)) {
                    val steamId = extractSteamId(url)

                    val resultIntent = Intent()
                    resultIntent.putExtra("steam_id", steamId)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                    return true
                }

                return false
            }
        }

        webView.loadUrl(openIdUrl)
    }

    private fun extractSteamId(url: String): String? {
        val uri = Uri.parse(url)
        val claimedId = uri.getQueryParameter("openid.claimed_id")
        return claimedId?.substringAfterLast("/")
    }
}
