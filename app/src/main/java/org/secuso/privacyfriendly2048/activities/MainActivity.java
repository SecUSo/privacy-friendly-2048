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

package org.secuso.privacyfriendly2048.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;

import org.secuso.privacyfriendly2048.R;
import org.secuso.privacyfriendly2048.activities.helper.BaseActivity;
import org.secuso.privacyfriendly2048.helpers.FirstLaunchManager;

import java.io.File;

/**
 * The MainActivity is the activity from which the game in each mode is started.
 * Therefore a ViewPager is used to depict pictures of the four mode.
 * At the bottom of the activity there are two buttons, which start a new game (START NEW GAME) and continue the previous game (CONTINUE GAME).
 * The CONTINUE GAME button is greyed out and is not selectable, if there is no previous played game available in this mode.
 *
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180910
 */
public class MainActivity extends BaseActivity {

    private ViewPager viewPager;
    private MainActivity.MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private ImageButton btnPrev, btnNext;
    private FirstLaunchManager firstLaunchManager;
    private int currentPage = 0;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private String mypref = "myPref";

    private int[] layouts = new int[]{
            R.layout.choose_slide1,
            R.layout.choose_slide2,
            R.layout.choose_slide3,
            R.layout.choose_slide4,
    };
    private boolean[] gameResumeable = new boolean[]{
            false,
            false,
            false,
            false
    };

    @Override
    protected void onStart() {
        super.onStart();
        preferences = getApplicationContext().getSharedPreferences(mypref, Context.MODE_PRIVATE);
        editor = preferences.edit();
        currentPage = preferences.getInt("currentPage", 0);
        viewPager.setCurrentItem(currentPage);
        updateButtons(currentPage);
        updateMovingButtons(currentPage);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        overridePendingTransition(0, 0);

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        firstLaunchManager = new FirstLaunchManager(this);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        btnPrev = (ImageButton) findViewById(R.id.btn_prev);
        btnNext = (ImageButton) findViewById(R.id.btn_next);


        //checking resumable
        File directory = getFilesDir();
        File[] files = directory.listFiles();

        for (int i = 0; i < files.length; i++) {
            Log.i("files", files[i].getName());
            for (int j = 0; j < gameResumeable.length; j++) {
                if (files[i].getName().equals("state" + (j + 4) + ".txt"))
                    gameResumeable[j] = true;
            }
        }


        // adding bottom dots
        addBottomDots(0);

        // making notification bar transparent
        changeStatusBarColor();

        myViewPagerAdapter = new MainActivity.MyViewPagerAdapter();

        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = getItem(-1);
                if (current >= 0) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                int current = getItem(+1);
                if (current < layouts.length) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {
                }
            }
        });
    }

    private void addListener(Button b1, Button b2, int n) {
        final int temp = n;
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra("n", temp);
                intent.putExtra("points", 0);
                intent.putExtra("new", true);
                intent.putExtra("filename", "state" + temp + ".txt");
                intent.putExtra("undo", false);
                createBackStack(intent);
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra("n", temp);
                intent.putExtra("new", false);
                intent.putExtra("filename", "state" + temp + ".txt");
                intent.putExtra("undo", false);
                createBackStack(intent);
            }
        });
    }

    private void createBackStack(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntentWithParentStack(intent);
            builder.startActivities();
        } else {
            startActivity(intent);
            finish();
        }
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int activeColor = ContextCompat.getColor(this, R.color.dot_light_screen);
        int inactiveColor = ContextCompat.getColor(this, R.color.dot_dark_screen);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(inactiveColor);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(activeColor);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
            currentPage = position;
            editor.putInt("currentPage", currentPage);
            editor.commit();
            updateButtons(position);

            updateMovingButtons(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    public void updateMovingButtons(int position) {
        if (position == layouts.length - 1) {
            // last page. make button text to GOT IT
            btnNext.setVisibility(View.INVISIBLE);
        } else {
            // still pages are left
            btnNext.setVisibility(View.VISIBLE);
        }
        if (position == 0) {
            // last page. make button text to GOT IT
            btnPrev.setVisibility(View.INVISIBLE);
        } else {
            // still pages are left
            btnPrev.setVisibility(View.VISIBLE);
        }
    }

    public void updateButtons(int position) {
        Button newGameButton = MainActivity.this.findViewById(R.id.button_newGame);
        Button continueButton = MainActivity.this.findViewById(R.id.button_continueGame);
        try {
            if (gameResumeable[position])
                continueButton.setBackgroundResource(R.drawable.standalone_button);
            else
                continueButton.setBackgroundResource(R.drawable.inactive_button);

            continueButton.setEnabled(gameResumeable[position]);
        } catch (Exception aie) {
            aie.printStackTrace();
        }
        addListener(newGameButton, continueButton, position + 4);
    }

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);
            ImageView imageView;
            switch (position) {
                case 0:
                    imageView = (ImageView) findViewById(R.id.main_menu_img1);
                    if (PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("pref_color", "1").equals("1"))
                        Glide.with(MainActivity.this).load(R.drawable.layout4x4_s).into(imageView);
                    else
                        Glide.with(MainActivity.this).load(R.drawable.layout4x4_o).into(imageView);
                    break;
                case 1:
                    imageView = (ImageView) findViewById(R.id.main_menu_img2);
                    if (PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("pref_color", "1").equals("1"))
                        Glide.with(MainActivity.this).load(R.drawable.layout5x5_s).into(imageView);
                    else
                        Glide.with(MainActivity.this).load(R.drawable.layout5x5_o).into(imageView);
                    break;
                case 2:
                    imageView = (ImageView) findViewById(R.id.main_menu_img3);
                    if (PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("pref_color", "1").equals("1"))
                        Glide.with(MainActivity.this).load(R.drawable.layout6x6_s).into(imageView);
                    else
                        Glide.with(MainActivity.this).load(R.drawable.layout6x6_o).into(imageView);
                    break;
                case 3:
                    imageView = (ImageView) findViewById(R.id.main_menu_img4);
                    if (PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("pref_color", "1").equals("1"))
                        Glide.with(MainActivity.this).load(R.drawable.layout7x7_s).into(imageView);
                    else
                        Glide.with(MainActivity.this).load(R.drawable.layout7x7_o).into(imageView);
                    break;
            }
            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    /**
     * This method connects the Activity to the menu item
     *
     * @return ID of the menu item it belongs to
     */
    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_example;
    }

}
