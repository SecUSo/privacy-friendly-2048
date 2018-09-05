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

package org.secuso.privacyfriendly2048.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.secuso.privacyfriendly2048.R;
import org.secuso.privacyfriendly2048.activities.helper.BaseActivityWithoutNavBar;
import org.secuso.privacyfriendly2048.activities.helper.GameState;
import org.secuso.privacyfriendly2048.activities.helper.GameStatistics;
import org.secuso.privacyfriendly2048.activities.helper.Gesten;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

/**
 * In this class the game activity is implemented
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180807
 */

@SuppressWarnings("StringConcatenationInLoop")
public class GameActivity extends BaseActivityWithoutNavBar {
    public static int n = 4;
    public TextView textFieldPoints;
    public TextView textFieldRecord;
    public int numberFieldSize = 0;
    static Element[][] Elements = null;
    static Element[][] last_elements = null;
    static Element[][] backgroundElements;
    static GameState gameState = null;

    RelativeLayout number_field;
    RelativeLayout number_field_background;
    RelativeLayout touch_field;
    ImageButton restartButton;
    ImageButton undoButton;
    public static int points = 0;
    public static int last_points = 0;
    public static long record = 0;

    public static long ADDINGSPEED = 100;
    public static long MOVINGSPEED = 80;
    public static long SCALINGSPEED = 100;
    public static float SCALINGFACTOR = 1.1f;

    public static boolean moved = false;
    public static boolean firstTime = true;
    public static boolean newGame;
    public boolean won2048=false;
    public static boolean gameOver = false;
    public static boolean createNewGame = true;
    public static boolean undo = false;
    public static boolean animationActivated = true;
    public static boolean saveState = true;

    public final int WINTHRESHOLD = 2048;
    public final double PROPABILITYFORTWO = 0.9;
    View.OnTouchListener swipeListener;


    static String filename;

    SharedPreferences sharedPref;

    GameStatistics gameStatistics = new GameStatistics(n);
    public static long startingTime;
    public int highestNumber;


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item= menu.findItem(R.id.action_settings);
        item.setVisible(false);
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public void onPause() {
        Log.i("lifecycle","pause");
        save();

        super.onPause();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saveState = true;
        Intent intent = getIntent();
        if(firstTime && intent.getBooleanExtra("new",true)) {
            createNewGame = true;
            firstTime = false;
        }


        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        animationActivated = sharedPref.getBoolean("pref_animationActivated",true);

        if(sharedPref.getBoolean("settings_display",true))
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_game);




