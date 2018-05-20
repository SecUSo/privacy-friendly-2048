/*
 This file is part of Privacy Friendly App Example.

 Privacy Friendly App Example is free software:
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

package org.secuso.privacyfriendlyexample.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.secuso.privacyfriendlyexample.R;
import org.secuso.privacyfriendlyexample.activities.helper.BaseActivity;
import org.secuso.privacyfriendlyexample.activities.helper.GameState;
import org.secuso.privacyfriendlyexample.activities.helper.Gesten;
import org.secuso.privacyfriendlyexample.activities.helper.element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Christopher Beckmann
 * @version 20161225
 * This activity is an example for the main menu of gaming applications
 */

public class GameActivity extends BaseActivity {
    public static int n = 4;
    public TextView textFieldPoints;
    public TextView textFieldRecord;
    public int numberFieldSize = 0;
    static element [][] elements = null;
    static GameState gameState = null;
    RelativeLayout number_field;
    RelativeLayout touch_field;
    public static int points = 0;
    public static int record = 0;


    public static boolean moved = false;
    public static boolean initialize = false;
    public static boolean firstTime = true;
    public static boolean newGame;
    public boolean won2048=false;
    public static boolean gameOver = false;
    public static boolean createNewGame = true;

    View.OnTouchListener swipeListener;

