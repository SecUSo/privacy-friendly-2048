package org.secuso.privacyfriendly2048.helpers

import android.content.Context
import android.preference.PreferenceManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import org.secuso.privacyfriendly2048.R

@ColorRes
fun GetColorRes(ctx: Context, res: Int): Int {
    val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

    val style: Int

    when (prefs.getString("pref_color", "1")) {
        "2" -> style = R.style.ThemeOverlay_ColorSchemeOriginal
        else -> style = R.style.ThemeOverlay_ColorSchemeDefault
    }

    val ta = ctx.obtainStyledAttributes(style, intArrayOf(res))
    val resolved = ta.getInt(0, 0)
    ta.recycle()

    return resolved
}

@ColorInt
fun GetColorInt(ctx: Context, res: Int): Int {
    // TODO: Not sure why this works, investigate
    return GetColorRes(ctx, res).toInt()
}