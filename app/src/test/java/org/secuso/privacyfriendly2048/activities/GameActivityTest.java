package org.secuso.privacyfriendly2048.activities;

import static android.app.PendingIntent.getActivity;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.secuso.privacyfriendly2048.activities.helper.GameStatistics;
import org.secuso.privacyfriendly2048.activities.helper.GameState;

import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.io.File;
import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class GameActivityTest {

    private GameStatistics gameStatisticsMock;

    @Before
    public void setUp() {
       gameStatisticsMock = mock(GameStatistics.class);
    }

    @Test
    public void testStatsGetFilenameInteraction() {
        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(GameActivity.class)) {
            scenario.onActivity(
                    activity -> {
                        scenario.moveToState(Lifecycle.State.RESUMED);
                        assertThat(scenario.getState()).isEqualTo(Lifecycle.State.RESUMED);

                        when(gameStatisticsMock.getFilename()).thenReturn("file");
                        activity.saveStatisticsToFile(gameStatisticsMock);
                        verify(gameStatisticsMock, times(1)).getFilename();
                        activity.getFilesDir().delete();
                    });

        }
    }

    @Test
    public void testGameStatsSetHighestNumberInteractions() {
        Intent startActivityIntent =
                new Intent(ApplicationProvider.getApplicationContext(), GameActivity.class)
                        .putExtra("n", 2);
        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(startActivityIntent)) {
            scenario.onActivity(
                    activity -> {
                        scenario.moveToState(Lifecycle.State.STARTED);
                        assertThat(scenario.getState()).isEqualTo(Lifecycle.State.STARTED);

                        activity.highestNumber = -1;
                        activity.gameStatistics = gameStatisticsMock;
                        activity.updateHighestNumber();
                        int highest = activity.highestNumber;
                        verify(gameStatisticsMock, atLeast(1)).setHighestNumber(highest);
                        activity.getFilesDir().delete();
                    });
        }
    }

    @Test
    public void testUpdateHighestNumber() throws Exception {
        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(GameActivity.class)) {
            scenario.onActivity(
                    activity -> {
                        activity.highestNumber = -1;
                        activity.updateHighestNumber();
                        assertTrue(activity.highestNumber > -1);
                        activity.getFilesDir().delete();
                    });

        }
    }

    @Test // calling on empty file should not crash the game
    public void readStatisticsFromFileEmpty() {
        Intent startActivityIntent =
                new Intent(ApplicationProvider.getApplicationContext(), GameActivity.class)
                        .putExtra("n", 1);
        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(startActivityIntent)) {
            scenario.onActivity(
                    activity -> {
                        File file = new File(activity.getFilesDir(), "statistics1.txt");
                        try {
                            file.createNewFile();
                            GameStatistics gs1 = activity.readStatisticsFromFile();
                            GameStatistics gs2 = new GameStatistics(1);
                            assertTrue(compareStatistics(gs1, gs2));

                            activity.getFilesDir().delete();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

        }
    }

    @Test // calling on nonexistent file should not crash the game
    public void readStatisticsFromFileOnNonexistentFile() {
        Intent startActivityIntent =
                new Intent(ApplicationProvider.getApplicationContext(), GameActivity.class)
                        .putExtra("n", 1);
        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(startActivityIntent)) {
            scenario.onActivity(
                    activity -> {

                        GameStatistics gs1 = activity.readStatisticsFromFile();
                        GameStatistics gs2 = new GameStatistics(1);
                        assertTrue(compareStatistics(gs1, gs2));

                        activity.getFilesDir().delete();

                    });

        }
    }

    @Test // writes to file
    public void saveStatisticsToFile() {
        int n = 2;
        Intent startActivityIntent =
                new Intent(ApplicationProvider.getApplicationContext(), GameActivity.class)
                        .putExtra("n", n);

        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(startActivityIntent)) {
            scenario.onActivity(
                    activity -> {

                        GameStatistics gs1 = new GameStatistics(n);
                        gs1.setHighestNumber(25);
                        gs1.setRecord(100);

                        activity.saveStatisticsToFile(gs1);

                        File file = new File(activity.getFilesDir(), gs1.getFilename());
                        assertTrue(file.exists());

                        activity.getFilesDir().delete();
                    });

        }
    }

    @Test(expected = NullPointerException.class)
    public void saveStatisticsToFileNullStats() {
        int n = 2;
        Intent startActivityIntent =
                new Intent(ApplicationProvider.getApplicationContext(), GameActivity.class)
                        .putExtra("n", n);

        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(startActivityIntent)) {
            scenario.onActivity(
                    activity -> {

                        activity.saveStatisticsToFile(null);
                        activity.getFilesDir().delete();
                    });

        }
    }

    @Test
    public void integrationTestOnSaveStatisticsToFileAndReadStatisticsFromFile() {
        int n = 2;
        Intent startActivityIntent =
                new Intent(ApplicationProvider.getApplicationContext(), GameActivity.class)
                        .putExtra("n", n);
        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(startActivityIntent)) {
            scenario.onActivity(
                    activity -> {

                        GameStatistics gs1 = new GameStatistics(n);
                        gs1.setHighestNumber(25);
                        gs1.setRecord(100);

                        activity.saveStatisticsToFile(gs1);
                        GameStatistics gs2 = activity.readStatisticsFromFile();
                        assertTrue(compareStatistics(gs1, gs2));

                        activity.getFilesDir().delete();
                    });

        }
    }

    @Test // writes to file
    public void saveStateToFile() {
        int n = 2;
        String filename = "stateFile.txt";
        Intent startActivityIntent =
                new Intent(ApplicationProvider.getApplicationContext(), GameActivity.class)
                        .putExtra("n", n)
                        .putExtra("filename", filename);

        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(startActivityIntent)) {
            scenario.onActivity(
                    activity -> {

                        GameState ms = new GameState(n);
                        activity.saveStateToFile(ms);

                        File file = new File(activity.getFilesDir(), filename);
                        assertTrue(file.exists());

                        activity.getFilesDir().delete();
                    });

        }
    }

    @Test // writes to file
    public void saveStateToFileNullState() {
        int n = 2;
        String filename = "stateFile.txt";
        Intent startActivityIntent =
                new Intent(ApplicationProvider.getApplicationContext(), GameActivity.class)
                        .putExtra("n", n)
                        .putExtra("filename", filename);

        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(startActivityIntent)) {
            scenario.onActivity(
                    activity -> {
                        activity.saveStateToFile(null);

                        File file = new File(activity.getFilesDir(), filename);
                        assertTrue(file.exists());

                        activity.getFilesDir().delete();
                    });

        }
    }

    @Test // reading state from empty file should not crash the game
    public void readStateFromFileNonExistent() {
        int n = 2;
        String filename = "stateFile.txt";
        Intent startActivityIntent =
                new Intent(ApplicationProvider.getApplicationContext(), GameActivity.class)
                        .putExtra("n", n)
                        .putExtra("filename", filename);

        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(startActivityIntent)) {
            scenario.onActivity(
                    activity -> {

                        GameState ms1 = new GameState(n);
                        GameState ms2 = activity.readStateFromFile();
                        assertTrue(compareStates(ms1, ms2));

                        activity.getFilesDir().delete();
                    });

        }
    }

    @Test // reading state from empty file should not crash the game
    public void readStateFromFileEmpty() {
        int n = 2;
        String filename = "stateFile.txt";
        Intent startActivityIntent =
                new Intent(ApplicationProvider.getApplicationContext(), GameActivity.class)
                        .putExtra("n", n)
                        .putExtra("filename", filename);

        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(startActivityIntent)) {
            scenario.onActivity(
                    activity -> {
                        File file = new File(activity.getFilesDir(), filename);
                        try {
                            file.createNewFile();
                            GameState ms1 = new GameState(n);
                            GameState ms2 = activity.readStateFromFile();
                            assertTrue(compareStates(ms1, ms2));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        activity.getFilesDir().delete();
                    });

        }
    }

    @Test
    public void integrationTestOnSaveStateToFileAndReadStateFromFile() {
        int n = 2;
        Intent startActivityIntent =
                new Intent(ApplicationProvider.getApplicationContext(), GameActivity.class)
                        .putExtra("n", n);
        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(startActivityIntent)) {
            scenario.onActivity(
                    activity -> {

                        GameState ms1 = new GameState(n);
                        ms1.points = 30;
                        ms1.n = n;
                        ms1.undo = true;
                        ms1.numbers = new int[]{1,2};

                        activity.saveStateToFile(ms1);
                        GameState ms2 = activity.readStateFromFile();
                        assertTrue(compareStates(ms1, ms2));

                        activity.getFilesDir().delete();
                    });

        }
    }

    @Test // reading file with null object should not crash game
    public void integrationTestOnSaveStateToFileAndReadStateFromFileWithNullState() {
        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(GameActivity.class)) {
            scenario.onActivity(
                    activity -> {

                        activity.saveStateToFile(null);
                        GameState ms1 = new GameState(4);
                        GameState ms2 = activity.readStateFromFile();
                        assertTrue(compareStates(ms1, ms2));

                        activity.getFilesDir().delete();
                    });

        }
    }

    @Test
    public void deleteStateFileNonexistent() {
        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(GameActivity.class)) {
            scenario.onActivity(
                    activity -> {

                        assertFalse(activity.deleteStateFile());
                        activity.getFilesDir().delete();
                    });

        }
    }
    @Test
    public void deleteStateFileExist() {
        String filename = "stateFile.txt";
        Intent startActivityIntent =
                new Intent(ApplicationProvider.getApplicationContext(), GameActivity.class)
                        .putExtra("filename", filename);
        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(startActivityIntent)) {
            scenario.onActivity(
                    activity -> {

                        File file = new File(activity.getFilesDir(), filename);
                        try {
                            file.createNewFile();
                            assertTrue(activity.deleteStateFile());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        activity.getFilesDir().delete();
                    });

        }
    }

    @Test
    public void integrationTestSaveStateToFileAndDeleteStateFile () {
        int n = 2;
        String filename = "stateFile.txt";
        Intent startActivityIntent =
                new Intent(ApplicationProvider.getApplicationContext(), GameActivity.class)
                        .putExtra("n", n)
                        .putExtra("filename", filename);

        try (ActivityScenario<GameActivity> scenario = ActivityScenario.launch(startActivityIntent)) {
            scenario.onActivity(
                    activity -> {

                        GameState ms = new GameState(n);
                        activity.saveStateToFile(ms);
                        assertTrue(activity.deleteStateFile());

                        activity.getFilesDir().delete();
                    });

        }
    }


    private boolean compareStatistics(GameStatistics gs1, GameStatistics gs2) {
        return gs1.getFilename().equals(gs2.getFilename())&&
                gs1.toString().equals(gs2.toString());
    }

    private boolean compareStates(GameState gs1, GameState gs2) {
        return gs1.toString().equals(gs2.toString());
    }
}