    static String filename;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("ON","Create" + savedInstanceState +  " ");
        Intent intent = getIntent();
        if(intent.getBooleanExtra("new",true))
            createNewGame = true;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_game);



        number_field = (RelativeLayout) findViewById(R.id.number_field);
        touch_field = (RelativeLayout) findViewById(R.id.touch_field) ;
        textFieldPoints = (TextView) findViewById(R.id.points);
        textFieldRecord = (TextView) findViewById(R.id.record);


        number_field.setBackgroundColor(Color.rgb(187,173,159));

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        Log.d("ONCONFIGCHANGE", "CALLED" );
        createNewGame = false;
        super.onConfigurationChanged(newConfig);
    }
    @Override
    protected int getNavigationDrawerID() {
        return 0;
    }

    @Override
    public void onBackPressed() {
        saveStateToFile(gameState);
        saveRecordToFile(record);
        super.onBackPressed();

    }

    protected void start()
    {
        Log.d("started","STARTED_________");

        android.view.ViewGroup.LayoutParams lp = number_field.getLayoutParams();

        //setting squared Number Field
        if(number_field.getHeight()>number_field.getWidth())
            lp.height = number_field.getWidth();
        else
            lp.width = number_field.getHeight();
        number_field.setLayoutParams(lp);

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
        Log.i("initializing","initializeState");
        Intent intent = getIntent();
        n = intent.getIntExtra("n", 4);
        newGame = intent.getBooleanExtra("new", true);
        filename = intent.getStringExtra("filename");
        record = readRecordFromFile();
        if (!newGame) {
            gameState = readStateFromFile();
            points = gameState.points;
        } else {
            gameState = new GameState(n);
            newGame = true;
        }
        elements = new element[n][n];

    }
    public void updateGameState()
    {
        gameState.n = n;
        gameState.points = points;
        gameState.record = record;
        check2048();

    }

    public void initialize()
    {
        Log.i("init","initializing");
        //NavigationBar Listener
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if(gameState!= null) {
                    saveStateToFile(gameState);
                    saveRecordToFile(record);
                }

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        if(getIntent().getIntExtra("n",4)!=n||createNewGame)
            initializeState();
        createNewGame = false;
        Log.i("gameState",""+gameState);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int abstand = (10* metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
        numberFieldSize = number_field.getWidth();
        if(numberFieldSize>number_field.getHeight())
            numberFieldSize = number_field.getHeight();
        int number_size = (numberFieldSize-abstand)/n-abstand;

        textFieldRecord.setText(""+record);
        textFieldPoints.setText(""+points);


        for(int i = 0; i < elements.length; i++) {
            for (int j = 0; j < elements[i].length; j++) {
                elements[i][j] = new element(this);
                elements[i][j].setNumber(gameState.getNumber(i,j));
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(number_size ,number_size);
                lp.setMarginStart(abstand+j*(number_size+abstand));
                lp.topMargin = abstand+i*(number_size+abstand);
                elements[i][j].setLayoutParams(lp);
                elements[i][j].updateFontSize();
                number_field.addView(elements[i][j]);
            }
        }
        if(elements!=null)
            Log.i("number field",""+display(elements));
        if(newGame)
        {
            moved = true;
            addNumber();
            newGame = false;
        }
    }
    public void setListener()
    {
        swipeListener = new Gesten(this){
            public boolean onSwipeTop() {
                moved = false;
                element s = new element(getApplicationContext());

                for(int i = 0; i < elements.length;i++)
                {
                    s.number =  elements[0][i].number;
                    s.posX = 0;
                    s.posY = i;


                    for(int j = 1; j<elements[i].length;j++)
                    {
                        if(elements[j][i].number != 0 &&( s.number == 0 || s.number == elements[j][i].number))
                        {
                            moved=true;
                            elements[s.posX][s.posY].setNumber(s.number + elements[j][i].number);
                            if(s.number!=0)
                                points += elements[s.posX][s.posY].number;
                            elements[j][i].setNumber(0);
                            if(s.number !=0)
                                s.posX++;
                            j=s.posX;
                            s.number = elements[j][i].number;

                        }
                        else if(elements[j][i].number != 0)
                        {
                            s.number = elements[j][i].number;
                            s.posX = j;
                            s.posY = i;
                        }
                    }

                }
                for(int i = 0; i < elements.length;i++)
                {
                    s.number =  elements[0][i].number;
                    s.posX = 0;
                    s.posY = i;


                    for(int j = 1; j<elements[i].length;j++)
                    {
                        if(elements[j][i].number != 0 && s.number == 0)
                        {
                            moved=true;
                            elements[s.posX][s.posY].setNumber(s.number + elements[j][i].number);
                            elements[j][i].setNumber(0);
                            if(s.number !=0)
                                s.posX++;
                            j=s.posX;
                            s.number = elements[j][i].number;

                        }
                        else if(s.number != 0)
                        {
                            s.number = elements[j][i].number;
                            s.posX = j;
                            s.posY = i;
                        }
                    }

                }
                addNumber();
                updateGameState();
                Log.d("TAG","up");
                //es wurde nach oben gewischt, hier den Code einfügen
                return false;
            }
            public boolean onSwipeRight() {
                moved = false;
                element s = new element(getApplicationContext());
                for(int i = 0; i < elements.length;i++)
                {
                    s.number =  elements[i][elements[i].length-1].number;
                    s.posX = i;
                    s.posY = elements[i].length-1;


                    for(int j = elements[i].length-2; j >= 0;j--)
                    {
                        if(elements[i][j].number != 0 &&( s.number == 0 || s.number == elements[i][j].number))
                        {
                            moved=true;
                            elements[s.posX][s.posY].setNumber(s.number + elements[i][j].number);
                            if(s.number!=0)
                                points += elements[s.posX][s.posY].number;
                            elements[i][j].setNumber(0);
                            if(s.number !=0)
                                s.posY--;
                            j=s.posY;
                            s.number = elements[i][j].number;
                        }
                        else if(elements[i][j].number != 0)
                        {
                            s.number = elements[i][j].number;
                            s.posX = i;
                            s.posY = j;
                        }
                    }

                }
                for(int i = 0; i < elements.length;i++)
                {
                    s.number =  elements[i][elements[i].length-1].number;
                    s.posX = i;
                    s.posY = elements[i].length-1;


                    for(int j = elements[i].length-2; j >= 0;j--)
                    {
                        if(elements[i][j].number != 0 && s.number == 0 )
                        {
                            moved=true;
                            elements[s.posX][s.posY].setNumber(s.number + elements[i][j].number);
                            elements[i][j].setNumber(0);
                            if(s.number !=0)
                                s.posY--;
                            j=s.posY;
                            s.number = elements[i][j].number;
                        }
                        else if(s.number != 0)
                        {
                            s.number = elements[i][j].number;
                            s.posX = i;
                            s.posY = j;
                        }
                    }

                }
                addNumber();
                updateGameState();
                Log.d("TAG","right");

                //es wurde nach rechts gewischt, hier den Code einfügen
                return false;
            }
            public boolean onSwipeLeft() {
                moved = false;
                element s = new element(getApplicationContext());
                for(int i = 0; i < elements.length;i++)
                {
                    s.number =  elements[i][0].number;
                    s.posX = i;
                    s.posY = 0;


                    for(int j = 1; j<elements[i].length;j++)
                    {
                        if(elements[i][j].number != 0 &&( s.number == 0 || s.number == elements[i][j].number))
                        {
                            moved=true;
                            elements[s.posX][s.posY].setNumber(s.number + elements[i][j].number);
                            if(s.number!=0)
                                points += elements[s.posX][s.posY].number;
                            elements[i][j].setNumber(0);
                            if(s.number !=0)
                                s.posY++;
                            j=s.posY;
                            s.number = elements[i][j].number;
                        }
                        else if(elements[i][j].number != 0)
                        {
                            s.number = elements[i][j].number;
                            s.posX = i;
                            s.posY = j;
                        }
                    }

                }
                for(int i = 0; i < elements.length;i++)
                {
                    s.number =  elements[i][0].number;
                    s.posX = i;
                    s.posY = 0;

                    for(int j = 1; j<elements[i].length;j++)
                    {
                        if(elements[i][j].number != 0 && s.number == 0)
                        {
                            moved=true;
                            elements[s.posX][s.posY].setNumber(s.number + elements[i][j].number);
                            elements[i][j].setNumber(0);
                            if(s.number !=0)
                                s.posY++;
                            j=s.posY;
                            s.number = elements[i][j].number;
                        }
                        else if(s.number != 0)
                        {
                            s.number = elements[i][j].number;
                            s.posX = i;
                            s.posY = j;
                        }
                    }

                }
                addNumber();
                updateGameState();
                Log.d("TAG","left");
                //es wurde nach links gewischt, hier den Code einfügen
                return false;
            }
            public boolean onSwipeBottom() {
                moved = false;
                element s = new element(getApplicationContext());
                for(int i = 0; i < elements.length;i++)
                {
                    s.number =  elements[elements[i].length-1][i].number;
                    s.posX = elements[i].length-1;
                    s.posY = i;


                    for(int j = elements[i].length-2; j>=0;j--)
                    {
                        if(elements[j][i].number != 0 &&( s.number == 0 || s.number == elements[j][i].number))
                        {
                            moved=true;
                            elements[s.posX][s.posY].setNumber(s.number + elements[j][i].number);
                            if(s.number!=0)
                                points += elements[s.posX][s.posY].number;
                            elements[j][i].setNumber(0);
                            if(s.number !=0)
                                s.posX--;
                            j=s.posX;
                            s.number = elements[j][i].number;
                        }
                        else if(elements[j][i].number != 0)
                        {
                            s.number = elements[j][i].number;
                            s.posX = j;
                            s.posY = i;
                        }
                    }

                }
                for(int i = 0; i < elements.length;i++)
                {
                    s.number =  elements[elements[i].length-1][i].number;
                    s.posX = elements[i].length-1;
                    s.posY = i;


                    for(int j = elements[i].length-2; j>=0;j--)
                    {
                        if(elements[j][i].number != 0 &&s.number == 0)
                        {
                            moved=true;
                            elements[s.posX][s.posY].setNumber(s.number + elements[j][i].number);
                            elements[j][i].setNumber(0);
                            if(s.number !=0)
                                s.posX--;
                            j=s.posX;
                            s.number = elements[j][i].number;
                        }
                        else if(s.number != 0)
                        {
                            s.number = elements[j][i].number;
                            s.posX = j;
                            s.posY = i;
                        }
                    }

                }
                addNumber();
                updateGameState();
                Log.d("TAG","down");
                //es wurde nach unten gewischt, hier den Code einfügen
                return false;
            }
            public boolean nichts(){
                Log.d("TAG","nothing");
                //es wurde keine wischrichtung erkannt, hier den Code einfügen
                return false;
            }
        };
        touch_field.setOnTouchListener(swipeListener);
        number_field.setOnTouchListener(swipeListener);
        for(int i = 0; i < elements.length; i++) {
            for (int j = 0; j < elements[i].length; j++) {
                elements[i][j].setOnTouchListener(swipeListener);
            }
        }
    }
    public void check2048()
    {
        if(won2048 == false)
        for(int i = 0; i < elements.length;i++)
        {
            for(int j = 0; j < elements[i].length;j++)
            {
                if(elements[i][j].number==64)
                {
                    Log.i("INFO","2048 erreicht");
                    //MESSAGE
                    new AlertDialog.Builder(this)
                            .setTitle("Gewonnen")
                            .setMessage("Sie haben gewonnen, wollen Sie weiter spielen?")
                            .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i("Ende","nein");
                                    onBackPressed();

                                }
                            })
                            .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i("Ende","ja");
                                }
                            }).create().show();
                    won2048=true;
                }
            }
        }
    }
    public void addNumber()
    {
        if(points>record) {
            record = points;
            textFieldRecord.setText(""+record);
        }
        if(moved) {
            moved = false;
            textFieldPoints.setText("" + points);
            element[] empty_fields = new element[n * n];
            int counter = 0;
            for (int i = 0; i < elements.length; i++) {
                for (int j = 0; j < elements[i].length; j++) {
                    if (elements[i][j].number == 0) {
                        empty_fields[counter++] = elements[i][j];
                    }
                }
            }
            if(counter>0) {
                int index = (int) (Math.random() * counter);
                int number = 2;
                if (Math.random() > 0.5)
                    number = 4;
                empty_fields[index].setNumber(number);
                if(counter == 1)
                {
                    gameOver = true;
                    for (int i = 0; i < elements.length; i++) {
                        for (int j = 0; j < elements[i].length; j++) {
                            if ((i+1 < elements.length && elements[i][j].number == elements[i+1][j].number)|| (j+1 < elements[i].length && elements[i][j].number == elements[i][j+1].number)) {
                                gameOver = false;
                                break;
                            }
                        }
                    }
                    if(gameOver)
                    {
                        gameOver();
                    }
                        Log.i("Game","over");
                }
            }

            gameState = new GameState(elements);
        }
    }
    public void gameOver()
    {
        new AlertDialog.Builder(this)
                .setTitle("Verloren")
                .setMessage("Sie haben verloren, wollen Sie ein neues Spiel starten?")
                .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("Ende","nein");
                        Log.i("StateFile ", "deleted: " + deleteStateFile(filename));
                        saveRecordToFile(record);
                        GameActivity.super.onBackPressed();

                    }
                })
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("Ende","ja");
                        createNewGame = true;
                        getIntent().putExtra("new",true);
                        initialize();
                    }
                }).create().show();
    }
    public String display(element[][] e)
    {
        String result = "";
        for(int i = 0; i < e.length; i++)
        {
            for(int j = 0; j < e[i].length;j++)
                result = result + " " + elements[i][j].number;
            result = result + "\n";
        }
        return result;
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
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        Log.i("saving","onSaveInstanceState");

        saveStateToFile(gameState);
        saveRecordToFile(record);

    }
    public void saveStateToFile(GameState nS)
    {
        Log.i("saving", ""+nS);
        try {
            if(filename == null)
                filename = "state" + n + ".txt";
            File file = new File(getFilesDir(), filename);
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(nS);
            out.close();
            fileOut.close();
            Log.i("Save","System data has been saved in " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void saveRecordToFile(int r)
    {
        try {

            File file = new File(getFilesDir(), "record" + n + ".txt");
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(r);
            out.close();
            fileOut.close();
            Log.i("Save","record data has been saved in " + "record" + n + ".txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean deleteStateFile(String filename)
    {
        try{
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
    public int readRecordFromFile()
    {
        int result = 0;
        try{
            File file = new File(getFilesDir(), "record" + n + ".txt");
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            result = (int)in.readObject();
            Log.i("reading", "record"+result);
            in.close();
            fileIn.close();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }
    public GameState readStateFromFile()
    {
        GameState nS = new GameState(n);
        try{
            File file = new File(getFilesDir(), filename);
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            nS = (GameState)in.readObject();
            Log.i("reading", "ungefiltert"+nS);
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
            Log.i("reading", ""+nS);
            in.close();
            fileIn.close();
            Log.i("Save","System data has been readed in " + filename);
        }
        catch(Exception e)
        {
            newGame = true;
            e.printStackTrace();
        }
        return nS;
    }
}
