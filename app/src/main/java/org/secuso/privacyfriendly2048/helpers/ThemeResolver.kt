package org.secuso.privacyfriendly2048.helpers

import android.content.Context
import android.preference.PreferenceManager
import androidx.annotation.StyleRes
import org.secuso.privacyfriendly2048.R

@StyleRes
fun GetThemeRes(ctx: Context): Int {
    val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

    return when (prefs.getString("currentTheme", "system")) {
        "light" -> R.style.AppThemeLight
        "dark" -> R.style.AppThemeDark
        else -> R.style.AppTheme
    }
}