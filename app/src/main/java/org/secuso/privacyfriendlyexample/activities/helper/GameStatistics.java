package org.secuso.privacyfriendlyexample.activities.helper;

import java.io.Serializable;

public class GameStatistics implements Serializable {
    private long moves = 0;
    private long timePlayed = 0;
    private long highestNumber = 2;
    private int n = 4;
    private String filename = "statistics" + n + ".txt";
    private long record = 0;

    public GameStatistics(int n)
    {
        this.n = n;
        filename = "statistics" + n + ".txt";
    }


    public long getHighestNumber() {
        return highestNumber;
    }

    public void setHighestNumber(long highestNumber) {
        if(this.highestNumber < highestNumber)
                this.highestNumber = highestNumber;
    }

    public long getTimePlayed() {
        return timePlayed;
    }

    public void addTimePlayed(long timePlayed) {
        this.timePlayed += timePlayed;
    }

    public boolean resetTimePlayed()
    {
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

    @Override
    public String toString() {
        return "moves " + moves +
               " timePlayed " + timePlayed/1000.0f +
                " highest Number " + highestNumber +
                " record" + record;
    }
}
