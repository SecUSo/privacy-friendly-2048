package org.secuso.privacyfriendly2048.model

import android.os.Parcel
import android.os.Parcelable
import kotlin.math.max

class GameBoard(private val size: Int, data: Array<Array<Int>>? = null): Parcelable {

    enum class Direction(val x: Int, val y: Int) {
        UP(0,-1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0);

        operator fun component1(): Int = x
        operator fun component2(): Int = y
    }
    
    data class SpawnProbability(val number: Int, val probability: Double) {
        init {
            if (probability < 0 || probability > 1) {
                throw IllegalArgumentException("The probability is: $probability. Should be 0 <= $probability <= 1")
            }
        }
    }

    data class SpawnProbabilityDistribution(private val distribution: List<SpawnProbability>) {
        init {
            val sum: Double = distribution.map { it.probability }.reduce { acc, prob -> acc + prob }
            if (sum - 1 > 1e-8) {
                throw IllegalArgumentException("The probability sum is: $sum. Needs to be $sum < 1 and almost at 1.")
            }
        }

        fun generate(): Int {
            var r = Math.random()
            val index = distribution.foldIndexed(-1) { index, acc, p ->
                if (p.probability < r) {
                    r -= p.probability
                } else if (acc < 0) {
                    return@foldIndexed index
                }
                return@foldIndexed acc
            }
            return distribution[max(index, 0)].number
        }
    }

    fun interface OnMergeListener {
        fun onMerge(index: Pair<Int, Int>, direction: Direction, mergedValue: Int)
    }

    var onMergeListener: OnMergeListener? = null

    var data: Array<Array<Int>> = data ?:
        (0 until size).map {
            (0 until size).map { 0 }.toTypedArray()
        }.toTypedArray()
        private set


    val freeCells
        get() = data.mapIndexed { i, row ->
            row.mapIndexed { j, cell -> (i to j) to cell }
        }.flatten().filter { cell ->
            cell.second == 0
        }.map { cell -> cell.first }

    val filledCells
        get() = data.mapIndexed { i, row ->
            row.mapIndexed { j, cell -> (i to j) to cell }
        }.flatten().filter { cell ->
            cell.second != 0
        }.map { cell -> cell.first }

    override fun toString() = data.joinToString("\n") { it.joinToString(",") }

    fun replaceBoard(data: Array<Array<Int>>) {
        this.data = data
    }

    fun move(direction: Direction) {
        val (x, y) = direction

        // Adjust the direction we iterate through according to the direction to move the cells
        // Iterating over and moving the cells using the same direction will yield an incorrect result
        //
        // i is the row index, j the column index
        for (i in if (y <= 0) 0 until size else size - 1 downTo 0) {
            for (j in if (x <= 0) 0 until size else size - 1 downTo 0) {
                // Bound-checks
                if (i + y < 0 || i + y >= size || j + x < 0 || j + x >= size) {
                    continue
                }

                // Skip empty cells
                if (data[i][j] == 0) {
                    continue
                }

                // Move the number to the edge of the direction
                // This can be achieved through iterating from (i,j) in the (x,y) direction
                // until the edge is reached, thus a maximum of boardSize times
                //
                // This isn't the most efficient approach, but is rather simple and less error-prone
                // and easy to maintain.
                for (offset in 0 until size) {
                    val (iSource, jSource) = Pair(i + y * offset, j + x * offset)
                    if (iSource < 0 || iSource >= size || jSource < 0 || jSource >= size) {
                        break
                    }
                    val (iTarget, jTarget) = Pair(iSource + y, jSource + x)
                    if (iTarget < 0 || iTarget >= size || jTarget < 0 || jTarget >= size) {
                        break
                    }

                    // The two cells contain the same number -- merge them into Target
                    if (data[iTarget][jTarget] == data[iSource][jSource]) {
                        data[iTarget][jTarget] *= 2
                        data[iSource][jSource] = 0
                        onMergeListener?.onMerge(iTarget to jTarget, direction, data[iTarget][jTarget])
                    } else if (data[iTarget][jTarget] == 0) {
                        data[iTarget][jTarget] = data[iSource][jSource]
                        data[iSource][jSource] = 0
                    } else {
                        // We cannot move the cell anymore
                        break
                    }
                }
            }
        }
    }

    fun fillRandomCell(amount: Int, distribution: SpawnProbabilityDistribution) {
        val possibleCells = freeCells.mapIndexed { index, pair -> index to pair  }.toMutableList()
        if (possibleCells.size <= amount) {
            possibleCells.forEach { (_,boardIndex) -> data[boardIndex.first][boardIndex.second] = distribution.generate() }
        } else {
            (0 until amount).forEach { _ ->
                val (cellIndex, boardIndex) = possibleCells.random()
                data[boardIndex.first][boardIndex.second] = distribution.generate()
                possibleCells.removeAt(cellIndex)
            }
        }
    }

    fun anyMergePossible() =
        filledCells
            .flatMap { (i,j) -> (Direction.entries.map { (x,y) -> data[i][j] to ((i + y) to (j + x)) }) }
            .filter { (_,index) -> index.first in 0 until size && index.second in 0 until size }
            .map { (value, index) -> value to data[index.first][index.second] }
            .any { (value, neighbour) -> value == neighbour }

    constructor(parcel: Parcel): this(
        parcel.readInt(),
        parcel.readInt().let {
            val data = IntArray(it * it)
            parcel.readIntArray(data)
            return@let data.toTypedArray().let { arr ->
                (0 until it).map { n -> arr.copyOfRange(n * it, (n + 1) * it) }
            }.toTypedArray()
        }
    )


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        // write the boardSize twice to first retrieve the size for the boardSize field
        // and the second to retrieve the correct data format
        parcel.writeInt(size)
        parcel.writeIntArray(data.flatten().toTypedArray().toIntArray())
        parcel.writeInt(size)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<GameBoard> {
        override fun createFromParcel(parcel: Parcel): GameBoard {
            return GameBoard(parcel)
        }

        override fun newArray(size: Int): Array<GameBoard?> {
            return arrayOfNulls(size)
        }
    }
}