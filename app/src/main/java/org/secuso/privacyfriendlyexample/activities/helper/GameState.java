package org.secuso.privacyfriendlyexample.activities.helper;


import android.os.Parcel;
import android.util.Log;

import java.io.Serializable;

public class GameState implements Serializable {
    public int n = 4;
    public int[] numbers;
    public int points = 0;
    public int record = 0;

    public GameState(int size)
    {
        numbers = new int[size*size];
    }
    public GameState(int [][] e)
    {
        int length = 1;
        for(int i = 0; i < e.length; i++)
        {
            if(e[i].length > length)
                length = e[i].length;
        }
        this.n = e.length;
        numbers = new int[e.length*e.length];
        int c = 0;
        for(int i = 0; i < e.length;i++)
        {
            for(int j = 0; j < e[i].length;j++){
                numbers[c++] = e[i][j];
            }
        }
    }
    public GameState(element[][] e)
    {
        int length = 1;
        for(int i = 0; i < e.length; i++)
        {
            if(e[i].length > length)
                length = e[i].length;
        }
        this.n = e.length;
        numbers = new int[e.length*e.length];
        int c = 0;
        for(int i = 0; i < e.length;i++)
        {
            for(int j = 0; j < e[i].length;j++){

                numbers[c++] = e[i][j].number;
            }
        }
    }
    public int getNumber(int i, int j)
    {

        try {
            return numbers[i*n+j];
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            Log.d("ERROR","i: " + i + " j: " + j + "i*n+j" + (i*n+j));
            e.printStackTrace();
        }

        return 0;
    }




    @Override
    public String toString() {
        String result = "numbers: ";
        for (int i:numbers) {
            result += i + " ";
        }
        result += ", n: " + n;
        result += ", points: " + points;
        result += ", record: " + record;
        return result;
    }
}
