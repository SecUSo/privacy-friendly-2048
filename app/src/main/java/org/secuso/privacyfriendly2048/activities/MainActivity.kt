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

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.bumptech.glide.Glide
import org.secuso.privacyfriendly2048.R
import org.secuso.privacyfriendly2048.activities.helper.BaseActivity
import org.secuso.privacyfriendly2048.helpers.FirstLaunchManager

/**
 * The MainActivity is the activity from which the game in each mode is started.
 * Therefore a ViewPager is used to depict pictures of the four mode.
 * At the bottom of the activity there are two buttons, which start a new game (START NEW GAME) and continue the previous game (CONTINUE GAME).
 * The CONTINUE GAME button is greyed out and is not selectable, if there is no previous played game available in this mode.
 *
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180910
 */
class MainActivity : BaseActivity() {
    private var viewPager: ViewPager? = null
    private var myViewPagerAdapter: MyViewPagerAdapter? = null
    private var dotsLayout: LinearLayout? = null
    private lateinit var dots: Array<TextView?>
    private var btnPrev: ImageButton? = null
    private var btnNext: ImageButton? = null
    private var firstLaunchManager: FirstLaunchManager? = null
    private var currentPage = 0
    private var editor: SharedPreferences.Editor? = null
    private val preferences: SharedPreferences by lazy {
        getSharedPreferences(mypref, MODE_PRIVATE)
    }
    private val mypref = "myPref"

    private val layouts = intArrayOf(
        R.layout.choose_slide1,
        R.layout.choose_slide2,
        R.layout.choose_slide3,
        R.layout.choose_slide4,
    )
    private val gameResumeable = booleanArrayOf(
        false,
        false,
        false,
        false
    )

    override fun onStart() {
        super.onStart()
        editor = preferences.edit()
        currentPage = preferences.getInt("currentPage", 0)
        viewPager!!.currentItem = currentPage
        updateButtons(currentPage)
        updateMovingButtons(currentPage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        overridePendingTransition(0, 0)

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        firstLaunchManager = FirstLaunchManager(this)

        viewPager = findViewById<View>(R.id.view_pager) as ViewPager
        dotsLayout = findViewById<View>(R.id.layoutDots) as LinearLayout
        btnPrev = findViewById<View>(R.id.btn_prev) as ImageButton
        btnNext = findViewById<View>(R.id.btn_next) as ImageButton


        //checking resumable
        val directory = filesDir
        val files = directory.listFiles()

        for (i in files!!.indices) {
            Log.i("files", files[i].name)
            for (j in gameResumeable.indices) {
                if (files[i].name == "state" + (j + 4) + ".txt") gameResumeable[j] = true
            }
        }


        // adding bottom dots
        addBottomDots(0)

        // making notification bar transparent
        changeStatusBarColor()

        myViewPagerAdapter = MyViewPagerAdapter()

        viewPager!!.adapter = myViewPagerAdapter
        viewPager!!.addOnPageChangeListener(viewPagerPageChangeListener)

        btnPrev!!.setOnClickListener {
            val current = getItem(-1)
            if (current >= 0) {
                // move to next screen
                viewPager!!.currentItem = current
            } else {
            }
        }

        btnNext!!.setOnClickListener {
            // checking for last page
            // if last page home screen will be launched
            val current = getItem(+1)
            if (current < layouts.size) {
                // move to next screen
                viewPager!!.currentItem = current
            } else {
            }
        }
    }

    private fun addListener(b1: Button, b2: Button, n: Int) {
        val temp = n
        b1.setOnClickListener {
            val intent = Intent(this@MainActivity, GameActivity::class.java)
            intent.putExtra("n", temp)
            intent.putExtra("points", 0)
            intent.putExtra("new", true)
            intent.putExtra("filename", "state$temp.txt")
            intent.putExtra("undo", false)
            createBackStack(intent)
        }
        b2.setOnClickListener {
            val intent = Intent(this@MainActivity, GameActivity::class.java)
            intent.putExtra("n", temp)
            intent.putExtra("new", false)
            intent.putExtra("filename", "state$temp.txt")
            intent.putExtra("undo", false)
            createBackStack(intent)
        }
    }

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

    private fun addBottomDots(currentPage: Int) {
        dots = arrayOfNulls(layouts.size)

        val activeColor = ContextCompat.getColor(this, R.color.dot_light_screen)
        val inactiveColor = ContextCompat.getColor(this, R.color.dot_dark_screen)

        dotsLayout!!.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]!!.text = Html.fromHtml("&#8226;")
            dots[i]!!.textSize = 35f
            dots[i]!!.setTextColor(inactiveColor)
            dotsLayout!!.addView(dots[i])
        }

