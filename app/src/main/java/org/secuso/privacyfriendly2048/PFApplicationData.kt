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
    lateinit var currentPage: Preferable<Int>
        private set
    lateinit var prefColorScheme: Preferable<String>
        private set

    private val preferences = appPreferences(context) {
        preferences {
            firstTimeLaunch = PreferenceFirstTimeLaunch().build().invoke(this)
            currentPage = preference {
                key = "currentPage"
                default = 0
                backup = true
            }
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
                prefColorScheme = radio {
                    key = "pref_color"
                    default = "1"
                    backup = true
                    entries {
                        entries(R.array.color_list)
                        values(resources.getStringArray(R.array.color_list_values).toList())
                    }
                    title { resource(R.string.settings_color) }
                    summary { transform { state, value -> state.entries.find { it.value == value }!!.entry } }
                }
            }
            category(ContextCompat.getString(context, R.string.settings_category_report)) {
                includeDeviceDataInReport = DeviceInformationOnErrorReport().build().invoke(this)
            }
        }
    }

    private val help = Help.build(context) {
        listOf(
            R.string.help_whatis to R.string.help_whatis_answer,
            R.string.help_privacy to R.string.help_privacy_answer,
            R.string.help_permission to R.string.help_permission_answer,
            R.string.help_play to R.string.help_play_answer,
            R.string.help_play_how to R.string.help_play_how_answer,
            R.string.help_play_add to R.string.help_play_add_answer,
            R.string.help_tip to R.string.help_tip_answer,
            R.string.help_undo to R.string.help_undo_answer,
            R.string.help_color to R.string.help_color_answer,
        ).forEach { (q, a) ->
            item {
                title { resource(q) }
                description { resource(a) }
            }
        }

    }

    private val about = About(
        name = context.resources.getString(R.string.app_name),
        version = BuildConfig.VERSION_NAME,
        authors = context.resources.getString(R.string.about_author_names),
        repo = context.resources.getString(R.string.github)
    )

    private val tutorial = buildTutorial {
        stage {
            title = ContextCompat.getString(context, R.string.Tutorial_Titel)
            images = listOf(R.id.image1)
            description = ContextCompat.getString(context, R.string.Tutorial_Instruction)
        }
        stage {
            title = ContextCompat.getString(context, R.string.Tutorial_Titel_Move)
            images = listOf(R.id.image2)
            description = ContextCompat.getString(context, R.string.Tutorial_Move)
        }
        stage {
            title = ContextCompat.getString(context, R.string.Tutorial_Titel_Move)
            images = listOf(R.id.image3)
            description = ContextCompat.getString(context, R.string.Tutorial_Move)
        }
        stage {
            title = ContextCompat.getString(context, R.string.Tutorial_Titel_Add)
            images = listOf(R.id.image4)
            description = ContextCompat.getString(context, R.string.Tutorial_Add)
        }
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


