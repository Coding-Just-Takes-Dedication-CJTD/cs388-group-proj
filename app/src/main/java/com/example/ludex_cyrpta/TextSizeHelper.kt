package com.example.ludex_cyrpta

import android.content.Context
import android.widget.TextView

object TextSizeHelper {

    fun applyTextSize(textView: TextView, context: Context, prefs: TextSizePreferences) {

        val sizeRes = when (prefs.getSize()) {
            TextSizePreferences.SIZE_SMALL -> R.dimen.text_small
            TextSizePreferences.SIZE_MEDIUM -> R.dimen.text_medium
            TextSizePreferences.SIZE_LARGE -> R.dimen.text_large
            else -> R.dimen.text_medium
        }

        val sizePx = context.resources.getDimension(sizeRes)
        textView.textSize = sizePx / context.resources.displayMetrics.scaledDensity
    }
}
