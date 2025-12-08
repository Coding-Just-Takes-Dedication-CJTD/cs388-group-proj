package com.example.ludex_cyrpta

import android.content.Context

class TextSizePreferences(context: Context) {

    private val prefs = context.getSharedPreferences("text_size_prefs", Context.MODE_PRIVATE)

    companion object {
        const val SIZE_SMALL = 1
        const val SIZE_MEDIUM = 2
        const val SIZE_LARGE = 3
    }

    fun setSize(size: Int) {
        prefs.edit().putInt("text_size", size).apply()
    }

    fun getSize(): Int {
        return prefs.getInt("text_size", SIZE_MEDIUM)
    }

    fun getScale(): Float {
        return when (getSize()) {
            SIZE_SMALL -> 0.85f
            SIZE_MEDIUM -> 1.0f
            SIZE_LARGE -> 1.25f
            else -> 1.0f
        }
    }
}
