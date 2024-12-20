package org.secuso.privacyfriendly2048.activities.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.secuso.privacyfriendly2048.R

class Grid2048BackgroundAdapter(val size: Int, val layoutInflater: LayoutInflater): RecyclerView.Adapter<Grid2048BackgroundAdapter.GridCellViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridCellViewHolder {
        return GridCellViewHolder(layoutInflater.inflate(R.layout.game_cell_background, parent, false))
    }

    override fun getItemCount() = size * size

    override fun onBindViewHolder(holder: GridCellViewHolder, position: Int) {}

    class GridCellViewHolder(root: View): RecyclerView.ViewHolder(root)
}