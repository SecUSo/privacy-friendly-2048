package org.secuso.privacyfriendly2048.model

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class Game2048(val config: GameConfig, boardState: Board? = null): IGame2048 {
    data class SingleGameState(val data: Board, val points: Long): Serializable {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SingleGameState

            if (points != other.points) return false
            if (!data.contentDeepEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = points
            result = 31 * result + data.contentDeepHashCode()
            return result.toInt()
        }
    }

    data class GameConfig(
        override val boardSize: Int,
        override val cellsToFill: Int,
        override val distribution: GameBoard.SpawnProbabilityDistribution,
        override val wonOnValue: Int
    ): IGameConfig, Serializable

    override var stats = GameStatistics(config.boardSize)
        private set
    private var board = GameBoard(config.boardSize, boardState)
    override var points = 0L
        private set
    private val boardHistory: MutableList<SingleGameState> = mutableListOf()
    override var state = determineGameState()
        private set
    private var time: Long? = null

    constructor(state: org.secuso.privacyfriendly2048.activities.helper.GameState): this(
        GameConfig(
            boardSize = state.n,
            cellsToFill = 0,
            wonOnValue = 2048,
            distribution = GameBoard.SpawnProbabilityDistribution(listOf(GameBoard.SpawnProbability(2, 0.9), GameBoard.SpawnProbability(4, 0.1)))
        ),
        state.numbers.toList().chunked(state.n).map { it.toTypedArray() }.toTypedArray()
    ) {
        val previous = SingleGameState(
            state.numbers.toList().chunked(state.n).map { it.toTypedArray() }.toTypedArray(),
            points = state.last_points.toLong()
        )
        boardHistory.add(previous)
    }

    private fun determineGameState(): GameState {
        return if (board.data.flatten().contains(config.wonOnValue)) {
            GameState.WON
        } else if (board.freeCells.isEmpty() && !board.anyMergePossible()) {
            GameState.LOST
        } else {
            GameState.RUNNING
        }
    }

    override fun board() = board.data

    override fun start() {
        time = System.currentTimeMillis()
        if (board.filledCells.isEmpty()) {
            board.fillRandomCell(1, config.distribution)
        }
    }

    override fun stop() {
        time?.let {
            stats.addTimePlayed(System.currentTimeMillis() - it)
        }
        time = null
    }

    override fun undo() {
       boardHistory.lastOrNull()?.let { (board, points) ->
           this.board.replaceBoard(board)
           this.points = points
           if (points > stats.record) {
               stats.record = points.toLong()
           }
           stats.undo()
           boardHistory.removeAt(boardHistory.lastIndex)
       }
    }

    override fun canUndo() = boardHistory.isNotEmpty()

    override fun move(direction: Direction): List<GameBoard.BoardChangeEvent> {
        // make a deep copy of the board state
        board.data.map { it -> it.clone() }.apply {
            boardHistory.add(SingleGameState(toTypedArray(), points))
        }
        val events = board.move(direction).toMutableList()
        events.forEach {
            if (it is GameBoard.BoardChangeEvent.MergeEvent) {
                val value = board.data[it.target.first][it.target.second]
                points += value
                stats.highestNumber = value.toLong()
                stats.record = points
            }
        }
        events += board.fillRandomCell(config.cellsToFill, config.distribution)

        when (direction) {
            Direction.LEFT -> stats.moveL()
            Direction.RIGHT -> stats.moveR()
            Direction.UP -> stats.moveT()
            Direction.DOWN -> stats.moveD()
        }

        if (state != GameState.WON) {
            determineGameState().let {
                if (it != GameState.RUNNING) {
                    stop()
                }
                state = it
            }
        }
        return events
    }

    fun saveStatsToFile(out: FileOutputStream) {
        stop()
        ObjectOutputStream(out).apply {
            writeObject(stats)
            close()
        }
        start()
    }

    fun readStatsFromFile(ips: FileInputStream) {
        ObjectInputStream(ips).apply {
            stats = readObject() as GameStatistics
            close()
        }
    }

    fun saveGameToFile(out: FileOutputStream) {
        ObjectOutputStream(out).apply {
            writeObject(config)
            writeObject(board)
            writeLong(points)
            writeObject(boardHistory.last())
        }
    }

    companion object {
        fun readGameFromFile(ips: FileInputStream): Game2048 {
            ObjectInputStream(ips).apply {
                when(val obj = readObject()) {
                    is org.secuso.privacyfriendly2048.activities.helper.GameState -> {
                        return Game2048(obj)
                    }
                    is GameConfig -> {
                        val board = readObject() as GameBoard
                        val game = Game2048(obj, board.data)
                        game.points = readLong()
                        game.boardHistory.add(readObject() as SingleGameState)
                        return game
                    }
                    else -> throw IllegalStateException("class ${obj::class.java} can not be serialized into a Game2048 instance.")
                }
            }
        }
    }
}