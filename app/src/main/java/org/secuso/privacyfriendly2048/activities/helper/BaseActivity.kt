/*
 This file is part of Privacy Friendly 2048. This app implements the functions of the
 game 2048 in a privacy friendly version.

 Privacy Friendly 2048 is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly App Example is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly App Example. If not, see <http://www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendly2048.activities.helper

import android.view.View
import androidx.core.content.ContextCompat
import org.secuso.pfacore.model.DrawerMenu
import org.secuso.pfacore.ui.activities.DrawerActivity
import org.secuso.privacyfriendly2048.R
import org.secuso.privacyfriendly2048.activities.MainActivity
import org.secuso.privacyfriendly2048.activities.StatsActivity

/**
 * @author Christopher Beckmann, Karola Marky, Patrick Schneider
 * @version 20241028
 * This class is a parent class of all activities that can be accessed from the
 * Navigation Drawer (example see MainActivity.kt)
 *
 * The default NavigationDrawer functionality is implemented in this class.
 */
abstract class BaseActivity : DrawerActivity() {

    override fun setContentView(layoutResID: Int) {
        super.setContent(layoutResID)
    }

    override fun setContentView(view: View) {
        super.setContent(view)
    }

    override fun drawer() = DrawerMenu.build {
        section {
            activity {
                name = ContextCompat.getString(this@BaseActivity, R.string.action_main)
                icon = R.drawable.ic_menu_home
                clazz = MainActivity::class.java
            }
            activity {
                name = ContextCompat.getString(this@BaseActivity, R.string.action_stat)
                icon = R.drawable.ic_menu_stat
                clazz = StatsActivity::class.java
            }
        }
        defaultDrawerSection(this)
    }
}
