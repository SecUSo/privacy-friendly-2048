package org.secuso.privacyfriendly2048.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class Game2048Test {
    @Test
    fun undo() {
        val config = Game2048.GameConfig(
            4,
            1,
            GameBoard.SpawnProbabilityDistribution(listOf(GameBoard.SpawnProbability(2, 0.9), GameBoard.SpawnProbability(4,0.1))),
            2048
        )
        val test: Array<Array<Int>> = arrayOf(
            arrayOf(0, 0, 0, 2),
            arrayOf(0, 0, 0, 2),
            arrayOf(0, 0, 0, 2),
            arrayOf(0, 0, 0, 2),
        )
        val control: Array<Array<Int>> = arrayOf(
            arrayOf(0, 0, 0, 2),
            arrayOf(0, 0, 0, 2),
            arrayOf(0, 0, 0, 2),
            arrayOf(0, 0, 0, 2),
        )
        val game = Game2048(config, test)
        game.move(Direction.UP)
        assertEquals(8, game.board()[0][3])
        assert(game.canUndo())
        game.undo()
        assertArrayEquals(control, game.board())
        assertNotEquals(8, game.board()[0][3])
    }
}