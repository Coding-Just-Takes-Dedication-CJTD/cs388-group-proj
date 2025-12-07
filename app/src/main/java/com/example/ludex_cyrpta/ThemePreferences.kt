package com.example.ludex_cyrpta

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class ThemePreferences(context: Context) {

    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    fun isDarkModeEnabled(): Boolean {
        return prefs.getBoolean("dark_mode", false)
    }

    fun setDarkMode(enabled: Boolean, activity: Activity) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()

        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // SAFE RESTART — fixes freezing and flickering
        activity.finish()
        activity.startActivity(activity.intent)
    }
}