        number_field = (RelativeLayout) findViewById(R.id.number_field);
        number_field_background = (RelativeLayout) findViewById(R.id.number_field_background);
        touch_field = (RelativeLayout) findViewById(R.id.touch_field) ;
        textFieldPoints = (TextView) findViewById(R.id.points);
        textFieldRecord = (TextView) findViewById(R.id.record);
        restartButton = (ImageButton) findViewById(R.id.restartButton);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveStatisticsToFile(gameStatistics);
                createNewGame();
            }
        });
        undoButton = (ImageButton) findViewById(R.id.undoButton);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoButton.setVisibility(View.INVISIBLE);
                if(undo&&last_elements != null) {
                    gameStatistics.undo();
                    Elements = last_elements;
                    points = last_points;
                    number_field.removeAllViews();
                    number_field_background.removeAllViews();
                    points = last_points;
                    textFieldPoints.setText(""+points);
                    setDPositions(false);
                    for(Element[] i : Elements)
                    {
                        for(Element j: i)
                        {
                            j.setVisibility(View.INVISIBLE);
                            number_field.addView(j);
                            j.drawItem();
                        }
                    }
                    for(int i = 0; i < Elements.length; i++) {
                        for (int j = 0; j < Elements[i].length; j++) {
                            Elements[i][j].setOnTouchListener(swipeListener);
                            backgroundElements[i][j].setOnTouchListener(swipeListener);
                        }
                    }
                    updateGameState();
                    drawAllElements(Elements);
                    number_field.refreshDrawableState();
                }
                undo = false;
            }
        });

        //number_field.setBackgroundColor((this.getResources().getColor(R.color.background_gamebord)));
        startingTime = Calendar.getInstance().getTimeInMillis();

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        createNewGame = false;
        super.onConfigurationChanged(newConfig);
    }

    public void save()
    {
        Log.i("saving","save");
        if(!createNewGame)
            saveStateToFile(gameState);
        gameStatistics.addTimePlayed(Calendar.getInstance().getTimeInMillis()-startingTime);
        startingTime = Calendar.getInstance().getTimeInMillis();
        saveStatisticsToFile(gameStatistics);
        firstTime = true;
    }
    @Override
    public void onBackPressed() {
        save();

        super.onBackPressed();

    }

    public void createNewGame()
    {
        createNewGame = true;
        getIntent().putExtra("new",true);
        number_field.removeAllViews();
        number_field_background.removeAllViews();
        initialize();

    }

    protected void start()
    {
        Log.i("activity","start");
        saveState = true;
        android.view.ViewGroup.LayoutParams lp = number_field.getLayoutParams();

        //setting squared Number Field
        if(number_field.getHeight()>number_field.getWidth())
            lp.height = number_field.getWidth();
        else
            lp.width = number_field.getHeight();
        number_field.setLayoutParams(lp);
        number_field_background.setLayoutParams(lp);

        initialize();
        setListener();
        if(newGame) {
            moved = true;
            addNumber();
        }
        newGame = false;

    }
    public void initializeState()
    {
        points = 0;
        Intent intent = getIntent();
        n = intent.getIntExtra("n", 4);
        newGame = intent.getBooleanExtra("new", true);
        filename = intent.getStringExtra("filename");
        undo = intent.getBooleanExtra("undo",false);
        if (!newGame) {
            gameState = readStateFromFile();
            points = gameState.points;
            last_points = gameState.last_points;
        } else {
            gameState = new GameState(n);
            newGame = true;
        }
        Elements = new Element[n][n];
        last_elements = new Element[n][n];
        backgroundElements = new Element[n][n];
        saveState = true;



    }

    public void drawAllElements(Element[][] e)
    {
        for(Element[] i : e)
        {
            for(Element j: i)
            {
                j.drawItem();
            }
        }
    }

    public void updateGameState()
    {
        gameState = new GameState(Elements,last_elements);
        gameState.n = n;
        gameState.points = points;
        gameState.last_points = last_points;
        gameState.undo = undo;
        updateHighestNumber();
        check2048();

    }

    public void initialize()
    {
        Log.i("activity","initialize");
        if(getIntent().getIntExtra("n",4)!=n||createNewGame)
        {
            initializeState();

        }
        gameStatistics = readStatisticsFromFile();
        record = gameStatistics.getRecord();
        last_points = gameState.last_points;
        createNewGame = false;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int abstand = (10* metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
        numberFieldSize = number_field.getWidth();
        if(numberFieldSize>number_field.getHeight())
            numberFieldSize = number_field.getHeight();
        int number_size = (numberFieldSize-abstand)/n-abstand;

        textFieldRecord.setText(""+record);
        textFieldPoints.setText(""+points);
        if(undo)
            undoButton.setVisibility(View.VISIBLE);
        else
            undoButton.setVisibility(View.INVISIBLE);

        number_field_background.removeAllViews();
        number_field.removeAllViews();
        for(int i = 0; i < Elements.length; i++) {
            for (int j = 0; j < Elements[i].length; j++) {
                //background elements
                backgroundElements[i][j] = new Element(this);
                //backgroundElements[i][j].setVisibility(View.INVISIBLE);

                Elements[i][j] = new Element(this);
                Elements[i][j].setNumber(gameState.getNumber(i,j));

                Elements[i][j].drawItem();
                if(Elements[i][j].getNumber() >= WINTHRESHOLD)
                    won2048 = true;
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(number_size ,number_size);
                lp.setMarginStart(abstand+j*(number_size+abstand));
                lp.topMargin = abstand+i*(number_size+abstand);
                Elements[i][j].setDPosition(lp.getMarginStart(),lp.topMargin);
                Elements[i][j].setLayoutParams(lp);
                backgroundElements[i][j].setLayoutParams(lp);
                Elements[i][j].updateFontSize();
                backgroundElements[i][j].setLayoutParams(lp);
                number_field_background.addView(backgroundElements[i][j]);
                number_field.addView(Elements[i][j]);
            }
        }
        last_elements =deepCopy(Elements);
        if(undo)
        {
            for(int i = 0; i < Elements.length; i++) {
                for (int j = 0; j < Elements[i].length; j++) {
                    last_elements[i][j].setNumber(gameState.getLastNumber(i,j));
                }
            }
        }
        if(newGame)
        {
            moved = true;
            addNumber();
            moved = true;
            addNumber();
            newGame = false;
        }
    }
    public void switchElementPositions(Element e1, Element e2)
    {
        int i = e1.getdPosX();
        int j = e1.getdPosY();

        e1.animateMoving = true;
        e1.setDPosition(e2.getdPosX(),e2.getdPosY());
        e2.animateMoving = false;
        e2.setDPosition(i,j);

    }

    public Element[][] deepCopy(Element[][]e)
    {
        Element[][] r = new Element[e.length][];
        for(int i = 0; i < r.length; i++)
        {
            r[i] = new Element[e[i].length];
            for(int j = 0; j < r[i].length; j++)
            {
                r[i][j] = e[i][j].copy();
            }
        }
        return r;
    }

    public void setListener()
    {
        swipeListener = new Gesten(this){
            public boolean onSwipeTop() {
                Element[][] temp = deepCopy(Elements);
                int temp_points = points;
                moved = false;
                Element s = new Element(getApplicationContext());

                for(int i = 0; i < Elements.length; i++)
                {
                    s.number =  Elements[0][i].number;
                    s.posX = 0;
                    s.posY = i;


                    for(int j = 1; j< Elements[i].length; j++)
                    {
                        if(Elements[j][i].number != 0 &&( s.number == 0 || s.number == Elements[j][i].number))
                        {
                            moved=true;
                            Elements[j][i].setNumber(s.number + Elements[j][i].number);
                            Elements[s.posX][s.posY].setNumber(0);
                            switchElementPositions(Elements[j][i], Elements[s.posX][s.posY]);
                            Element z = Elements[j][i];
                            Elements[j][i] = Elements[s.posX][s.posY];
                            Elements[s.posX][s.posY] = z;
                            if(s.number!=0)
                                points += Elements[s.posX][s.posY].number;
                            if(s.number !=0)
                                s.posX++;
                            j=s.posX;
                            s.number = Elements[j][i].number;

                        }
                        else if(Elements[j][i].number != 0)
                        {
                            s.number = Elements[j][i].number;
                            s.posX = j;
                            s.posY = i;
                        }
                    }

                }
                for(int i = 0; i < Elements.length; i++)
                {
                    s.number =  Elements[0][i].number;
                    s.posX = 0;
                    s.posY = i;


                    for(int j = 1; j< Elements[i].length; j++)
                    {
                        if(Elements[j][i].number != 0 && s.number == 0)
                        {
                            moved=true;
                            Elements[j][i].setNumber(s.number + Elements[j][i].number);
                            Elements[s.posX][s.posY].setNumber(0);
                            switchElementPositions(Elements[j][i], Elements[s.posX][s.posY]);
                            Element z = Elements[j][i];
                            Elements[j][i] = Elements[s.posX][s.posY];
                            Elements[s.posX][s.posY] = z;
                            if(s.number !=0)
                                s.posX++;
                            j=s.posX;
                            s.number = Elements[j][i].number;

                        }
                        else if(s.number != 0)
                        {
                            s.number = Elements[j][i].number;
                            s.posX = j;
                            s.posY = i;
                        }
                    }

                }
                if(moved) {
                    gameStatistics.addMoves(1);
                    last_points = temp_points;
                    last_elements = temp;
                    undoButton.setVisibility(View.VISIBLE);
                    undo = true;
                }
                if(moved)
                    gameStatistics.moveT();
                addNumber();
                setDPositions(animationActivated);
                updateGameState();
                //es wurde nach oben gewischt, hier den Code einfügen
                return false;
            }
            public boolean onSwipeRight() {
                Element[][] temp = deepCopy(Elements);
                int temp_points = points;
                moved = false;
                Element s = new Element(getApplicationContext());
                for(int i = 0; i < Elements.length; i++)
                {
                    s.number =  Elements[i][Elements[i].length-1].number;
                    s.posX = i;
                    s.posY = Elements[i].length-1;


                    for(int j = Elements[i].length-2; j >= 0; j--)
                    {
                        if(Elements[i][j].number != 0 &&( s.number == 0 || s.number == Elements[i][j].number))
                        {
                            moved=true;

                            Elements[i][j].setNumber(s.number + Elements[i][j].number);
                            Elements[s.posX][s.posY].setNumber(0);
                            switchElementPositions(Elements[i][j], Elements[s.posX][s.posY]);
                            Element z = Elements[i][j];
                            Elements[i][j] = Elements[s.posX][s.posY];
                            Elements[s.posX][s.posY] = z;

                            if(s.number!=0)
                                points += Elements[s.posX][s.posY].number;
                            if(s.number !=0)
                                s.posY--;
                            j=s.posY;
                            s.number = Elements[i][j].number;
                        }
                        else if(Elements[i][j].number != 0)
                        {
                            s.number = Elements[i][j].number;
                            s.posX = i;
                            s.posY = j;
                        }
                    }

                }
                for(int i = 0; i < Elements.length; i++)
                {
                    s.number =  Elements[i][Elements[i].length-1].number;
                    s.posX = i;
                    s.posY = Elements[i].length-1;


                    for(int j = Elements[i].length-2; j >= 0; j--)
                    {
                        if(Elements[i][j].number != 0 && s.number == 0 )
                        {
                            moved=true;

                            Elements[i][j].setNumber(s.number + Elements[i][j].number);
                            Elements[s.posX][s.posY].setNumber(0);
                            switchElementPositions(Elements[i][j], Elements[s.posX][s.posY]);
                            Element z = Elements[i][j];
                            Elements[i][j] = Elements[s.posX][s.posY];
                            Elements[s.posX][s.posY] = z;


                            if(s.number !=0)
                                s.posY--;
                            j=s.posY;
                            s.number = Elements[i][j].number;
                        }
                        else if(s.number != 0)
                        {
                            s.number = Elements[i][j].number;
                            s.posX = i;
                            s.posY = j;
                        }
                    }

                }
                if(moved) {
                    gameStatistics.addMoves(1);
                    last_points = temp_points;
                    last_elements = temp;
                    undoButton.setVisibility(View.VISIBLE);
                    undo = true;
                }
                if(moved)
                    gameStatistics.moveR();
                addNumber();
                setDPositions(animationActivated);
                updateGameState();

                //es wurde nach rechts gewischt, hier den Code einfügen
                return false;
            }
            public boolean onSwipeLeft() {
                Element[][] temp = deepCopy(Elements);
                int temp_points = points;
                moved = false;
                Element s = new Element(getApplicationContext());
                for(int i = 0; i < Elements.length; i++)
                {
                    s.number =  Elements[i][0].number;
                    s.posX = i;
                    s.posY = 0;


                    for(int j = 1; j< Elements[i].length; j++)
                    {
                        if(Elements[i][j].number != 0 &&( s.number == 0 || s.number == Elements[i][j].number))
                        {
                            moved=true;


                            Elements[i][j].setNumber(s.number + Elements[i][j].number);
                            Elements[s.posX][s.posY].setNumber(0);
                            switchElementPositions(Elements[i][j], Elements[s.posX][s.posY]);
                            Element z = Elements[i][j];
                            Elements[i][j] = Elements[s.posX][s.posY];
                            Elements[s.posX][s.posY] = z;

                            if(s.number!=0)
                                points += Elements[s.posX][s.posY].number;
                            if(s.number !=0)
                                s.posY++;
                            j=s.posY;
                            s.number = Elements[i][j].number;
                        }
                        else if(Elements[i][j].number != 0)
                        {
                            s.number = Elements[i][j].number;
                            s.posX = i;
                            s.posY = j;
                        }
                    }

                }
                for(int i = 0; i < Elements.length; i++)
                {
                    s.number =  Elements[i][0].number;
                    s.posX = i;
                    s.posY = 0;

                    for(int j = 1; j< Elements[i].length; j++)
                    {
                        if(Elements[i][j].number != 0 && s.number == 0)
                        {
                            moved=true;

                            Elements[i][j].setNumber(s.number + Elements[i][j].number);
                            Elements[s.posX][s.posY].setNumber(0);
                            switchElementPositions(Elements[i][j], Elements[s.posX][s.posY]);
                            Element z = Elements[i][j];
                            Elements[i][j] = Elements[s.posX][s.posY];
                            Elements[s.posX][s.posY] = z;

                            if(s.number !=0)
                                s.posY++;
                            j=s.posY;
                            s.number = Elements[i][j].number;
                        }
                        else if(s.number != 0)
                        {
                            s.number = Elements[i][j].number;
                            s.posX = i;
                            s.posY = j;
                        }
                    }

                }
                if(moved) {
                    gameStatistics.addMoves(1);
                    last_points = temp_points;
                    last_elements = temp;
                    undoButton.setVisibility(View.VISIBLE);
                    undo = true;
                }
                if(moved)
                    gameStatistics.moveL();
                addNumber();
                setDPositions(animationActivated);
                updateGameState();
                //es wurde nach links gewischt, hier den Code einfügen
                return false;
            }
            public boolean onSwipeBottom() {
                Element[][] temp = deepCopy(Elements);
                int temp_points = points;
                moved = false;
                Element s = new Element(getApplicationContext());
                for(int i = 0; i < Elements.length; i++)
                {
                    s.number =  Elements[Elements[i].length-1][i].number;
                    s.posX = Elements[i].length-1;
                    s.posY = i;


                    for(int j = Elements[i].length-2; j>=0; j--)
                    {
                        if(Elements[j][i].number != 0 &&( s.number == 0 || s.number == Elements[j][i].number))
                        {
                            moved=true;

                            Elements[j][i].setNumber(s.number + Elements[j][i].number);
                            Elements[s.posX][s.posY].setNumber(0);
                            switchElementPositions(Elements[j][i], Elements[s.posX][s.posY]);
                            Element z = Elements[j][i];
                            Elements[j][i] = Elements[s.posX][s.posY];
                            Elements[s.posX][s.posY] = z;

                            if(s.number!=0)
                                points += Elements[s.posX][s.posY].number;
                            if(s.number !=0)
                                s.posX--;
                            j=s.posX;
                            s.number = Elements[j][i].number;
                        }
                        else if(Elements[j][i].number != 0)
                        {
                            s.number = Elements[j][i].number;
                            s.posX = j;
                            s.posY = i;
                        }
                    }

                }
                for(int i = 0; i < Elements.length; i++)
                {
                    s.number =  Elements[Elements[i].length-1][i].number;
                    s.posX = Elements[i].length-1;
                    s.posY = i;


                    for(int j = Elements[i].length-2; j>=0; j--)
                    {
                        if(Elements[j][i].number != 0 &&s.number == 0)
                        {
                            moved=true;

                            Elements[j][i].setNumber(s.number + Elements[j][i].number);
                            Elements[s.posX][s.posY].setNumber(0);
                            switchElementPositions(Elements[j][i], Elements[s.posX][s.posY]);
                            Element z = Elements[j][i];
                            Elements[j][i] = Elements[s.posX][s.posY];
                            Elements[s.posX][s.posY] = z;

                            if(s.number !=0)
                                s.posX--;
                            j=s.posX;
                            s.number = Elements[j][i].number;
                        }
                        else if(s.number != 0)
                        {
                            s.number = Elements[j][i].number;
                            s.posX = j;
                            s.posY = i;
                        }
                    }

                }
                if(moved) {
                    gameStatistics.addMoves(1);
                    last_points = temp_points;
                    last_elements = temp;
                    undoButton.setVisibility(View.VISIBLE);
                    undo = true;
                }
                if(moved)
                    gameStatistics.moveD();
                addNumber();
                setDPositions(animationActivated);
                updateGameState();
                //es wurde nach unten gewischt, hier den Code einfügen
                return false;
            }
            public boolean nichts(){
                //es wurde keine wischrichtung erkannt, hier den Code einfügen
                return false;
            }
        };
        touch_field.setOnTouchListener(swipeListener);
        number_field.setOnTouchListener(swipeListener);
        for(int i = 0; i < Elements.length; i++) {
            for (int j = 0; j < Elements[i].length; j++) {
                Elements[i][j].setOnTouchListener(swipeListener);
                backgroundElements[i][j].setOnTouchListener(swipeListener);
            }
        }
    }
    public String display(Element[][] e)
    {
        String result = "\n";
        for(int i = 0; i < e.length; i++)
        {
            for(int j = 0; j < e[i].length;j++)
                result = result + " " + Elements[i][j].number; //+ " "+elements[i][j];
            result = result + "\n";
        }
        result += "\n";
        for(int i = 0; i < e.length; i++)
        {
            for(int j = 0; j < e[i].length;j++)
                result = result + " (" + Elements[i][j].getX() + " , " + Elements[i][j].getY() + ")" + " v:" + Elements[i][j].getVisibility();//+" "+elements[i][j];
            result = result + "\n";
        }
        return result;
    }

    public void updateHighestNumber()
    {
        for(int i = 0; i < Elements.length; i++)
        {
            for(int j = 0; j < Elements[i].length; j++)
            {
                if(highestNumber < Elements[i][j].number)
                {
                    highestNumber = Elements[i][j].number;
                    gameStatistics.setHighestNumber(highestNumber);
                }
            }
        }
    }

    public void check2048()
    {
        if(won2048 == false)
        for(int i = 0; i < Elements.length; i++)
        {
            for(int j = 0; j < Elements[i].length; j++)
            {
                if(Elements[i][j].number==WINTHRESHOLD)
                {

                    saveStatisticsToFile(gameStatistics);
                    //MESSAGE
                    new AlertDialog.Builder(this)
                            .setTitle((this.getResources().getString(R.string.Titel_V_Message)))
                            .setMessage((this.getResources().getString(R.string.Winning_Message)))
                            .setNegativeButton((this.getResources().getString(R.string.No_Message)), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onBackPressed();

                                }
                            })
                            .setPositiveButton((this.getResources().getString(R.string.Yes_Message)), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setCancelable(false)
                            .create().show();
                    won2048=true;
                }
            }
        }
    }



    public void setDPositions(boolean animation)
    {
        long SCALINGSPEED = GameActivity.SCALINGSPEED;
        long ADDINGSPEED = GameActivity.ADDINGSPEED;
        long MOVINGSPEED = GameActivity.MOVINGSPEED;
        boolean scale = true;
        if(!animation)
        {
            SCALINGSPEED = 1;
            ADDINGSPEED = 1;
            MOVINGSPEED = 1;
            scale = false;
        }
        for(Element[] i: Elements)
        {
            for(Element j:i)
            {
                if(j.dPosX != j.getX())
                {
                    if(j.animateMoving&&animation)
                    {
                        if(j.number != j.dNumber)
                            j.animate().x(j.dPosX).setDuration(MOVINGSPEED).setStartDelay(0).setInterpolator(new LinearInterpolator()).setListener(new MovingListener(j,scale)).start();
                        else
                            j.animate().x(j.dPosX).setDuration(MOVINGSPEED).setStartDelay(0).setInterpolator(new LinearInterpolator()).setListener(new MovingListener(j,false)).start();

                    }
                    else {
                        if(!animation) {
                            ViewGroup.MarginLayoutParams lp1 = (ViewGroup.MarginLayoutParams) j.getLayoutParams();
                            lp1.leftMargin = j.dPosX;
                            j.setLayoutParams(lp1);
                            j.drawItem();
                        }
                        else
                            j.animate().x(j.dPosX).setDuration(0).setStartDelay(MOVINGSPEED).setInterpolator(new LinearInterpolator()).setListener(new MovingListener(j,false)).start();

                    }

                }
                if(j.dPosY != j.getY())
                {
                    if(j.animateMoving&&animation)
                    {
                        if(j.number != j.dNumber)
                            j.animate().y(j.dPosY).setDuration(MOVINGSPEED).setStartDelay(0).setInterpolator(new LinearInterpolator()).setListener(new MovingListener(j,scale)).start();
                        else
                            j.animate().y(j.dPosY).setDuration(MOVINGSPEED).setStartDelay(0).setInterpolator(new LinearInterpolator()).setListener(new MovingListener(j,false)).start();

                    }
                    else {
                        if(!animation)
                        {
                            ViewGroup.MarginLayoutParams lp1 = (ViewGroup.MarginLayoutParams) j.getLayoutParams();
                            lp1.topMargin = j.dPosY;
                            j.setLayoutParams(lp1);
                            j.drawItem();
                        }
                        else
                            j.animate().y(j.dPosY).setDuration(0).setStartDelay(MOVINGSPEED).setInterpolator(new LinearInterpolator()).setListener(new MovingListener(j,false)).start();

                    }


                }
            }
        }
    }

    class MovingListener extends AnimatorListenerAdapter {
        Element e = null;
        long SCALINGSPEED = 100;
        float scalingFactor = 1.5f;
        boolean scale =false;
        public MovingListener(Element e, boolean scale )
        {
            super();
            this.e = e;
            this.SCALINGSPEED = GameActivity.SCALINGSPEED;
            this.scalingFactor = GameActivity.SCALINGFACTOR;
            this.scale = scale;
        }
        @Override
        public void onAnimationCancel(Animator animation) {
            super.onAnimationCancel(animation);
            animation.setupEndValues();
            if(e!=null)
                e.drawItem();
        }
        @Override
        public void onAnimationPause(Animator animation){
            super.onAnimationPause(animation);
        }
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            if(e!=null) {
                e.drawItem();
                if(scale)
                    e.animate().scaleX(scalingFactor).scaleY(scalingFactor).setDuration(SCALINGSPEED).setStartDelay(0).setInterpolator(new LinearInterpolator()).setListener(new ScalingListener(e)).start();
            }

        }
    }

    class ScalingListener extends AnimatorListenerAdapter {
        Element e = null;
        public ScalingListener(Element e)
        {
            super();
            this.e = e;
        }
        public ScalingListener()
        {
            super();
        }
        @Override
        public void onAnimationCancel(Animator animation) {
            super.onAnimationCancel(animation);
            animation.setupEndValues();
        }
        @Override
        public void onAnimationPause(Animator animation){
            super.onAnimationPause(animation);
        }
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            if(e!=null) {
                e.animate().scaleX(1.0f).scaleY(1.0f).setDuration(SCALINGSPEED).setStartDelay(0).setInterpolator(new LinearInterpolator()).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                    }
                }).start();
            }

        }
    }

    public void addNumber()
    {

        if(points>record) {
            record = points;
            gameStatistics.setRecord(record);
            textFieldRecord.setText(""+record);
        }
        if(moved) {
            gameOver = false;
            moved = false;
            textFieldPoints.setText("" + points);
            Element[] empty_fields = new Element[n * n];
            int counter = 0;
            for (int i = 0; i < Elements.length; i++) {
                for (int j = 0; j < Elements[i].length; j++) {
                    if (Elements[i][j].number == 0) {
                        empty_fields[counter++] = Elements[i][j];
                    }
                }
            }
            if(counter>0) {
                int index = (int) (Math.random() * counter);
                int number = 2;
                if (Math.random() > PROPABILITYFORTWO)
                    number = 4;

                empty_fields[index].setNumber(number);
                empty_fields[index].drawItem();
                if(animationActivated){
                    empty_fields[index].setAlpha(0);
                    empty_fields[index].animate().alpha(1).setInterpolator(new LinearInterpolator()).setStartDelay(MOVINGSPEED).setDuration(ADDINGSPEED).start();
                }
                if(counter == 1)
                {
                    gameOver = true;
                    for (int i = 0; i < Elements.length; i++) {
                        for (int j = 0; j < Elements[i].length; j++) {
                            if ((i+1 < Elements.length && Elements[i][j].number == Elements[i+1][j].number)|| (j+1 < Elements[i].length && Elements[i][j].number == Elements[i][j+1].number)) {
                                gameOver = false;
                            }
                        }
                    }
                }
            }
            updateGameState();

            if(gameOver)
            {
                gameOver();
            }
        }
        Log.i("number of elements", ""+number_field.getChildCount() + ", "+ number_field_background.getChildCount());
    }
    public void gameOver()
    {
        Log.i("record",""+record + ", " + gameStatistics.getRecord());
        saveStatisticsToFile(gameStatistics);
        new AlertDialog.Builder(this)
                .setTitle((this.getResources().getString(R.string.Titel_L_Message, points)))
                .setMessage(this.getResources().getString(R.string.Lost_Message, points))
                .setNegativeButton((this.getResources().getString(R.string.No_Message)), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createNewGame = true;
                        getIntent().putExtra("new",true);
                        initialize();
                        deleteStateFile();
                        saveState = false;
                        GameActivity.this.onBackPressed();

                    }
                })
                .setPositiveButton((this.getResources().getString(R.string.Yes_Message)), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createNewGame();
                    }
                })
                .setCancelable(false)
                .create().show();
        Log.i("record","danach");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_settings) {
            return true;
        }
        else if( id == android.R.id.home){
            save();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        start();


    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

       save();

    }

    public void saveStateToFile(GameState nS)
    {
        if(saveState)
        try {
            if(filename == null)
                filename = "state" + n + ".txt";
            File file = new File(getFilesDir(), filename);
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(nS);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteStateFile()
    {
        try{
            if(filename == null)
                filename = "state" + n + ".txt";
            File directory = getFilesDir();
            File f = new File(directory,filename);
            return f.delete();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }
    public GameState readStateFromFile()
    {
        GameState nS = new GameState(n);
        try{
            File file = new File(getFilesDir(), filename);
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            nS = (GameState)in.readObject();
            boolean emptyField = true;
            for(int i = 0; i <nS.numbers.length;i++)
            {
                if(nS.numbers[i]>0)
                {
                    emptyField = false;
                    break;
                }
            }
            if(emptyField||nS.n != n) {
                nS = new GameState(n);
                newGame = true;
            }
            in.close();
            fileIn.close();
        }
        catch(Exception e)
        {
            newGame = true;
            e.printStackTrace();
        }
        return nS;
    }
    public GameStatistics readStatisticsFromFile()
    {
        GameStatistics gS = new GameStatistics(n);
        try{
            File file = new File(getFilesDir(), "statistics" + n + ".txt");
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            gS = (GameStatistics)in.readObject();
            in.close();
            fileIn.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return gS;
    }
    public void saveStatisticsToFile(GameStatistics gS)
    {
        try {
            File file = new File(getFilesDir(), gS.getFilename());
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(gS);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
