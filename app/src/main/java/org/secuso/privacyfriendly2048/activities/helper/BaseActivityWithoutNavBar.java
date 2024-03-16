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

package org.secuso.privacyfriendly2048.activities.helper;

import android.os.Bundle;
import android.os.Handler;

import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;
import androidx.core.app.TaskStackBuilder;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.KeyEvent;
import android.view.View;

import org.secuso.privacyfriendly2048.R;

/**
 * @author Christopher Beckmann, Karola Marky
 * @version 20171017
 * This class is a parent class of all activities that can be accessed from the
 * Navigation Drawer (example see MainActivity.java)
 *
 * The default NavigationDrawer functionality is implemented in this class. If you wish to inherit
 * the default behaviour, make sure the content view has a NavigationDrawer with the id 'nav_view',
 * the header should point to 'nav_header_main' and the menu should be loaded from 'main_drawer'.
 *
 * Also the main layout that holds the content of the activity should have the id 'main_content'.
 * This way it will automatically fade in and out every time a transition is happening.
 *
 */
public abstract class BaseActivityWithoutNavBar extends AppCompatActivity {


    static final int NAVDRAWER_LAUNCH_DELAY = 250;
    static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    protected Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        overridePendingTransition(0, 0);
    }

    @Override
    public void onResume() {
        super.onResume();

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

}
