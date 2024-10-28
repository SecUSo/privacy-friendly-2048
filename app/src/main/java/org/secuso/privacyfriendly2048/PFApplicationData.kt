package org.secuso.privacyfriendly2048

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.map
import org.secuso.pfacore.model.Theme
import org.secuso.pfacore.model.about.About
import org.secuso.pfacore.model.preferences.Preferable
import org.secuso.pfacore.model.preferences.settings.ISettingData
import org.secuso.pfacore.ui.PFData
import org.secuso.pfacore.ui.help.Help
import org.secuso.pfacore.ui.preferences.appPreferences
import org.secuso.pfacore.ui.preferences.settings.DeviceInformationOnErrorReport
import org.secuso.pfacore.ui.preferences.settings.PreferenceFirstTimeLaunch
import org.secuso.pfacore.ui.preferences.settings.SettingThemeSelector
import org.secuso.pfacore.ui.tutorial.buildTutorial

class PFApplicationData private constructor(context: Context) {

    lateinit var theme: ISettingData<String>
        private set
    lateinit var firstTimeLaunch: Preferable<Boolean>
        private set
    lateinit var includeDeviceDataInReport: Preferable<Boolean>
        private set
    lateinit var animationActivated: Preferable<Boolean>
        private set

    private val preferences = appPreferences(context) {
        preferences {
            firstTimeLaunch = PreferenceFirstTimeLaunch().build().invoke(this)
        }
        settings {
            category(ContextCompat.getString(context, R.string.settings_category_appearance)) {
                theme = SettingThemeSelector().build().invoke(this)
                animationActivated = switch {
                    key = "pref_animationActivated"
                    title { resource(R.string.settings_animation_title) }
                    summary { resource(R.string.settings_animation_summary) }
                    default = true
                    backup = true
                }
            }
            category(ContextCompat.getString(context, R.string.settings_category_report)) {
                includeDeviceDataInReport = DeviceInformationOnErrorReport().build().invoke(this)
            }
        }
    }

    private val help = Help.build(context) {

    }

    private val about = About(
        name = context.resources.getString(R.string.app_name),
        version = BuildConfig.VERSION_NAME,
        authors = context.resources.getString(R.string.about_author_names),
        repo = context.resources.getString(R.string.github)
    )

    private val tutorial = buildTutorial {

    }

    val data = PFData(
        about = about,
        help = help,
        settings = preferences.settings,
        tutorial = tutorial,
        theme = theme.state.map { Theme.valueOf(it) },
        firstLaunch = firstTimeLaunch,
        includeDeviceDataInReport = includeDeviceDataInReport,
    )

    companion object {
        private var _instance: PFApplicationData? = null
        fun instance(context: Context): PFApplicationData {
            if (_instance == null) {
                _instance = PFApplicationData(context)
            }
            return _instance!!
        }

    }
}


