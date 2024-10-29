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
package org.secuso.privacyfriendly2048.activities

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import org.secuso.pfacore.model.DrawerElement
import org.secuso.privacyfriendly2048.PFApplicationData
import org.secuso.privacyfriendly2048.R
import org.secuso.privacyfriendly2048.activities.helper.BaseActivity
import org.secuso.privacyfriendly2048.activities.helper.GameStatistics
import java.io.File
import java.io.FileInputStream
import java.io.InvalidClassException
import java.io.ObjectInputStream
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * The game statistics of the four modes are loaded and shown with a ViePager in this activity.
 *
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180910
 */
class StatsActivity : BaseActivity() {

    override fun isActiveDrawerElement(element: DrawerElement) = element.name == ContextCompat.getString(this, R.string.action_stat)

    private val layouts = intArrayOf(
        R.layout.fragment_stats1,
        R.layout.fragment_stats2,
        R.layout.fragment_stats3,
        R.layout.fragment_stats4,
    )

    var TABNAMES: Array<String> = arrayOf("4x4", "5x5", "6x6", "7x7")

    /**
     * The [PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * [FragmentPagerAdapter] derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [FragmentStatePagerAdapter].
     */
    private val mSectionsPagerAdapter by lazy {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        MyViewPagerAdapter()
    }

