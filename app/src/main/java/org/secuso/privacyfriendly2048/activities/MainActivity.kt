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
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.bumptech.glide.Glide
import org.secuso.pfacore.model.DrawerElement
import org.secuso.privacyfriendly2048.PFApplicationData
import org.secuso.privacyfriendly2048.R
import org.secuso.privacyfriendly2048.activities.helper.BaseActivity

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
    private val viewPager: ViewPager by lazy { findViewById(R.id.view_pager) }
    private val dotsLayout: LinearLayout by lazy { findViewById(R.id.layoutDots) }
    private lateinit var dots: Array<TextView?>
    private val btnPrev: ImageButton by lazy { findViewById(R.id.btn_prev) }
    private val btnNext: ImageButton by lazy { findViewById(R.id.btn_next) }
    private val currentPage by lazy { PFApplicationData.instance(this).currentPage }
    private val myViewPagerAdapter: MyViewPagerAdapter by lazy { MyViewPagerAdapter() }

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

    override fun isActiveDrawerElement(element: DrawerElement) = element.name == ContextCompat.getString(this, R.string.action_main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // adding bottom dots
        addBottomDots(currentPage.value)
        currentPage.state.observe(this) {
            addBottomDots(it)
            updateButtons(it)
            updateMovingButtons(it)
        }

        //checking resumable
        val directory = filesDir
        val files = directory.listFiles()

        for (i in files!!.indices) {
            Log.i("files", files[i].name)
            for (j in gameResumeable.indices) {
                if (files[i].name == "state" + (j + 4) + ".txt") gameResumeable[j] = true
            }
        }

        viewPager.adapter = myViewPagerAdapter
        viewPager.currentItem = currentPage.value
        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageSelected(position: Int) {
                    currentPage.value = position
                }

                override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {
                }

                override fun onPageScrollStateChanged(arg0: Int) {
                }
            }
        )

        btnPrev.setOnClickListener {
            getItem(-1).let {
                if (it >= 0) {
                    viewPager.currentItem = it
                }
            }
        }

        btnNext.setOnClickListener {
            // checking for last page
            // if last page home screen will be launched
            getItem(+1).let {
                if (it < layouts.size) {
                    viewPager.currentItem = it
                }
            }
        }
    }

    private fun addListener(b1: Button, b2: Button, n: Int) {
        b1.setOnClickListener {
            val intent = Intent(this@MainActivity, GameActivity::class.java)
            intent.putExtra("n", n)
            intent.putExtra("points", 0)
            intent.putExtra("new", true)
            intent.putExtra("filename", "state$n.txt")
            intent.putExtra("undo", false)
            startActivity(intent)
        }
        b2.setOnClickListener {
            val intent = Intent(this@MainActivity, GameActivity::class.java)
            intent.putExtra("n", n)
            intent.putExtra("new", false)
            intent.putExtra("filename", "state$n.txt")
            intent.putExtra("undo", false)
            startActivity(intent)
        }
    }

    private fun addBottomDots(currentPage: Int) {
        dots = arrayOfNulls(layouts.size)

        val activeColor = ContextCompat.getColor(this, R.color.dot_light_screen)
        val inactiveColor = ContextCompat.getColor(this, R.color.dot_dark_screen)

        dotsLayout.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]!!.text = HtmlCompat.fromHtml("&#8226;", HtmlCompat.FROM_HTML_MODE_LEGACY)
            dots[i]!!.textSize = 35f
            dots[i]!!.setTextColor(inactiveColor)
            dotsLayout.addView(dots[i])
        }

        if (dots.isNotEmpty()) dots[currentPage]!!.setTextColor(activeColor)
    }

    private fun getItem(i: Int): Int {
        return viewPager.currentItem + i
    }



    fun updateMovingButtons(position: Int) {
        if (position == layouts.size - 1) {
            btnNext.visibility = View.INVISIBLE
        } else {
            btnNext.visibility = View.VISIBLE
        }
        if (position == 0) {
            btnPrev.visibility = View.INVISIBLE
        } else {
            btnPrev.visibility = View.VISIBLE
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
     * View pager adapter
     */
    inner class MyViewPagerAdapter : PagerAdapter() {
        private var layoutInflater: LayoutInflater? = null

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater!!.inflate(layouts[position], container, false)
            container.addView(view)
            val imageView: ImageView
            val prefColorScheme = PFApplicationData.instance(this@MainActivity).prefColorScheme
            when (position) {
                0 -> {
                    imageView = findViewById<ImageView>(R.id.main_menu_img1)
                    if (prefColorScheme.value == "1") Glide.with(this@MainActivity)
                        .load(R.drawable.layout4x4_s).into(imageView)
                    else Glide.with(this@MainActivity).load(R.drawable.layout4x4_o).into(imageView)
                }

                1 -> {
                    imageView = findViewById<ImageView>(R.id.main_menu_img2)
                    if (prefColorScheme.value == "1") Glide.with(this@MainActivity)
                        .load(R.drawable.layout5x5_s).into(imageView)
                    else Glide.with(this@MainActivity).load(R.drawable.layout5x5_o).into(imageView)
                }

                2 -> {
                    imageView = findViewById<ImageView>(R.id.main_menu_img3)
                    if (prefColorScheme.value == "1") Glide.with(this@MainActivity)
                        .load(R.drawable.layout6x6_s).into(imageView)
                    else Glide.with(this@MainActivity).load(R.drawable.layout6x6_o).into(imageView)
                }

                3 -> {
                    imageView = findViewById<ImageView>(R.id.main_menu_img4)
                    if (prefColorScheme.value == "1") Glide.with(this@MainActivity)
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
}
