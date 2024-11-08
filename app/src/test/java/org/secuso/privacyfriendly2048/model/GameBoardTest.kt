package org.secuso.privacyfriendly2048.model

import org.junit.Assert.*
import org.junit.Test

class GameBoardTest {

    @Test
    fun move() {
        val init: Array<Array<Int>> = arrayOf(
            arrayOf(0, 0, 0, 0),
            arrayOf(0, 2, 0, 0),
            arrayOf(0, 0, 2, 0),
            arrayOf(0, 0, 0, 2),
        )
        val board = GameBoard(4, init)

        // expected after moves: r
        val first: Array<Array<Int>> = arrayOf(
            arrayOf(0, 0, 0, 0),
            arrayOf(0, 0, 0, 2),
            arrayOf(0, 0, 0, 2),
            arrayOf(0, 0, 0, 2),
        )
        board.move(GameBoard.Direction.RIGHT)
        assertArrayEquals(first, board.data)

        // expected after moves: r, r
        val second: Array<Array<Int>> = arrayOf(
            arrayOf(0, 0, 0, 0),
            arrayOf(0, 0, 0, 2),
            arrayOf(0, 0, 0, 2),
            arrayOf(0, 0, 0, 2),
        )
        board.move(GameBoard.Direction.RIGHT)
        assertArrayEquals(second, board.data)

        // expected after moves: r, r, u
        val third: Array<Array<Int>> = arrayOf(
            arrayOf(0, 0, 0, 4),
            arrayOf(0, 0, 0, 2),
            arrayOf(0, 0, 0, 0),
            arrayOf(0, 0, 0, 0),
        )
        board.move(GameBoard.Direction.UP)
        assertArrayEquals(third, board.data)

        // expected after moves: r, r, u, d
        val fourth: Array<Array<Int>> = arrayOf(
            arrayOf(0, 0, 0, 0),
            arrayOf(0, 0, 0, 0),
            arrayOf(0, 0, 0, 4),
            arrayOf(0, 0, 0, 2),
        )
        board.move(GameBoard.Direction.DOWN)
        assertArrayEquals(fourth, board.data)

        // expected after moves: r, r, u, d, l
        val fifth: Array<Array<Int>> = arrayOf(
            arrayOf(0, 0, 0, 0),
            arrayOf(0, 0, 0, 0),
            arrayOf(4, 0, 0, 0),
            arrayOf(2, 0, 0, 0),
        )
        board.move(GameBoard.Direction.LEFT)
        assertArrayEquals(fifth, board.data)
    }

    @Test
    fun fill() {
        val init: Array<Array<Int>> = arrayOf(
            arrayOf(0, 0, 0, 0),
            arrayOf(0, 2, 0, 0),
            arrayOf(0, 0, 2, 0),
            arrayOf(0, 0, 0, 2),
        )
        val board = GameBoard(4, init)
        val distribution = GameBoard.SpawnProbabilityDistribution(
            listOf(
                GameBoard.SpawnProbability(2, 0.53),
                GameBoard.SpawnProbability(4, 0.32),
                GameBoard.SpawnProbability(8, 0.15)
            )
        )
        board.fillRandomCell(1, distribution)
        assertEquals(board.filledCells.size, 4)

        board.fillRandomCell(3, distribution)
        assertEquals(board.filledCells.size, 7)

        board.fillRandomCell(20, distribution)
        assertEquals(board.filledCells.size, 16)
        assertEquals(board.freeCells.size, 0)

        board.fillRandomCell(1, distribution)
        assertEquals(board.filledCells.size, 16)
        assertEquals(board.freeCells.size, 0)
    }
}