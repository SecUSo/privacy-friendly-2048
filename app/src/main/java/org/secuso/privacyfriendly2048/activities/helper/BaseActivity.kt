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

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.TaskStackBuilder
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import org.secuso.privacyfriendly2048.R
import org.secuso.privacyfriendly2048.activities.MainActivity
import org.secuso.privacyfriendly2048.activities.StatsActivity

/**
 * @author Christopher Beckmann, Karola Marky
 * @version 20171017
 * This class is a parent class of all activities that can be accessed from the
 * Navigation Drawer (example see MainActivity.java)
 *
 *
 * The default NavigationDrawer functionality is implemented in this class. If you wish to inherit
 * the default behaviour, make sure the content view has a NavigationDrawer with the id 'nav_view',
 * the header should point to 'nav_header_main' and the menu should be loaded from 'main_drawer'.
 *
 *
 * Also the main layout that holds the content of the activity should have the id 'main_content'.
 * This way it will automatically fade in and out every time a transition is happening.
 */
abstract class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    // Navigation drawer:
    var mDrawerLayout: DrawerLayout? = null
    private var mNavigationView: NavigationView? = null

    // Helper
    private var mHandler: Handler? = null
    protected var mSharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        mHandler = Handler()

        overridePendingTransition(0, 0)
    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    protected abstract val navigationDrawerID: Int

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId

        return goToNavigationItem(itemId)
    }


    protected fun goToNavigationItem(itemId: Int): Boolean {
        if (itemId == navigationDrawerID) {
            // just close drawer because we are already in this activity
            mDrawerLayout!!.closeDrawer(GravityCompat.START)
            return true
        }

        // delay transition so the drawer can close
        mHandler!!.postDelayed({ callDrawerItem(itemId) }, NAVDRAWER_LAUNCH_DELAY.toLong())

        mDrawerLayout!!.closeDrawer(GravityCompat.START)

        selectNavigationItem(itemId)

        // fade out the active activity
        val mainContent = findViewById<View>(R.id.main_content)
        mainContent?.animate()?.alpha(0f)?.setDuration(MAIN_CONTENT_FADEOUT_DURATION.toLong())
        return true
    }

    // set active navigation item
    private fun selectNavigationItem(itemId: Int) {
        for (i in 0 until mNavigationView!!.menu.size()) {
            val b = itemId == mNavigationView!!.menu.getItem(i).itemId
            mNavigationView!!.menu.getItem(i).setChecked(b)
        }
    }

    /**
     * Enables back navigation for activities that are launched from the NavBar. See
     * `AndroidManifest.xml` to find out the parent activity names for each activity.
     *
     * @param intent
     */
    private fun createBackStack(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val builder = TaskStackBuilder.create(this)
            builder.addNextIntentWithParentStack(intent)
            builder.startActivities()
        } else {
            startActivity(intent)
            finish()
        }
    }

    /**
     * This method manages the behaviour of the navigation drawer
     * Add your menu items (ids) to res/menu/activity_main_drawer.xml
     *
     * @param itemId Item that has been clicked by the user
     */
    private fun callDrawerItem(itemId: Int) {
        val intent: Intent

        when (itemId) {
            R.id.nav_example -> {
                intent = Intent(this, MainActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }

            R.id.nav_tutorial -> {
                intent = Intent(this, TutorialActivity::class.java)
                createBackStack(intent)
            }

            R.id.nav_about -> {
                intent = Intent(this, AboutActivity::class.java)
                createBackStack(intent)
            }

            R.id.nav_help -> {
                intent = Intent(this, HelpActivity::class.java)
                createBackStack(intent)
            }

            R.id.nav_statistics -> {
                intent = Intent(this, StatsActivity::class.java)
                createBackStack(intent)
            }

            R.id.nav_settings -> {
                intent = Intent(this, SettingsActivity::class.java)
                intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment::class.java.getName())
                intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true)
                createBackStack(intent)
            }

            else -> {}
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        if (supportActionBar == null) {
            setSupportActionBar(toolbar)
        }

        mDrawerLayout = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
            this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        mDrawerLayout!!.addDrawerListener(toggle)

        toggle.syncState()

        mNavigationView = findViewById<View>(R.id.nav_view) as NavigationView
        mNavigationView!!.setNavigationItemSelectedListener(this)

        selectNavigationItem(navigationDrawerID)

        val mainContent = findViewById<View>(R.id.main_content)
        if (mainContent != null) {
            mainContent.alpha = 0f
            mainContent.animate().alpha(1f).setDuration(MAIN_CONTENT_FADEIN_DURATION.toLong())
        }
    }


    companion object {
        // delay to launch nav drawer item, to allow close animation to play
        const val NAVDRAWER_LAUNCH_DELAY: Int = 250

        // fade in and fade out durations for the main content when switching between
        // different Activities of the app through the Nav Drawer
        const val MAIN_CONTENT_FADEOUT_DURATION: Int = 150
        const val MAIN_CONTENT_FADEIN_DURATION: Int = 250
    }
}
