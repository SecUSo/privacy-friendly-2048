package org.secuso.privacyfriendly2048.activities.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import org.secuso.privacyfriendly2048.R
import org.secuso.privacyfriendly2048.activities.helper.GridRecyclerView
import org.secuso.privacyfriendly2048.helpers.GetColorRes
import org.secuso.privacyfriendly2048.model.GameBoard
import kotlin.time.Duration.Companion.milliseconds

class Grid2048Adapter(
    val context: Context,
    val layoutInflater: LayoutInflater,
    val recyclerView: GridRecyclerView,
    private var grid: Array<Array<Int>>,
): RecyclerView.Adapter<Grid2048Adapter.GridCellViewHolder>() {
    private val size = grid.size

    private fun Pair<Int, Int>.linear() = first * size + second
    private fun Int.gridIndex() = this / size to this % size

    private fun notifyItemSwapped(source: Pair<Int, Int>, target: Pair<Int, Int>) {
        notifyItemMoved(source.linear(), target.linear())
        if (source.first < target.first) {
            notifyItemMoved(target.linear() - 1, source.linear())
        } else if (target.first < source.first) {
            notifyItemMoved(target.linear() + 1, source.linear())
        }
    }

    suspend fun updateGrid(grid: Array<Array<Int>>, events: List<GameBoard.BoardChangeEvent>) {

        // End all animations which are currently running.
        // This only happens it the user swipes faster than the animation time,
        // but we do not want to block the user from further interactions.
        // We rather stop all animations, ensure that the state is correct again and then proceed normally.
        if (recyclerView.itemAnimator != null) {
            recyclerView.itemAnimator?.endAnimations()
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                child.animate().cancel()
                child.alpha = 1f
                val index = i.gridIndex()
            }
        }

        this.grid = grid
        for (event in events) {
            when (event) {
                is GameBoard.BoardChangeEvent.MoveEvent -> {
                    notifyItemSwapped(event.source, event.target)
                }
                is GameBoard.BoardChangeEvent.MergeEvent -> {
                    if (recyclerView.itemAnimator != null ){
                        val target = recyclerView.findViewHolderForAdapterPosition(event.target.linear())!!
                        val source = recyclerView.findViewHolderForAdapterPosition(event.source.linear())!!

                        source.itemView.animate()
                            .alpha(0f)
                            .x(target.itemView.x)
                            .y(target.itemView.y)
                            .setDuration(GridRecyclerView.MERGE_DURATION)
                            .withEndAction {
                                notifyItemChanged(event.source.linear())

                            }
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationCancel(animation: Animator) {
                                    val source = recyclerView.findViewHolderForAdapterPosition(event.source.linear())!!
                                    source.itemView.alpha = 1f // Reset alpha for future use
                                }
                            })
                            .start()
                    }

                    notifyItemChanged(event.target.linear())
                    notifyItemChanged(event.source.linear())
                }
                is GameBoard.BoardChangeEvent.SpawnEvent -> {
                    // we do not handle the spawn events now, but rather wait until the moves/merges are finished.
                    // This ensures that there are no weird visual glitches due to overlapping animations.
                }
            }
        }
        delay(GridRecyclerView.MAX_DELAY.milliseconds)
        for (event in events) {
            if (event is GameBoard.BoardChangeEvent.SpawnEvent) {
                notifyItemChanged(event.source.linear())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridCellViewHolder {
        return GridCellViewHolder(context, layoutInflater.inflate(R.layout.game_cell, parent, false))
    }

    override fun getItemCount() = size * size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: GridCellViewHolder, position: Int) {
        val cell = grid[position / size][position % size]
        if (cell == 0) {
            holder.root.visibility = View.INVISIBLE
            return
        }
        holder.btn.text = cell.toString()
        holder.root.visibility = View.VISIBLE
        holder.adaptColor(cell)
    }

    class GridCellViewHolder(val context: Context, val root: View): RecyclerView.ViewHolder(root) {
        val btn by lazy { itemView.findViewById<AppCompatButton>(R.id.cell) }

        fun adaptColor(value: Int) {
            when (value) {
                2 -> {
                    btn.background.setTint(GetColorRes(context, R.attr.button2))
                    btn.setTextColor(GetColorRes(context, R.attr.button2Text))
                }
                4 -> {
                    btn.background.setTint(GetColorRes(context, R.attr.button4))
                    btn.setTextColor(GetColorRes(context, R.attr.button4Text))
                }
                8 -> {
                    btn.background.setTint(GetColorRes(context, R.attr.button8))
                    btn.setTextColor(GetColorRes(context, R.attr.button8Text))
                }
                16 -> {
                    btn.background.setTint(GetColorRes(context, R.attr.button16))
                    btn.setTextColor(GetColorRes(context, R.attr.button16Text))
                }
                32 -> {
                    btn.background.setTint(GetColorRes(context, R.attr.button32))
                    btn.setTextColor(GetColorRes(context, R.attr.button32Text))
                }
                64 -> {
                    btn.background.setTint(GetColorRes(context, R.attr.button64))
                    btn.setTextColor(GetColorRes(context, R.attr.button64Text))
                }
                128 -> {
                    btn.background.setTint(GetColorRes(context, R.attr.button128))
                    btn.setTextColor(GetColorRes(context, R.attr.button128Text))
                }
                256 -> {
                    btn.background.setTint(GetColorRes(context, R.attr.button256))
                    btn.setTextColor(GetColorRes(context, R.attr.button256Text))
                }
                512 -> {
                    btn.background.setTint(GetColorRes(context, R.attr.button512))
                    btn.setTextColor(GetColorRes(context, R.attr.button512Text))
                }
                1024 -> {
                    btn.background.setTint(GetColorRes(context, R.attr.button1024))
                    btn.setTextColor(GetColorRes(context, R.attr.button1024Text))
                }
                2048 -> {
                    btn.background.setTint(GetColorRes(context, R.attr.button2048))
                    btn.setTextColor(GetColorRes(context, R.attr.button2048Text))
                }
                4096 -> {
                    btn.background.setTint(GetColorRes(context, R.attr.button4096))
                    btn.setTextColor(GetColorRes(context, R.attr.button4096Text))
                }
                8192 -> {
                    btn.background.setTint(GetColorRes(context, R.attr.button8192))
                    btn.setTextColor(GetColorRes(context, R.attr.button8192Text))
                }
                16384 -> {
                    btn.background.setTint(GetColorRes(context, R.attr.button16384))
                    btn.setTextColor(GetColorRes(context, R.attr.button16384Text))
                }
                32768 -> {
                    btn.background.setTint(GetColorRes(context, R.attr.button32768))
                    btn.setTextColor(GetColorRes(context, R.attr.button32768Text))
                }
            }
        }
    }
}