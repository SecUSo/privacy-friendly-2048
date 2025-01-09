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

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.secuso.privacyfriendly2048.PFApplicationData
import org.secuso.privacyfriendly2048.R
import org.secuso.privacyfriendly2048.activities.adapter.Grid2048Adapter
import org.secuso.privacyfriendly2048.activities.adapter.Grid2048BackgroundAdapter
import org.secuso.privacyfriendly2048.activities.helper.Gestures
import org.secuso.privacyfriendly2048.activities.helper.GridRecyclerView
import org.secuso.privacyfriendly2048.activities.viewmodel.GameViewModel
import org.secuso.privacyfriendly2048.model.Direction
import org.secuso.privacyfriendly2048.model.Game2048
import org.secuso.privacyfriendly2048.model.GameBoard
import org.secuso.privacyfriendly2048.model.GameState

/**
 * This activity contains the entire game and draws the game field depending on the selected mode and the screen size.
 * Also the Intent influences, when a new game is generated or a previous game status is loaded.
 * Therefore a two dimensional field named elements is filled with instances of the class Element depending on the selected mode (4x4, 5x5, 6x6, 7x7).
 * The function of the two buttons restart and undo are also implemented.
 * With the help of the class Gesture the user inputs are recognized and the corresponding method is retrieved.
 * This method contains the important game logic for 2048.
 * With loops the values of the elements will be added and the whole elements will be moved in the selected direction.
 * After each move (only if something has been moved) a new element appears on the game field with the help of the method addNumber().
 * This new element is placed randomly on a free spot of the game field.
 * If the activity is paused or stopped, the game status is saved in the GameState and the GameStatistics will be updated.
 *
 * @author Julian Wadephul and Saskia Jacob
 * @author Patrick Schneider
 * @version 20241107
 */
class GameActivity: org.secuso.pfacore.ui.activities.BaseActivity() {
    val viewModel: GameViewModel by viewModels { GameViewModel.GameViewModelFactory(
        filesDir,
        Game2048.GameConfig(
            boardSize = intent.getIntExtra("n", 4),
            wonOnValue = 2048,
            cellsToFill = 1,
            distribution = GameBoard.SpawnProbabilityDistribution(listOf(GameBoard.SpawnProbability(2, 0.9), GameBoard.SpawnProbability(4, 0.1)))
        ),
        intent?.getBooleanExtra("new", true) == false
    ) }

    val textPoints by lazy { findViewById<TextView>(R.id.points) }
    val textRecord by lazy { findViewById<TextView>(R.id.record) }
    val undoButton by lazy { findViewById<ImageButton>(R.id.undoButton) }
    val grid by lazy { findViewById<GridRecyclerView>(R.id.grid) }
    val gridBackground by lazy { findViewById<GridRecyclerView>(R.id.grid_background) }
    val adapter by lazy { Grid2048Adapter(this, layoutInflater, grid, viewModel.board()) { animation_running = false } }
    val adapterBackground by lazy { Grid2048BackgroundAdapter(viewModel.boardSize, layoutInflater) }

    var gameWon = false
    var already_undone = false
    var animation_running = false

    val gestureListener by lazy {
        object : Gestures(this@GameActivity) {
            override fun onSwipeTop() = move(Direction.UP)
            override fun onSwipeBottom() = move(Direction.DOWN)
            override fun onSwipeLeft() = move(Direction.LEFT)
            override fun onSwipeRight() = move(Direction.RIGHT)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            gestureListener.onTouch(null, event)
        }
        return super.onTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_game);
        super.onCreate(savedInstanceState);

