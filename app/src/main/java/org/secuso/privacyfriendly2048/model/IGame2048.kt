package org.secuso.privacyfriendly2048.model

typealias Board = Array<Array<Int>>

enum class Direction(val x: Int, val y: Int) {
    UP(0,-1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    operator fun component1(): Int = x
    operator fun component2(): Int = y
}

enum class GameState {
    RUNNING,
    LOST,
    WON;
}

interface IGameConfig {
    val boardSize: Int
    val cellsToFill: Int
    val distribution: GameBoard.SpawnProbabilityDistribution
    val wonOnValue: Int
}

interface IGame2048 {
    fun start()
    fun stop()
    fun undo()
    fun move(direction: Direction): List<GameBoard.BoardChangeEvent>
    fun board(): Board
    fun canUndo(): Boolean
    val state: GameState
    val stats: GameStatistics
    val points: Long
}