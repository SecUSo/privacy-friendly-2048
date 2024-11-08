package org.secuso.privacyfriendly2048.model

typealias Board = Array<Array<Int>>

class Game2048(val config: GameConfig, boardState: Array<Array<Int>>? = null) {
    data class SingleGameState(val data: Array<Array<Int>>, val points: Int)

    data class GameConfig(
        val boardSize: Int,
        val cellsToFill: Int,
        val distribution: GameBoard.SpawnProbabilityDistribution,
        val wonOnValue: Int
    )

    enum class GameState {
        RUNNING,
        LOST,
        WON;
    }

    private var board = GameBoard(config.boardSize, boardState)
    private var points = 0
    private val boardHistory: MutableList<SingleGameState> = mutableListOf()
    private var state = determineGameState()

    init {
        board.onMergeListener = GameBoard.OnMergeListener { _, _, mergedValue -> points += mergedValue }
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

    fun undo() {
       boardHistory.lastOrNull()?.let { (board, points) ->
           this.board.replaceBoard(board)
           this.points = points
           boardHistory.removeAt(boardHistory.lastIndex)
       }
    }

    fun move(direction: GameBoard.Direction) {
        if (state != GameState.RUNNING) {
            return
        }
        board.move(direction)
        board.fillRandomCell(config.cellsToFill, config.distribution)
        boardHistory.add(SingleGameState(board.data, points))
    }
}