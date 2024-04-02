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


package org.secuso.privacyfriendly2048.activities.helper;

import java.io.Serializable;

/**
 * The current statistics in each modes is defined in this class.
 * It contains the highest reached number, the amount of swipes (total and for each direction), playing time, highest score and the amount of using the undo button.
 * For saving this data in a file, the interface java.io.Serializable is implemented.
 *
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180910
 */
public class GameStatistics implements Serializable {
    private long moves = 0;
    private long timePlayed = 0;
    private long highestNumber = 2;
    private int n = 4;
    private String filename = "statistics" + n + ".txt";
    private long record = 0;
    private int undo = 0;
    private int moves_l = 0;
    private int moves_r = 0;
    private int moves_t = 0;
    private int moves_d = 0;

    public GameStatistics(int n) {
        this.n = n;
        filename = "statistics" + n + ".txt";
    }


    public long getHighestNumber() {
        return highestNumber;
    }

    public void setHighestNumber(long highestNumber) {
        if (this.highestNumber < highestNumber)
            this.highestNumber = highestNumber;
    }

    public long getTimePlayed() {
        return timePlayed;
    }

    public void addTimePlayed(long timePlayed) {
        this.timePlayed += timePlayed;
    }

    public boolean resetTimePlayed() {
        this.timePlayed = 0;
        return true;
    }

    public long getMoves() {
        return moves;
    }

    public void addMoves(long moves) {
        this.moves += moves;
    }

    public String getFilename() {
        return filename;
    }

    public long getRecord() {
        return record;
    }

    public void setRecord(long record) {
        this.record = record;
    }

    public void undo() {
        undo++;
    }

    public void moveL() {
        moves_l++;
    }

    public void moveR() {
        moves_r++;
    }

    public void moveT() {
        moves_t++;
    }

    public void moveD() {
        moves_d++;
    }

    public int getUndo() {
        return undo;
    }

    @Override
    public String toString() {
        return "moves " + moves +
                " timePlayed " + timePlayed / 1000.0f +
                " highest Number " + highestNumber +
                " record" + record;
    }

    public int getMoves_d() {
        return moves_d;
    }

    public int getMoves_t() {
        return moves_t;
    }

    public int getMoves_r() {
        return moves_r;
    }

    public int getMoves_l() {
        return moves_l;
    }
}