    /**
     * The [ViewPager] that will host the section contents.
     */
    private val mViewPager: ViewPager by lazy {
        findViewById(R.id.main_content)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

//        val actionBar = supportActionBar
//        //actionBar.setTitle(R.string.menu_highscore);
//        actionBar!!.setDisplayHomeAsUpEnabled(true)
//        actionBar.setBackgroundDrawable(ColorDrawable(Color.parseColor("#024265")))


        // Set up the ViewPager with the sections adapter.
        mViewPager.adapter = mSectionsPagerAdapter

        findViewById<TabLayout>(R.id.tabs).setupWithViewPager(mViewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_stats, menu)
        //getMenuInflater().inflate(R.menu.menu_stats, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        when (item.itemId) {
            R.id.action_reset -> {
                //    SaveLoadStatistics.resetStats(this);
                //    mSectionsPagerAdapter.refresh(this);
                resetGameStatistics()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun resetGameStatistics() {
        for (n in 4..7) {
            try {
                val file = File(filesDir, "statistics$n.txt")
                file.delete()
            } catch (e: Exception) {
            }
        }
        finish()
        startActivity(intent)
    }


    inner class MyViewPagerAdapter : PagerAdapter() {
        private val layoutInflater by lazy {
            getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }

        override fun getPageTitle(position: Int): CharSequence {
            return TABNAMES[position]
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = layoutInflater.inflate(layouts[position], container, false)
            container.addView(view)
            var img: ImageView? = ImageView(this@StatsActivity)
            var highestNumber = TextView(this@StatsActivity)
            var timePlayed = TextView(this@StatsActivity)
            var undo = TextView(this@StatsActivity)
            var moves_L = TextView(this@StatsActivity)
            var moves_R = TextView(this@StatsActivity)
            var moves_T = TextView(this@StatsActivity)
            var moves_D = TextView(this@StatsActivity)
            var moves = TextView(this@StatsActivity)
            var tpm = TextView(this@StatsActivity)
            var rekord = TextView(this@StatsActivity)
            when (position) {
                0 -> {
                    highestNumber = findViewById(R.id.highest_number1)
                    timePlayed = findViewById(R.id.time_played1)
                    undo = findViewById(R.id.undo_number1)
                    moves_D = findViewById(R.id.moves_D1)
                    moves_L = findViewById(R.id.moves_L1)
                    moves_R = findViewById(R.id.moves_R1)
                    moves_T = findViewById(R.id.moves_T1)
                    moves = findViewById(R.id.moves_All1)
                    tpm = findViewById(R.id.time_swipes1)
                    rekord = findViewById(R.id.highest_score1)
                    img = findViewById(R.id.stat_img1)
                    Glide.with(this@StatsActivity).load(
                        if (PFApplicationData.instance(this@StatsActivity).prefColorScheme.value == "1") {
                            R.drawable.layout4x4_s
                        } else {
                            R.drawable.layout4x4_o
                        }
                    ).into(img)
                }

                1 -> {
                    highestNumber = findViewById(R.id.highest_number2)
                    timePlayed = findViewById(R.id.time_played2)
                    undo = findViewById(R.id.undo_number2)
                    moves_D = findViewById(R.id.moves_D2)
                    moves_L = findViewById(R.id.moves_L2)
                    moves_R = findViewById(R.id.moves_R2)
                    moves_T = findViewById(R.id.moves_T2)
                    moves = findViewById(R.id.moves_All2)
                    tpm = findViewById(R.id.time_swipes2)
                    rekord = findViewById(R.id.highest_score2)
                    img = findViewById(R.id.stat_img2)
                    Glide.with(this@StatsActivity).load(
                        if (PFApplicationData.instance(this@StatsActivity).prefColorScheme.value == "1") {
                            R.drawable.layout5x5_s
                        } else {
                            R.drawable.layout5x5_o
                        }
                    ).into(img)
                }

                2 -> {
                    highestNumber = findViewById(R.id.highest_number3)
                    timePlayed = findViewById(R.id.time_played3)
                    undo = findViewById(R.id.undo_number3)
                    moves_D = findViewById(R.id.moves_D3)
                    moves_L = findViewById(R.id.moves_L3)
                    moves_R = findViewById(R.id.moves_R3)
                    moves_T = findViewById(R.id.moves_T3)
                    moves = findViewById(R.id.moves_All3)
                    tpm = findViewById(R.id.time_swipes3)
                    rekord = findViewById(R.id.highest_score3)
                    img = findViewById(R.id.stat_img3)
                    Glide.with(this@StatsActivity).load(
                        if (PFApplicationData.instance(this@StatsActivity).prefColorScheme.value == "1") {
                            R.drawable.layout6x6_s
                        } else {
                            R.drawable.layout6x6_o
                        }
                    ).into(img)
                }

                3 -> {
                    highestNumber = findViewById(R.id.highest_number4)
                    timePlayed = findViewById(R.id.time_played4)
                    undo = findViewById(R.id.undo_number4)
                    moves_D = findViewById(R.id.moves_D4)
                    moves_L = findViewById(R.id.moves_L4)
                    moves_R = findViewById(R.id.moves_R4)
                    moves_T = findViewById(R.id.moves_T4)
                    moves = findViewById(R.id.moves_All4)
                    tpm = findViewById(R.id.time_swipes4)
                    rekord = findViewById(R.id.highest_score4)
                    img = findViewById(R.id.stat_img4)
                    Glide.with(this@StatsActivity).load(
                        if (PFApplicationData.instance(this@StatsActivity).prefColorScheme.value == "1") {
                            R.drawable.layout7x7_s
                        } else {
                            R.drawable.layout7x7_o
                        }
                    ).into(img)
                }
            }
            val gameStatistics = readStatisticsFromFile(position + 4)
            highestNumber.text = "${gameStatistics.highestNumber}"
            timePlayed.text = formatMillis(gameStatistics.timePlayed)
            undo.text = "${gameStatistics.undo}"
            moves_D.text = "${gameStatistics.moves_d}"
            moves_R.text = "${gameStatistics.moves_r}"
            moves_T.text = "${gameStatistics.moves_t}"
            moves_L.text = "${gameStatistics.moves_l}"
            moves.text = "${gameStatistics.moves}"
            if (gameStatistics.moves != 0L) {
                tpm.text = formatSmallMillis(gameStatistics.timePlayed / gameStatistics.moves)
            }
            else {
                tpm.text = "0"
            }
            rekord.text = "${gameStatistics.record}"

            return view
        }

        private fun formatSmallMillis(timeInMillis: Long): String {
            var timeInMillis = timeInMillis
            var sign = ""
            if (timeInMillis < 0) {
                sign = "-"
                timeInMillis = abs(timeInMillis.toDouble()).toLong()
            }
            val seconds = (timeInMillis.toDouble()) / TimeUnit.SECONDS.toMillis(1).toDouble()
            val sb = StringBuilder(",##0.00")
            val df = DecimalFormat(sb.toString())
            df.roundingMode = RoundingMode.HALF_UP
            val formatted = StringBuilder(20)
            formatted.append(sign)
            formatted.append(df.format(seconds))
            formatted.append(" s")
            return formatted.toString()
        }

        private fun formatMillis(timeInMillis: Long): String {
            var timeInMillis = timeInMillis
            var sign = ""
            if (timeInMillis < 0) {
                sign = "-"
                timeInMillis = abs(timeInMillis.toDouble()).toLong()
            }
            val seconds = (timeInMillis.toDouble()) / TimeUnit.HOURS.toMillis(1).toDouble()
            val sb = StringBuilder(",##0.00")
            val df = DecimalFormat(sb.toString())
            df.roundingMode = RoundingMode.HALF_UP
            val formatted = StringBuilder(20)
            formatted.append(sign)
            formatted.append(df.format(seconds))
            formatted.append(" h")
            return formatted.toString()
        }

        override fun getCount(): Int {
            return layouts.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }


        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }

        private fun readStatisticsFromFile(n: Int): GameStatistics {
            var gS = GameStatistics(n)
            try {
                val file = File(filesDir, "statistics$n.txt")
                val fileIn = FileInputStream(file)
                val `in` = ObjectInputStream(fileIn)
                gS = `in`.readObject() as GameStatistics
                `in`.close()
                fileIn.close()
            } catch (ice: InvalidClassException) {
                val file = File(filesDir, "statistics$n.txt")
                file.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return gS
        }
    }
}
