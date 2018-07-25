package org.secuso.privacyfriendlyexample.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import org.secuso.privacyfriendlyexample.activities.helper.BaseActivity;
import org.secuso.privacyfriendlyexample.R;
import org.secuso.privacyfriendlyexample.activities.helper.GameStatistics;

import java.io.File;
import java.io.FileInputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StatsActivity extends BaseActivity {

    private int[] layouts = new int[]{
            R.layout.fragment_stats1,
            R.layout.fragment_stats2,
            R.layout.fragment_stats3,
            R.layout.fragment_stats4,
    };

    String [] TABNAMES = {"4x4","5x5","6x6","7x7"};

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
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

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
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


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stats, menu);
        //getMenuInflater().inflate(R.menu.menu_stats, menu);
        return true;
        //return false;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch(item.getItemId()) {
            //case R.id.action_reset:
            //    SaveLoadStatistics.resetStats(this);
            //    mSectionsPagerAdapter.refresh(this);
            //    return true;
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
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
            Log.i("position", ""+position);
            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);
            TextView highestNumber = new TextView(StatsActivity.this);
            TextView timePlayed = new TextView(StatsActivity.this);
            TextView undo = new TextView(StatsActivity.this);
            TextView moves_L = new TextView(StatsActivity.this);
            TextView moves_R = new TextView(StatsActivity.this);
            TextView moves_T = new TextView(StatsActivity.this);
            TextView moves_D = new TextView(StatsActivity.this);
            switch(position)
            {
                case 0:
                    highestNumber = findViewById(R.id.highest_number1);
                    timePlayed = findViewById(R.id.time_played1);
                    undo = findViewById(R.id.undo_number1);
                    moves_D = findViewById(R.id.moves_D1);
                    moves_L = findViewById(R.id.moves_L1);
                    moves_R = findViewById(R.id.moves_R1);
                    moves_T = findViewById(R.id.moves_T1);
                    break;
                case 1:
                    highestNumber = findViewById(R.id.highest_number2);
                    timePlayed = findViewById(R.id.time_played2);
                    undo = findViewById(R.id.undo_number2);
                    break;
                case 2:
                    highestNumber = findViewById(R.id.highest_number3);
                    timePlayed = findViewById(R.id.time_played3);
                    undo = findViewById(R.id.undo_number3);
                    break;
                case 3:
                    highestNumber = findViewById(R.id.highest_number4);
                    timePlayed = findViewById(R.id.time_played4);
                    undo = findViewById(R.id.undo_number4);
                    break;
            }
            GameStatistics gameStatistics = readStatisticsFromFile(position+4);
            Date d = new Date(gameStatistics.getTimePlayed());

            highestNumber.setText(""+gameStatistics.getHighestNumber());
            timePlayed.setText(formatMillis(gameStatistics.getTimePlayed()));
            undo.setText("" + gameStatistics.getUndo());
            moves_D.setText("" + gameStatistics.getMoves_d());
            moves_R.setText("" + gameStatistics.getMoves_r());
            moves_T.setText("" + gameStatistics.getMoves_t());
            moves_L.setText("" + gameStatistics.getMoves_l());



            return view;
        }
        public String formatMillis(long timeInMillis) {
            String sign = "";
            if (timeInMillis < 0) {
                sign = "-";
                timeInMillis = Math.abs(timeInMillis);
            }
            long hours = timeInMillis / TimeUnit.HOURS.toMillis(1);
            long minutes = timeInMillis % TimeUnit.HOURS.toMillis(1)/ TimeUnit.MINUTES.toMillis(1);
            long seconds = timeInMillis % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);
            long millis = timeInMillis % TimeUnit.SECONDS.toMillis(1);

            final StringBuilder formatted = new StringBuilder(20);
            formatted.append(sign);
            formatted.append(String.format("%03d",hours));
            formatted.append(String.format(":%02d", minutes));
            formatted.append(String.format(":%02d", seconds));
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
        public GameStatistics readStatisticsFromFile(int n)
        {
            GameStatistics gS = new GameStatistics(n);
            try{
                File file = new File(getFilesDir(), "statistics" + n + ".txt");
                FileInputStream fileIn = new FileInputStream(file);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                gS = (GameStatistics)in.readObject();
                in.close();
                fileIn.close();
            }
            catch(InvalidClassException ice)
            {
                File file = new File(getFilesDir(), "statistics" + n + ".txt");
                file.delete();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            Log.i("statistics", gS.toString());
            return gS;
        }
    }



}