        if (dots.size > 0) dots[currentPage]!!.setTextColor(activeColor)
    }

    private fun getItem(i: Int): Int {
        return viewPager!!.currentItem + i
    }

    //  viewpager change listener
    var viewPagerPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            addBottomDots(position)
            currentPage = position
            editor!!.putInt("currentPage", currentPage)
            editor!!.commit()
            updateButtons(position)

            updateMovingButtons(position)
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {
        }

        override fun onPageScrollStateChanged(arg0: Int) {
        }
    }

    fun updateMovingButtons(position: Int) {
        if (position == layouts.size - 1) {
            // last page. make button text to GOT IT
            btnNext!!.visibility = View.INVISIBLE
        } else {
            // still pages are left
            btnNext!!.visibility = View.VISIBLE
        }
        if (position == 0) {
            // last page. make button text to GOT IT
            btnPrev!!.visibility = View.INVISIBLE
        } else {
            // still pages are left
            btnPrev!!.visibility = View.VISIBLE
        }
    }

    fun updateButtons(position: Int) {
        val newGameButton = this@MainActivity.findViewById<Button>(R.id.button_newGame)
        val continueButton = this@MainActivity.findViewById<Button>(R.id.button_continueGame)
        try {
            if (gameResumeable[position]) continueButton.setBackgroundResource(R.drawable.standalone_button)
            else continueButton.setBackgroundResource(R.drawable.inactive_button)

            continueButton.isEnabled = gameResumeable[position]
        } catch (aie: Exception) {
            aie.printStackTrace()
        }
        addListener(newGameButton, continueButton, position + 4)
    }

    /**
     * Making notification bar transparent
     */
    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    /**
     * View pager adapter
     */
    inner class MyViewPagerAdapter : PagerAdapter() {
        private var layoutInflater: LayoutInflater? = null

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater!!.inflate(layouts[position], container, false)
            container.addView(view)
            val imageView: ImageView
            when (position) {
                0 -> {
                    imageView = findViewById<View>(R.id.main_menu_img1) as ImageView
                    if (PreferenceManager.getDefaultSharedPreferences(this@MainActivity).getString("pref_color", "1") == "1") Glide.with(this@MainActivity)
                        .load(R.drawable.layout4x4_s).into(imageView)
                    else Glide.with(this@MainActivity).load(R.drawable.layout4x4_o).into(imageView)
                }

                1 -> {
                    imageView = findViewById<View>(R.id.main_menu_img2) as ImageView
                    if (PreferenceManager.getDefaultSharedPreferences(this@MainActivity).getString("pref_color", "1") == "1") Glide.with(this@MainActivity)
                        .load(R.drawable.layout5x5_s).into(imageView)
                    else Glide.with(this@MainActivity).load(R.drawable.layout5x5_o).into(imageView)
                }

                2 -> {
                    imageView = findViewById<View>(R.id.main_menu_img3) as ImageView
                    if (PreferenceManager.getDefaultSharedPreferences(this@MainActivity).getString("pref_color", "1") == "1") Glide.with(this@MainActivity)
                        .load(R.drawable.layout6x6_s).into(imageView)
                    else Glide.with(this@MainActivity).load(R.drawable.layout6x6_o).into(imageView)
                }

                3 -> {
                    imageView = findViewById<View>(R.id.main_menu_img4) as ImageView
                    if (PreferenceManager.getDefaultSharedPreferences(this@MainActivity).getString("pref_color", "1") == "1") Glide.with(this@MainActivity)
                        .load(R.drawable.layout7x7_s).into(imageView)
                    else Glide.with(this@MainActivity).load(R.drawable.layout7x7_o).into(imageView)
                }
            }
            return view
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
    }

    protected val navigationDrawerID: Int
        /**
         * This method connects the Activity to the menu item
         *
         * @return ID of the menu item it belongs to
         */
        get() = R.id.nav_example
}
