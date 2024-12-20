package org.secuso.privacyfriendly2048.activities.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.secuso.privacyfriendly2048.model.Board
import org.secuso.privacyfriendly2048.model.Direction
import org.secuso.privacyfriendly2048.model.Game2048
import org.secuso.privacyfriendly2048.model.GameBoard
import org.secuso.privacyfriendly2048.model.GameState
import org.secuso.privacyfriendly2048.model.GameStatistics
import org.secuso.privacyfriendly2048.model.IGame2048
import org.secuso.privacyfriendly2048.model.IGameConfig
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class GameViewModel(
    val filesDir: File,
    val config: Game2048.GameConfig,
    continuePrevious: Boolean,
): ViewModel(), IGame2048, IGameConfig by config {
    private var game: Game2048 = if (!continuePrevious) {
            Game2048(config).apply {
                try {
                    readStatsFromFile(FileInputStream(File(filesDir, "statistics${config.boardSize}.txt")))
                } catch (error: IOException) {
                    error.printStackTrace()
                }
            }
        } else {
            readStateFromFile()
        }

    override fun start() {
        game.start()
    }

    override fun stop() {
        game.stop()
        saveStatisticsToFile()
        if (game.state != GameState.RUNNING) {
            deleteStateFile()
        }
    }

    override fun move(direction: Direction): List<GameBoard.BoardChangeEvent> {
        Log.d("GameViewModel", "moving: ${direction}")
        return game.move(direction)
    }

    override fun undo() {
        game.undo()
    }

    override fun canUndo() = game.canUndo()

    override fun board(): Board {
        return game.board()
    }

    override val points: Long
        get() = game.points

    override val state: GameState
        get() = game.state

    override val stats: GameStatistics
        get() = game.stats

    fun reset() {
        deleteStateFile()
        game = Game2048(config)
    }

    fun save() {
        if (state != GameState.RUNNING) {
            return
        }
        try {
            FileOutputStream(File(filesDir, "state${game.config.boardSize}.txt")).apply {
                game.saveGameToFile(this)
                close()
            }
        } catch (e: IOException) {
            e.printStackTrace();
        }
        game.stop();
        saveStatisticsToFile();
    }

    private fun deleteStateFile() {
        try {
            File(filesDir, "state${game.config.boardSize}.txt").delete()
        } catch (e: IOException) {
            e.printStackTrace();
        }
    }

    private fun saveStatisticsToFile() {
        try {
            FileOutputStream(File(filesDir, "statistics${game.config.boardSize}.txt")).apply {
                game.saveStatsToFile(this)
                close()
            }
        } catch (e: IOException) {
            e.printStackTrace();
        }
    }

    private fun readStateFromFile(): Game2048 {
        return Game2048.readGameFromFile(FileInputStream(File(filesDir, "state${config.boardSize}.txt")))
    }

    class GameViewModelFactory(val filesDir: File, val config: Game2048.GameConfig, val continuePrevious: Boolean): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass == GameViewModel::class.java) {
                return GameViewModel(filesDir, config, continuePrevious) as T
            } else {
                return super.create(modelClass)
            }
        }
    }
}