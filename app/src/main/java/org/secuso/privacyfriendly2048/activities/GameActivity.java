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

import static org.secuso.privacyfriendly2048.helpers.ThemeResolverKt.GetColorInt;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.PathInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.secuso.privacyfriendly2048.R;
import org.secuso.privacyfriendly2048.activities.helper.BaseActivityWithoutNavBar;
import org.secuso.privacyfriendly2048.activities.helper.GameState;
import org.secuso.privacyfriendly2048.activities.helper.GameStatistics;
import org.secuso.privacyfriendly2048.activities.helper.Gestures;
import org.secuso.privacyfriendly2048.helpers.SpringInterpolator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

/**
 * This activity contains the entire game and draws the game field depending on the selected mode and the screen size.
 * Also the Intent influences, when a new game is generated or a previous game status is loaded.
 * Therefore a two dimensional field named elements is filled with instances of the class Element depending on the selected mode (4x4, 5x5, 6x6, 7x7).
 * The function of the two buttons restart and undo are also implemented.
 * With the help of the class Gesture the user inputs are recognized and the corresponding method is retrieved.
 * This method contains the important game logic for 2048.
 * With loops the values of the elements will be added and the whole elements will be moved in the selected direction.
 * After each move (only if something has been moved) a new element appears on the game field with the help of the method addNumber().
 * This new element is placed randomly on a free spot of the game field.
 * If the activity is paused or stopped, the game status is saved in the GameState and the GameStatistics will be updated.
 *
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180910
 */

@SuppressWarnings("StringConcatenationInLoop")
public class GameActivity extends BaseActivityWithoutNavBar {
    public static int n = 4;
    public TextView textFieldPoints;
    public TextView textFieldRecord;
    public int numberFieldSize = 0;
    static Element[][] elements = null;
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

    // tile motion lives in TileAnimations; new tiles appear right after the slide has landed
    public static long MOVINGSPEED = TileAnimations.SLIDE_MS;
    public static long SPAWNDELAY = TileAnimations.SLIDE_MS + 16;

    // UI micro-interactions (score counter, undo button, button press)
    private static final TimeInterpolator EASE_OUT = new PathInterpolator(0.2f, 0f, 0f, 1f);
    private static final TimeInterpolator EASE_IN = new PathInterpolator(0.4f, 0f, 1f, 1f);
    private static final TimeInterpolator PRESS_RELEASE = new SpringInterpolator(0.55, 0.32, 420);
    private static final TimeInterpolator SCORE_POP = new SpringInterpolator.Pulse(0.5, 0.28, 300);
    private long lastMergeHaptic = 0;
    private boolean undoShown = false;

    public static boolean moved = false;
    public static boolean firstTime = true;
    public static boolean newGame;
    public boolean won2048 = false;
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

