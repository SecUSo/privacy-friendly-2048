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
package org.secuso.privacyfriendly2048.model

import java.io.Serializable

/**
 * The current statistics in each modes is defined in this class.
 * It contains the highest reached number, the amount of swipes (total and for each direction), playing time, highest score and the amount of using the undo button.
 * For saving this data in a file, the interface java.io.Serializable is implemented.
 *
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180910
 */
class GameStatistics(n: Int) : Serializable {
    val moves: Long
        get() = moves_d.toLong() + moves_t + moves_l + moves_r
    var timePlayed: Long = 0
        private set
    private var highestNumber: Long = 2
    private var n = 4
    @JvmField
    var filename: String = "statistics$n.txt"
    @JvmField
    var record: Long = 0
    var undo: Int = 0
        private set
    var moves_l: Int = 0
        private set
    var moves_r: Int = 0
        private set
    var moves_t: Int = 0
        private set
    var moves_d: Int = 0
        private set

    init {
        this.n = n
        filename = "statistics$n.txt"
    }


    fun getHighestNumber(): Long {
        return highestNumber
    }

    fun setHighestNumber(highestNumber: Long) {
        if (this.highestNumber < highestNumber) this.highestNumber = highestNumber
    }

    fun setRecord(record: Long) {
        if (this.record < record) this.record = record
    }

    fun addTimePlayed(timePlayed: Long) {
        this.timePlayed += timePlayed
    }

    fun resetTimePlayed(): Boolean {
        this.timePlayed = 0
        return true
    }

    fun undo() {
        undo++
    }

    fun moveL() {
        moves_l++
    }

    fun moveR() {
        moves_r++
    }

    fun moveT() {
        moves_t++
    }

    fun moveD() {
        moves_d++
    }

    override fun toString(): String {
        return "moves " + moves +
                " timePlayed " + timePlayed / 1000.0f +
                " highest Number " + highestNumber +
                " record" + record
    }
}
