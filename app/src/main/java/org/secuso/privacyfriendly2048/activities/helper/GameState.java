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


import org.secuso.privacyfriendly2048.activities.Element;

import java.io.Serializable;

/**
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180807
 */
public class GameState implements Serializable {
    public int n = 4;
    public int[] numbers;
    public int[] last_numbers;
    public int points = 0;
    public int last_points = 0;
    public boolean undo = false;

    public GameState(int size) {
        numbers = new int[size * size];
    }

    public GameState(int[][] e) {
        int length = 1;
        for (int i = 0; i < e.length; i++) {
            if (e[i].length > length)
                length = e[i].length;
        }
        this.n = e.length;
        numbers = new int[e.length * e.length];
        int c = 0;
        for (int i = 0; i < e.length; i++) {
            for (int j = 0; j < e[i].length; j++) {
                numbers[c++] = e[i][j];
            }
        }
        last_numbers = numbers;
    }

    public GameState(Element[][] e, Element[][] e2) {
        int length = 1;
        for (int i = 0; i < e.length; i++) {
            if (e[i].length > length)
                length = e[i].length;
        }
        this.n = e.length;
        numbers = new int[e.length * e.length];
        int c = 0;
        for (int i = 0; i < e.length; i++) {
            for (int j = 0; j < e[i].length; j++) {

                numbers[c++] = e[i][j].number;
            }
        }
        length = 1;
        for (int i = 0; i < e2.length; i++) {
            if (e2[i].length > length)
                length = e2[i].length;
        }
        last_numbers = new int[e2.length * e2.length];
        c = 0;
        for (int i = 0; i < e2.length; i++) {
            for (int j = 0; j < e2[i].length; j++) {

                last_numbers[c++] = e2[i][j].number;
            }
        }
    }

    public int getNumber(int i, int j) {

        try {
            return numbers[i * n + j];
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int getLastNumber(int i, int j) {

        try {
            return last_numbers[i * n + j];
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return 0;
    }


    @Override
    public String toString() {
        String result = "numbers: ";
        for (int i : numbers) {
            result += i + " ";
        }
        result += ", n: " + n;
        result += ", points: " + points;
        result += ", undo: " + undo;
        return result;
    }
}
