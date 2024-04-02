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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

import org.secuso.privacyfriendly2048.R;
import org.secuso.privacyfriendly2048.activities.helper.BaseActivity;
import org.secuso.privacyfriendly2048.activities.helper.GameStatistics;

import java.io.File;
import java.io.FileInputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * The game statistics of the four modes are loaded and shown with a ViePager in this activity.
 *
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180910
 */
public class StatsActivity extends BaseActivity {

    private int[] layouts = new int[]{
            R.layout.fragment_stats1,
            R.layout.fragment_stats2,
            R.layout.fragment_stats3,
            R.layout.fragment_stats4,
    };

    String[] TABNAMES = {"4x4", "5x5", "6x6", "7x7"};

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private StatsActivity.MyViewPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        //actionBar.setTitle(R.string.menu_highscore);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#024265")));

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new MyViewPagerAdapter();

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.main_content);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        //tabLayout.setTabTextColors(Color.WHITE,Color.YELLOW);
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_statistics;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stats, menu);
        //getMenuInflater().inflate(R.menu.menu_stats, menu);
        return true;
        //return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.action_reset:
                //    SaveLoadStatistics.resetStats(this);
                //    mSectionsPagerAdapter.refresh(this);

                resetGameStatistics();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void resetGameStatistics() {
        for (int n = 4; n <= 7; n++) {
            try {
                File file = new File(getFilesDir(), "statistics" + n + ".txt");
                file.delete();
            } catch (Exception e) {

            }
        }
        finish();
        startActivity(getIntent());
    }


    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TABNAMES[position];
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);
            ImageView img = new ImageView(StatsActivity.this);
            TextView highestNumber = new TextView(StatsActivity.this);
            TextView timePlayed = new TextView(StatsActivity.this);
            TextView undo = new TextView(StatsActivity.this);
            TextView moves_L = new TextView(StatsActivity.this);
            TextView moves_R = new TextView(StatsActivity.this);
            TextView moves_T = new TextView(StatsActivity.this);
            TextView moves_D = new TextView(StatsActivity.this);
            TextView moves = new TextView(StatsActivity.this);
            TextView tpm = new TextView(StatsActivity.this);
            TextView rekord = new TextView(StatsActivity.this);
            switch (position) {
                case 0:
                    highestNumber = findViewById(R.id.highest_number1);
                    timePlayed = findViewById(R.id.time_played1);
                    undo = findViewById(R.id.undo_number1);
                    moves_D = findViewById(R.id.moves_D1);
                    moves_L = findViewById(R.id.moves_L1);
                    moves_R = findViewById(R.id.moves_R1);
                    moves_T = findViewById(R.id.moves_T1);
                    moves = findViewById(R.id.moves_All1);
                    tpm = findViewById(R.id.time_swipes1);
                    rekord = findViewById(R.id.highest_score1);
                    img = findViewById(R.id.stat_img1);
                    if (PreferenceManager.getDefaultSharedPreferences(StatsActivity.this).getString("pref_color", "1").equals("1"))
                        Glide.with(StatsActivity.this).load(R.drawable.layout4x4_s).into(img);
                    else
                        Glide.with(StatsActivity.this).load(R.drawable.layout4x4_o).into(img);
                    break;
                case 1:
                    highestNumber = findViewById(R.id.highest_number2);
                    timePlayed = findViewById(R.id.time_played2);
                    undo = findViewById(R.id.undo_number2);
                    moves_D = findViewById(R.id.moves_D2);
                    moves_L = findViewById(R.id.moves_L2);
                    moves_R = findViewById(R.id.moves_R2);
                    moves_T = findViewById(R.id.moves_T2);
                    moves = findViewById(R.id.moves_All2);
                    tpm = findViewById(R.id.time_swipes2);
                    rekord = findViewById(R.id.highest_score2);
                    img = findViewById(R.id.stat_img2);
                    if (PreferenceManager.getDefaultSharedPreferences(StatsActivity.this).getString("pref_color", "1").equals("1"))
                        Glide.with(StatsActivity.this).load(R.drawable.layout5x5_s).into(img);
                    else
                        Glide.with(StatsActivity.this).load(R.drawable.layout5x5_o).into(img);
                    break;
                case 2:
                    highestNumber = findViewById(R.id.highest_number3);
                    timePlayed = findViewById(R.id.time_played3);
                    undo = findViewById(R.id.undo_number3);
                    moves_D = findViewById(R.id.moves_D3);
                    moves_L = findViewById(R.id.moves_L3);
                    moves_R = findViewById(R.id.moves_R3);
                    moves_T = findViewById(R.id.moves_T3);
                    moves = findViewById(R.id.moves_All3);
                    tpm = findViewById(R.id.time_swipes3);
                    rekord = findViewById(R.id.highest_score3);
                    img = findViewById(R.id.stat_img3);
                    if (PreferenceManager.getDefaultSharedPreferences(StatsActivity.this).getString("pref_color", "1").equals("1"))
                        Glide.with(StatsActivity.this).load(R.drawable.layout6x6_s).into(img);
                    else
                        Glide.with(StatsActivity.this).load(R.drawable.layout6x6_o).into(img);
                    break;
                case 3:
                    highestNumber = findViewById(R.id.highest_number4);
                    timePlayed = findViewById(R.id.time_played4);
                    undo = findViewById(R.id.undo_number4);
                    moves_D = findViewById(R.id.moves_D4);
                    moves_L = findViewById(R.id.moves_L4);
                    moves_R = findViewById(R.id.moves_R4);
                    moves_T = findViewById(R.id.moves_T4);
                    moves = findViewById(R.id.moves_All4);
                    tpm = findViewById(R.id.time_swipes4);
                    rekord = findViewById(R.id.highest_score4);
                    img = findViewById(R.id.stat_img4);
                    if (PreferenceManager.getDefaultSharedPreferences(StatsActivity.this).getString("pref_color", "1").equals("1"))
                        Glide.with(StatsActivity.this).load(R.drawable.layout7x7_s).into(img);
                    else
                        Glide.with(StatsActivity.this).load(R.drawable.layout7x7_o).into(img);
                    break;
            }
            GameStatistics gameStatistics = readStatisticsFromFile(position + 4);
            highestNumber.setText("" + gameStatistics.getHighestNumber());
            timePlayed.setText(formatMillis(gameStatistics.getTimePlayed()));
            undo.setText("" + gameStatistics.getUndo());
            moves_D.setText("" + gameStatistics.getMoves_d());
            moves_R.setText("" + gameStatistics.getMoves_r());
            moves_T.setText("" + gameStatistics.getMoves_t());
            moves_L.setText("" + gameStatistics.getMoves_l());
            moves.setText("" + gameStatistics.getMoves());
            if (gameStatistics.getMoves() != 0)
                tpm.setText("" + formatSmallMillis(gameStatistics.getTimePlayed() / gameStatistics.getMoves()));
            else
                tpm.setText("0");
            rekord.setText("" + gameStatistics.getRecord());


            return view;
        }

        public String formatSmallMillis(long timeInMillis) {
            String sign = "";
            if (timeInMillis < 0) {
                sign = "-";
                timeInMillis = Math.abs(timeInMillis);
            }
            Double seconds = new Double(((double) timeInMillis) / (double) TimeUnit.SECONDS.toMillis(1));
            StringBuilder sb = new StringBuilder(",##0.00");
            DecimalFormat df = new DecimalFormat(sb.toString());
            df.setRoundingMode(RoundingMode.HALF_UP);
            final StringBuilder formatted = new StringBuilder(20);
            formatted.append(sign);
            formatted.append(df.format(seconds));
            formatted.append(" s");
            return formatted.toString();
        }

        public String formatMillis(long timeInMillis) {
            String sign = "";
            if (timeInMillis < 0) {
                sign = "-";
                timeInMillis = Math.abs(timeInMillis);
            }
            Double seconds = new Double(((double) timeInMillis) / (double) TimeUnit.HOURS.toMillis(1));
            StringBuilder sb = new StringBuilder(",##0.00");
            DecimalFormat df = new DecimalFormat(sb.toString());
            df.setRoundingMode(RoundingMode.HALF_UP);
            final StringBuilder formatted = new StringBuilder(20);
            formatted.append(sign);
            formatted.append(df.format(seconds));
            formatted.append(" h");
            return formatted.toString();
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

        public GameStatistics readStatisticsFromFile(int n) {
            GameStatistics gS = new GameStatistics(n);
            try {
                File file = new File(getFilesDir(), "statistics" + n + ".txt");
                FileInputStream fileIn = new FileInputStream(file);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                gS = (GameStatistics) in.readObject();
                in.close();
                fileIn.close();
            } catch (InvalidClassException ice) {
                File file = new File(getFilesDir(), "statistics" + n + ".txt");
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return gS;
        }
    }


}