    private Activity myActivity;


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_settings);
        item.setVisible(false);
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public void onPause() {
        Log.i("lifecycle", "pause");
        save();

        super.onPause();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myActivity = this;
        saveState = true;

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (firstTime && intent.getBooleanExtra("new", true)) {
                createNewGame = true;
                firstTime = false;
            }
        }

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        animationActivated = sharedPref.getBoolean("pref_animationActivated", true);

        if (sharedPref.getBoolean("settings_display", true))
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (animationActivated)
            requestHighestRefreshRate();
        setContentView(R.layout.activity_game);

        initResources();


    }

    /**
     * Asks for the panel's fastest mode (e.g. 120 Hz) at the current resolution while the game
     * is in front, so the springs are rendered with every frame the display can show.
     * Some ROMs cap unlisted apps at 60 Hz unless the window asks for more.
     */
    private void requestHighestRefreshRate() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        try {
            Display display = getWindowManager().getDefaultDisplay();
            Display.Mode current = display.getMode();
            Display.Mode best = current;
            for (Display.Mode mode : display.getSupportedModes()) {
                if (mode.getPhysicalWidth() == current.getPhysicalWidth()
                        && mode.getPhysicalHeight() == current.getPhysicalHeight()
                        && mode.getRefreshRate() > best.getRefreshRate()) {
                    best = mode;
                }
            }
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.preferredDisplayModeId = best.getModeId();
            getWindow().setAttributes(params);
        } catch (RuntimeException e) {
            Log.w("display", "could not request refresh rate", e);
        }
    }

    private void handleUndo() {
        setUndoVisible(false);
        if (undo && last_elements != null) {
            gameStatistics.undo();
            elements = last_elements;
            points = last_points;
            number_field.removeAllViews();
            //number_field_background.removeAllViews();
            points = last_points;
            setCounter(textFieldPoints, points, false);
            setDPositions(false);
            for (Element[] i : elements) {
                for (Element j : i) {
                    j.setVisibility(View.INVISIBLE);
                    number_field.addView(j);
                    j.drawItem();
                }
            }
            for (int i = 0; i < elements.length; i++) {
                for (int j = 0; j < elements[i].length; j++) {
                    elements[i][j].setOnTouchListener(swipeListener);
                    backgroundElements[i][j].setOnTouchListener(swipeListener);
                }
            }
            updateGameState();
            drawAllElements(elements);
            number_field.refreshDrawableState();
        }
        undo = false;
    }
    public void initResources() {
        number_field = (RelativeLayout) findViewById(R.id.number_field);
        number_field_background = (RelativeLayout) findViewById(R.id.number_field_background);
        touch_field = (RelativeLayout) findViewById(R.id.touch_field);
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
                handleUndo();
            }
        });
        restartButton.setOnTouchListener(pressFeedback);
        undoButton.setOnTouchListener(pressFeedback);

        //number_field.setBackgroundColor((this.getResources().getColor(R.color.background_gamebord)));
        startingTime = Calendar.getInstance().getTimeInMillis();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        createNewGame = false;
        super.onConfigurationChanged(newConfig);
    }

    public void save() {
        Log.i("saving", "save");
        if (!createNewGame)
            saveStateToFile(gameState);
        gameStatistics.addTimePlayed(Calendar.getInstance().getTimeInMillis() - startingTime);
        startingTime = Calendar.getInstance().getTimeInMillis();
        saveStatisticsToFile(gameStatistics);
        firstTime = true;
    }

    @Override
    public void onBackPressed() {
        save();

        super.onBackPressed();

    }

    public void createNewGame() {
        createNewGame = true;
        getIntent().putExtra("new", true);
        number_field.removeAllViews();
        number_field_background.removeAllViews();
        initialize();

    }

    protected void start() {
        Log.i("activity", "start");
        saveState = true;
        android.view.ViewGroup.LayoutParams lp = number_field.getLayoutParams();

        //setting squared Number Field
        if (number_field.getHeight() > number_field.getWidth())
            lp.height = number_field.getWidth();
        else
            lp.width = number_field.getHeight();
        number_field.setLayoutParams(lp);
        number_field_background.setLayoutParams(lp);

        initialize();
        setListener();
        if (newGame) {
            moved = true;
            addNumber();
        }
        newGame = false;

    }

    public void initializeState() {
        points = 0;
        Intent intent = getIntent();
        n = intent.getIntExtra("n", 4);
        newGame = intent.getBooleanExtra("new", true);
        filename = intent.getStringExtra("filename");
        undo = intent.getBooleanExtra("undo", false);
        if (!newGame) {
            gameState = readStateFromFile();
            points = gameState.points;
            last_points = gameState.last_points;
        } else {
            gameState = new GameState(n);
            newGame = true;
        }
        elements = new Element[n][n];
        last_elements = new Element[n][n];
        backgroundElements = new Element[n][n];
        saveState = true;


    }

    public void drawAllElements(Element[][] e) {
        for (Element[] i : e) {
            for (Element j : i) {
                j.drawItem();
            }
        }
    }

    public void updateGameState() {
        gameState = new GameState(elements, last_elements);
        gameState.n = n;
        gameState.points = points;
        gameState.last_points = last_points;
        gameState.undo = undo;
        updateHighestNumber();
        check2048();

    }

    public void initialize() {
        Log.i("activity", "initialize");
        if (getIntent().getIntExtra("n", 4) != n || createNewGame) {
            initializeState();

        }
        gameStatistics = readStatisticsFromFile();
        record = gameStatistics.getRecord();
        last_points = gameState.last_points;
        createNewGame = false;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int abstand = (10 * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
        numberFieldSize = number_field.getWidth();
        if (numberFieldSize > number_field.getHeight())
            numberFieldSize = number_field.getHeight();
        int number_size = (numberFieldSize - abstand) / n - abstand;
        // continuous-looking corners that scale with the tile, like iOS app icons
        float tileRadius = number_size * 0.16f;

        setCounter(textFieldRecord, record, false);
        setCounter(textFieldPoints, points, false);
        undoButton.animate().cancel();
        undoButton.setAlpha(1f);
        undoButton.setScaleX(1f);
        undoButton.setScaleY(1f);
        undoShown = undo;
        if (undo)
            undoButton.setVisibility(View.VISIBLE);
        else
            undoButton.setVisibility(View.INVISIBLE);

        GradientDrawable board = new GradientDrawable();
        board.setColor(GetColorInt(this, R.attr.boardBackground));
        board.setCornerRadius(tileRadius + abstand * 0.6f);
        number_field_background.setBackground(board);

        number_field_background.removeAllViews();
        number_field.removeAllViews();
        for (int i = 0; i < elements.length; i++) {
            for (int j = 0; j < elements[i].length; j++) {
                //background elements
                backgroundElements[i][j] = new Element(this);
                backgroundElements[i][j].setCornerRadius(tileRadius);
                //backgroundElements[i][j].setVisibility(View.INVISIBLE);

                elements[i][j] = new Element(this);
                elements[i][j].setCornerRadius(tileRadius);
                elements[i][j].setTileSize(number_size);
                elements[i][j].setNumber(gameState.getNumber(i, j));

                elements[i][j].drawItem();
                if (elements[i][j].getNumber() >= WINTHRESHOLD)
                    won2048 = true;
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(number_size, number_size);
                lp.setMarginStart(abstand + j * (number_size + abstand));
                lp.topMargin = abstand + i * (number_size + abstand);
                elements[i][j].setDPosition(lp.getMarginStart(), lp.topMargin);
                elements[i][j].setLayoutParams(lp);
                backgroundElements[i][j].setLayoutParams(lp);
                backgroundElements[i][j].setLayoutParams(lp);
                backgroundElements[i][j].setOnTouchListener(swipeListener);
                elements[i][j].setOnTouchListener(swipeListener);
                number_field_background.addView(backgroundElements[i][j]);
                number_field.addView(elements[i][j]);
            }
        }
        last_elements = deepCopy(elements);
        if (undo) {
            for (int i = 0; i < elements.length; i++) {
                for (int j = 0; j < elements[i].length; j++) {
                    last_elements[i][j].setNumber(gameState.getLastNumber(i, j));
                }
            }
        }
        if (newGame) {
            moved = true;
            addNumber();
            moved = true;
            addNumber();
            newGame = false;
        }
    }

    public void switchElementPositions(Element e1, Element e2) {
        int i = e1.getdPosX();
        int j = e1.getdPosY();

        e1.animateMoving = true;
        e1.setDPosition(e2.getdPosX(), e2.getdPosY());
        e2.animateMoving = false;
        e2.setDPosition(i, j);

    }

    public Element[][] deepCopy(Element[][] e) {
        Element[][] r = new Element[e.length][];
        for (int i = 0; i < r.length; i++) {
            r[i] = new Element[e[i].length];
            for (int j = 0; j < r[i].length; j++) {
                r[i][j] = e[i][j].copy();
            }
        }
        return r;
    }

    private boolean handleSwipeTop() {
        Element[][] temp = deepCopy(elements);
        int temp_points = points;
        moved = false;
        Element s = new Element(myActivity);

        for (int i = 0; i < elements.length; i++) {
            s.number = elements[0][i].number;
            s.posX = 0;
            s.posY = i;


            for (int j = 1; j < elements[i].length; j++) {
                if (elements[j][i].number != 0 && (s.number == 0 || s.number == elements[j][i].number)) {
                    moved = true;
                    elements[j][i].setNumber(s.number + elements[j][i].number);
                    elements[s.posX][s.posY].setNumber(0);
                    switchElementPositions(elements[j][i], elements[s.posX][s.posY]);
                    Element z = elements[j][i];
                    elements[j][i] = elements[s.posX][s.posY];
                    elements[s.posX][s.posY] = z;
                    if (s.number != 0)
                        points += elements[s.posX][s.posY].number;
                    if (s.number != 0)
                        s.posX++;
                    j = s.posX;
                    s.number = elements[j][i].number;

                } else if (elements[j][i].number != 0) {
                    s.number = elements[j][i].number;
                    s.posX = j;
                    s.posY = i;
                }
            }

        }
        for (int i = 0; i < elements.length; i++) {
            s.number = elements[0][i].number;
            s.posX = 0;
            s.posY = i;


            for (int j = 1; j < elements[i].length; j++) {
                if (elements[j][i].number != 0 && s.number == 0) {
                    moved = true;
                    elements[j][i].setNumber(s.number + elements[j][i].number);
                    elements[s.posX][s.posY].setNumber(0);
                    switchElementPositions(elements[j][i], elements[s.posX][s.posY]);
                    Element z = elements[j][i];
                    elements[j][i] = elements[s.posX][s.posY];
                    elements[s.posX][s.posY] = z;
                    if (s.number != 0)
                        s.posX++;
                    j = s.posX;
                    s.number = elements[j][i].number;

                } else if (s.number != 0) {
                    s.number = elements[j][i].number;
                    s.posX = j;
                    s.posY = i;
                }
            }

        }
        if (moved) {
            gameStatistics.addMoves(1);
            last_points = temp_points;
            last_elements = temp;
            setUndoVisible(true);
            undo = true;
        }
        if (moved)
            gameStatistics.moveT();
        addNumber();
        setDPositions(animationActivated);
        updateGameState();
        //es wurde nach oben gewischt, hier den Code einfügen
        return false;
    }

    private boolean handleSwipeRight() {
        Element[][] temp = deepCopy(elements);
        int temp_points = points;
        moved = false;
        Element s = new Element(myActivity);
        for (int i = 0; i < elements.length; i++) {
            s.number = elements[i][elements[i].length - 1].number;
            s.posX = i;
            s.posY = elements[i].length - 1;


            for (int j = elements[i].length - 2; j >= 0; j--) {
                if (elements[i][j].number != 0 && (s.number == 0 || s.number == elements[i][j].number)) {
                    moved = true;

                    elements[i][j].setNumber(s.number + elements[i][j].number);
                    elements[s.posX][s.posY].setNumber(0);
                    switchElementPositions(elements[i][j], elements[s.posX][s.posY]);
                    Element z = elements[i][j];
                    elements[i][j] = elements[s.posX][s.posY];
                    elements[s.posX][s.posY] = z;

                    if (s.number != 0)
                        points += elements[s.posX][s.posY].number;
                    if (s.number != 0)
                        s.posY--;
                    j = s.posY;
                    s.number = elements[i][j].number;
                } else if (elements[i][j].number != 0) {
                    s.number = elements[i][j].number;
                    s.posX = i;
                    s.posY = j;
                }
            }

        }
        for (int i = 0; i < elements.length; i++) {
            s.number = elements[i][elements[i].length - 1].number;
            s.posX = i;
            s.posY = elements[i].length - 1;


            for (int j = elements[i].length - 2; j >= 0; j--) {
                if (elements[i][j].number != 0 && s.number == 0) {
                    moved = true;

                    elements[i][j].setNumber(s.number + elements[i][j].number);
                    elements[s.posX][s.posY].setNumber(0);
                    switchElementPositions(elements[i][j], elements[s.posX][s.posY]);
                    Element z = elements[i][j];
                    elements[i][j] = elements[s.posX][s.posY];
                    elements[s.posX][s.posY] = z;


                    if (s.number != 0)
                        s.posY--;
                    j = s.posY;
                    s.number = elements[i][j].number;
                } else if (s.number != 0) {
                    s.number = elements[i][j].number;
                    s.posX = i;
                    s.posY = j;
                }
            }

        }
        if (moved) {
            gameStatistics.addMoves(1);
            last_points = temp_points;
            last_elements = temp;
            setUndoVisible(true);
            undo = true;
        }
        if (moved)
            gameStatistics.moveR();
        addNumber();
        setDPositions(animationActivated);
        updateGameState();

        //es wurde nach rechts gewischt, hier den Code einfügen
        return false;
    }

    private boolean handleSwipeLeft() {
        Element[][] temp = deepCopy(elements);
        int temp_points = points;
        moved = false;
        Element s = new Element(myActivity);
        for (int i = 0; i < elements.length; i++) {
            s.number = elements[i][0].number;
            s.posX = i;
            s.posY = 0;


            for (int j = 1; j < elements[i].length; j++) {
                if (elements[i][j].number != 0 && (s.number == 0 || s.number == elements[i][j].number)) {
                    moved = true;


                    elements[i][j].setNumber(s.number + elements[i][j].number);
                    elements[s.posX][s.posY].setNumber(0);
                    switchElementPositions(elements[i][j], elements[s.posX][s.posY]);
                    Element z = elements[i][j];
                    elements[i][j] = elements[s.posX][s.posY];
                    elements[s.posX][s.posY] = z;

                    if (s.number != 0)
                        points += elements[s.posX][s.posY].number;
                    if (s.number != 0)
                        s.posY++;
                    j = s.posY;
                    s.number = elements[i][j].number;
                } else if (elements[i][j].number != 0) {
                    s.number = elements[i][j].number;
                    s.posX = i;
                    s.posY = j;
                }
            }

        }
        for (int i = 0; i < elements.length; i++) {
            s.number = elements[i][0].number;
            s.posX = i;
            s.posY = 0;

            for (int j = 1; j < elements[i].length; j++) {
                if (elements[i][j].number != 0 && s.number == 0) {
                    moved = true;

                    elements[i][j].setNumber(s.number + elements[i][j].number);
                    elements[s.posX][s.posY].setNumber(0);
                    switchElementPositions(elements[i][j], elements[s.posX][s.posY]);
                    Element z = elements[i][j];
                    elements[i][j] = elements[s.posX][s.posY];
                    elements[s.posX][s.posY] = z;

                    if (s.number != 0)
                        s.posY++;
                    j = s.posY;
                    s.number = elements[i][j].number;
                } else if (s.number != 0) {
                    s.number = elements[i][j].number;
                    s.posX = i;
                    s.posY = j;
                }
            }

        }
        if (moved) {
            gameStatistics.addMoves(1);
            last_points = temp_points;
            last_elements = temp;
            setUndoVisible(true);
            undo = true;
        }
        if (moved)
            gameStatistics.moveL();
        addNumber();
        setDPositions(animationActivated);
        updateGameState();
        //es wurde nach links gewischt, hier den Code einfügen
        return false;
    }

    private boolean handleSwipeBottom() {
        Element[][] temp = deepCopy(elements);
        int temp_points = points;
        moved = false;
        Element s = new Element(myActivity);
        for (int i = 0; i < elements.length; i++) {
            s.number = elements[elements[i].length - 1][i].number;
            s.posX = elements[i].length - 1;
            s.posY = i;


            for (int j = elements[i].length - 2; j >= 0; j--) {
                if (elements[j][i].number != 0 && (s.number == 0 || s.number == elements[j][i].number)) {
                    moved = true;

                    elements[j][i].setNumber(s.number + elements[j][i].number);
                    elements[s.posX][s.posY].setNumber(0);
                    switchElementPositions(elements[j][i], elements[s.posX][s.posY]);
                    Element z = elements[j][i];
                    elements[j][i] = elements[s.posX][s.posY];
                    elements[s.posX][s.posY] = z;

                    if (s.number != 0)
                        points += elements[s.posX][s.posY].number;
                    if (s.number != 0)
                        s.posX--;
                    j = s.posX;
                    s.number = elements[j][i].number;
                } else if (elements[j][i].number != 0) {
                    s.number = elements[j][i].number;
                    s.posX = j;
                    s.posY = i;
                }
            }

        }
        for (int i = 0; i < elements.length; i++) {
            s.number = elements[elements[i].length - 1][i].number;
            s.posX = elements[i].length - 1;
            s.posY = i;


            for (int j = elements[i].length - 2; j >= 0; j--) {
                if (elements[j][i].number != 0 && s.number == 0) {
                    moved = true;

                    elements[j][i].setNumber(s.number + elements[j][i].number);
                    elements[s.posX][s.posY].setNumber(0);
                    switchElementPositions(elements[j][i], elements[s.posX][s.posY]);
                    Element z = elements[j][i];
                    elements[j][i] = elements[s.posX][s.posY];
                    elements[s.posX][s.posY] = z;

                    if (s.number != 0)
                        s.posX--;
                    j = s.posX;
                    s.number = elements[j][i].number;
                } else if (s.number != 0) {
                    s.number = elements[j][i].number;
                    s.posX = j;
                    s.posY = i;
                }
            }

        }
        if (moved) {
            gameStatistics.addMoves(1);
            last_points = temp_points;
            last_elements = temp;
            setUndoVisible(true);
            undo = true;
        }
        if (moved)
            gameStatistics.moveD();
        addNumber();
        setDPositions(animationActivated);
        updateGameState();
        //es wurde nach unten gewischt, hier den Code einfügen
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                handleSwipeTop();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                handleSwipeRight();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                handleSwipeLeft();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                handleSwipeBottom();
                return true;
            case KeyEvent.KEYCODE_Z:
                if (event.isCtrlPressed()) {
                    handleUndo();
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setListener() {
        swipeListener = new Gestures(this) {
            public boolean onSwipeTop() {
                return handleSwipeTop();
            }

            public boolean onSwipeRight() {
                return handleSwipeRight();
            }

            public boolean onSwipeLeft() {
                return handleSwipeLeft();
            }

            public boolean onSwipeBottom() {
                return handleSwipeBottom();
            }

            public boolean nichts() {
                //es wurde keine wischrichtung erkannt, hier den Code einfügen
                return false;
            }
        };
        touch_field.setOnTouchListener(swipeListener);
        number_field.setOnTouchListener(swipeListener);
        for (int i = 0; i < elements.length; i++) {
            for (int j = 0; j < elements[i].length; j++) {
                elements[i][j].setOnTouchListener(swipeListener);
                backgroundElements[i][j].setOnTouchListener(swipeListener);
            }
        }
    }

    public String display(Element[][] e) {
        String result = "\n";
        for (int i = 0; i < e.length; i++) {
            for (int j = 0; j < e[i].length; j++)
                result = result + " " + elements[i][j].number; //+ " "+elements[i][j];
            result = result + "\n";
        }
        result += "\n";
        for (int i = 0; i < e.length; i++) {
            for (int j = 0; j < e[i].length; j++)
                result = result + " (" + elements[i][j].getX() + " , " + elements[i][j].getY() + ")" + " v:" + elements[i][j].getVisibility();//+" "+elements[i][j];
            result = result + "\n";
        }
        return result;
    }

    public void updateHighestNumber() {
        for (int i = 0; i < elements.length; i++) {
            for (int j = 0; j < elements[i].length; j++) {
                if (highestNumber < elements[i][j].number) {
                    highestNumber = elements[i][j].number;
                    gameStatistics.setHighestNumber(highestNumber);
                }
            }
        }
    }

    public void check2048() {
        if (won2048 == false) {
            for (int i = 0; i < elements.length; i++) {
                for (int j = 0; j < elements[i].length; j++) {
                    if (elements[i][j].number == WINTHRESHOLD) {

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
                        won2048 = true;
                    }
                }
            }
        }
    }


    public void setDPositions(boolean animation) {
        for (Element[] i : elements) {
            for (Element j : i) {
                boolean offX = j.dPosX != j.getX();
                boolean offY = j.dPosY != j.getY();
                if (!offX && !offY)
                    continue;
                if (!animation) {
                    TileAnimations.settle(j);
                    ViewGroup.MarginLayoutParams lp1 = (ViewGroup.MarginLayoutParams) j.getLayoutParams();
                    if (offX)
                        lp1.leftMargin = j.dPosX;
                    if (offY)
                        lp1.topMargin = j.dPosY;
                    j.setLayoutParams(lp1);
                    j.drawItem();
                } else if (j.animateMoving) {
                    // a changed number means this tile absorbs another one when it lands
                    TileAnimations.slide(j, j.number != j.dNumber, mergeFeedback);
                } else {
                    // emptied / absorbed tiles stay put until the sliding tiles have arrived
                    TileAnimations.jumpAfterSlide(j);
                }
            }
        }
    }

    /** A light tick when tiles merge, at most once per move. Respects the system haptics setting. */
    private final Runnable mergeFeedback = new Runnable() {
        @Override
        public void run() {
            long now = SystemClock.uptimeMillis();
            if (now - lastMergeHaptic < 80)
                return;
            lastMergeHaptic = now;
            if (number_field != null)
                number_field.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        }
    };

    /** Restart / undo buttons sink a little under the finger and spring back on release. */
    private final View.OnTouchListener pressFeedback = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.86f).scaleY(0.86f).setDuration(120).setStartDelay(0)
                            .setInterpolator(EASE_OUT).setListener(null).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(420).setStartDelay(0)
                            .setInterpolator(PRESS_RELEASE).setListener(null).start();
                    break;
            }
            return false; // let the click go through
        }
    };

    /** Fades and scales the undo button in or out instead of popping its visibility. */
    private void setUndoVisible(boolean visible) {
        if (undoButton == null)
            return;
        if (!animationActivated) {
            undoButton.animate().cancel();
            undoButton.setAlpha(1f);
            undoButton.setScaleX(1f);
            undoButton.setScaleY(1f);
            undoButton.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            undoShown = visible;
            return;
        }
        if (visible == undoShown && (!visible || undoButton.getVisibility() == View.VISIBLE))
            return;
        undoShown = visible;
        if (visible) {
            if (undoButton.getVisibility() != View.VISIBLE) {
                undoButton.setAlpha(0f);
                undoButton.setScaleX(0.6f);
                undoButton.setScaleY(0.6f);
                undoButton.setVisibility(View.VISIBLE);
            }
            undoButton.animate().alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(0).setDuration(320)
                    .setInterpolator(EASE_OUT).setListener(null).start();
        } else {
            undoButton.animate().alpha(0f).scaleX(0.6f).scaleY(0.6f).setStartDelay(0).setDuration(180)
                    .setInterpolator(EASE_IN).setListener(null)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            if (!undoShown) {
                                undoButton.setVisibility(View.INVISIBLE);
                                undoButton.setScaleX(1f);
                                undoButton.setScaleY(1f);
                            }
                        }
                    }).start();
        }
    }

    /**
     * Shows a score. When animated, the number counts up with an ease-out curve and gives a small
     * springy nudge, like the score labels in iOS games.
     */
    private void setCounter(final TextView view, final long value, boolean animate) {
        if (view == null)
            return;
        Object running = view.getTag();
        if (running instanceof Animator)
            ((Animator) running).cancel();
        view.setTag(null);

        long shown;
        try {
            shown = Long.parseLong(view.getText().toString());
        } catch (NumberFormatException e) {
            shown = value;
        }
        if (!animate || !animationActivated || shown >= value) {
            view.setText(String.valueOf(value));
            return;
        }

        final long from = shown;
        ValueAnimator count = ValueAnimator.ofFloat(0f, 1f);
        count.setDuration(380);
        count.setInterpolator(EASE_OUT);
        count.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                view.setText(String.valueOf(from + Math.round((value - from) * (double) f)));
            }
        });
        count.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setText(String.valueOf(value));
                if (view.getTag() == animation)
                    view.setTag(null);
            }
        });
        view.setTag(count);
        count.start();

        ObjectAnimator nudge = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.12f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.12f));
        nudge.setDuration(300);
        nudge.setInterpolator(SCORE_POP);
        nudge.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setScaleX(1f);
                view.setScaleY(1f);
            }
        });
        nudge.start();
    }

    public void addNumber() {

        if (points > record) {
            record = points;
            gameStatistics.setRecord(record);
            setCounter(textFieldRecord, record, true);
        }
        if (moved) {
            gameOver = false;
            moved = false;
            setCounter(textFieldPoints, points, true);
            Element[] empty_fields = new Element[n * n];
            int counter = 0;
            for (int i = 0; i < elements.length; i++) {
                for (int j = 0; j < elements[i].length; j++) {
                    if (elements[i][j].number == 0) {
                        empty_fields[counter++] = elements[i][j];
                    }
                }
            }
            if (counter > 0) {
                int index = (int) (Math.random() * counter);
                int number = 2;
                if (Math.random() > PROPABILITYFORTWO)
                    number = 4;

                empty_fields[index].setNumber(number);
                empty_fields[index].drawItem();
                if (animationActivated) {
                    TileAnimations.spawn(empty_fields[index], SPAWNDELAY);
                }
                if (counter == 1) {
                    gameOver = true;
                    for (int i = 0; i < elements.length; i++) {
                        for (int j = 0; j < elements[i].length; j++) {
                            if ((i + 1 < elements.length && elements[i][j].number == elements[i + 1][j].number) || (j + 1 < elements[i].length && elements[i][j].number == elements[i][j + 1].number)) {
                                gameOver = false;
                            }
                        }
                    }
                }
            }
            updateGameState();

            if (gameOver) {
                gameOver();
            }
        }
        Log.i("number of elements", "" + number_field.getChildCount() + ", " + number_field_background.getChildCount());
    }

    public void gameOver() {
        Log.i("record", "" + record + ", " + gameStatistics.getRecord());
        saveStatisticsToFile(gameStatistics);
        new AlertDialog.Builder(this)
                .setTitle((this.getResources().getString(R.string.Titel_L_Message, points)))
                .setMessage(this.getResources().getString(R.string.Lost_Message, points))
                .setNegativeButton((this.getResources().getString(R.string.No_Message)), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createNewGame = true;
                        getIntent().putExtra("new", true);
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
        Log.i("record", "danach");
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
        } else if (id == android.R.id.home) {
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

    public void saveStateToFile(GameState nS) {
        if (saveState) {
            try {
                if (filename == null)
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
    }

    public boolean deleteStateFile() {
        try {
            if (filename == null)
                filename = "state" + n + ".txt";
            File directory = getFilesDir();
            File f = new File(directory, filename);
            return f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public GameState readStateFromFile() {
        GameState nS = new GameState(n);
        try {
            File file = new File(getFilesDir(), filename);
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            nS = (GameState) in.readObject();
            boolean emptyField = true;
            for (int i = 0; i < nS.numbers.length; i++) {
                if (nS.numbers[i] > 0) {
                    emptyField = false;
                    break;
                }
            }
            if (emptyField || nS.n != n) {
                nS = new GameState(n);
                newGame = true;
            }
            in.close();
            fileIn.close();
        } catch (Exception e) {
            newGame = true;
            e.printStackTrace();
        }
        return nS;
    }

    public GameStatistics readStatisticsFromFile() {
        GameStatistics gS = new GameStatistics(n);
        try {
            File file = new File(getFilesDir(), "statistics" + n + ".txt");
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            gS = (GameStatistics) in.readObject();
            in.close();
            fileIn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gS;
    }

    public void saveStatisticsToFile(GameStatistics gS) {
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