        if (PFApplicationData.instance(this).prefDisplayLock.value) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        textPoints.text = viewModel.points.toString()
        textRecord.text = viewModel.stats.record.toString()
        findViewById<ImageButton>(R.id.restartButton).setOnClickListener {
            viewModel.stop()
            viewModel.reset()
            viewModel.start()
            lifecycleScope.launch {
                adapter.updateGrid(viewModel.board(), listOf())
                @SuppressLint("NotifyDataSetChanged")
                adapter.notifyDataSetChanged()
            }
        }
        undoButton.setOnClickListener { undo() }
        undoButton.visibility = if (viewModel.canUndo()) { View.VISIBLE } else { View.INVISIBLE }
        viewModel.start()

        grid.layoutManager = object : GridLayoutManager(this, viewModel.boardSize, VERTICAL, false) {
            override fun canScrollHorizontally() = false
            override fun canScrollVertically() = false
        }
        grid.gestures = gestureListener
        grid.adapter = adapter
        grid.setHasFixedSize(true)

        if (!PFApplicationData.instance(this).animationActivated.value) {
            grid.itemAnimator = null
        }

        gridBackground.layoutManager = object : GridLayoutManager(this, viewModel.boardSize, VERTICAL, false) {
            override fun canScrollHorizontally() = false
            override fun canScrollVertically() = false
        }
        gridBackground.adapter = adapterBackground
        gridBackground.setHasFixedSize(true)
    }

    private fun undo() {
        Log.d("GameActivity", "undo pressed")
        viewModel.undo()
        already_undone = true
        lifecycleScope.launch {
            adapter.updateGrid(viewModel.board(), listOf())
        }
        @SuppressLint("NotifyDataSetChanged")
        adapter.notifyDataSetChanged()
        undoButton.visibility = if (viewModel.canUndo() && (PFApplicationData.instance(this).multipleUndo.value || !already_undone)) { View.VISIBLE } else { View.INVISIBLE }
    }

    private fun move(direction: Direction): Boolean {
        if (animation_running) {
            return false
        }
        lifecycleScope.launch {
            viewModel.move(direction).let { events ->
                if (events.isNotEmpty()) {
                    animation_running = true
                    Log.d("animation", "started")
                    adapter.updateGrid(viewModel.board(), events)
                    already_undone = false
                }
            }
        }
        textPoints.text = viewModel.points.toString()
        textRecord.text = viewModel.stats.record.toString()
        undoButton.visibility = if (viewModel.canUndo()) { View.VISIBLE } else { View.INVISIBLE }

        if (!gameWon && viewModel.state == GameState.WON) {
            gameWon = true
            viewModel.stop()
            AlertDialog.Builder(this)
                .setTitle((this.resources.getString(R.string.Titel_V_Message)))
                .setMessage((this.resources.getString(R.string.Winning_Message)))
                .setNegativeButton((this.resources.getString(R.string.No_Message))) { dialog, which -> onBackPressed() }
                .setPositiveButton((this.resources.getString(R.string.Yes_Message))) { dialog, which -> }
                .setCancelable(false)
                .create().show()
        } else if (viewModel.state == GameState.LOST) {
            viewModel.stop()
            AlertDialog.Builder(this)
                .setTitle((this.resources.getString(R.string.Titel_L_Message, viewModel.points)))
                .setMessage(this.resources.getString(R.string.Lost_Message, viewModel.points))
                .setNegativeButton((this.resources.getString(R.string.No_Message))) { dialog, which ->
                    viewModel.reset()
                    intent.putExtra("new", true)
                    finish()
                    this@GameActivity.onBackPressed()
                }
                .setPositiveButton((this.resources.getString(R.string.Yes_Message))) { _, _ ->
                    startActivity(Intent(this, GameActivity::class.java))
                    finish()
                }
                .setCancelable(false)
                .create().show()
        }
        return false
    }

    // This handles actions via a S-Pen
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                move(Direction.UP)
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                move(Direction.DOWN)
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                move(Direction.LEFT)
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                move(Direction.RIGHT)
                return true
            }
            KeyEvent.KEYCODE_Z -> if (event?.isCtrlPressed == true) {
                undo()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        viewModel.save()
        super.onPause()
    }

}