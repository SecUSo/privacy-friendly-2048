package org.secuso.privacyfriendly2048.activities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.secuso.privacyfriendly2048.activities.helper.GameStatistics;
import org.secuso.privacyfriendly2048.activities.helper.GameState;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.io.File;
import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class GameActivityTest {

    private GameStatistics gameStatisticsMock;
    private GameState gameStateMock;

    @Before
    public void setUp() {
       gameStatisticsMock = mock(GameStatistics.class);
       gameStateMock = mock(GameState.class);
    }

    @Test
    public void testStatsGetFilenameInteraction() {

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class)) {
            controller.setup();
            GameActivity activity = spy(controller.get());
            when(gameStatisticsMock.getFilename()).thenReturn("file");
            activity.saveStatisticsToFile(gameStatisticsMock);

            verify(gameStatisticsMock, times(1)).getFilename();
            activity.getFilesDir().delete();
        }
    }

//    @Test
//    public void testStatsSetListenerInteraction() {
//
//        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class)) {
//            controller.setup();
//            GameActivity activity = spy(controller.get());
//
//        }
//    }

    @Test
    public void testGameStatsSetHighestNumberInteractions() {

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class)) {
            controller.setup();
            GameActivity activity = spy(controller.get());
            activity.gameStatistics = gameStatisticsMock;
            activity.highestNumber = -1;
            activity.initialize();
            activity.updateHighestNumber();

            verify(gameStatisticsMock, times(1)).setHighestNumber(any(Long.class));
        }
    }


    @Test
    public void testGameStatsGameOverInteractions() {
        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class)) {
            controller.setup();
            GameActivity activity = spy(controller.get());
            when(gameStatisticsMock.getRecord()).thenReturn(500L);
            when(gameStatisticsMock.getFilename()).thenReturn("file");
            activity.gameStatistics = gameStatisticsMock;
            activity.gameOver();

            InOrder inorder = inOrder(gameStatisticsMock);

            inorder.verify(gameStatisticsMock, times(1)).getRecord();
            inorder.verify(gameStatisticsMock, times(1)).getFilename();
            activity.getFilesDir().delete();
        }
    }

    @Test
    public void testGameStatsInitializeInteractions() {
        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class)) {
            controller.setup();
            GameActivity activity = spy(controller.get());
            doReturn(gameStatisticsMock).when(activity).readStatisticsFromFile();
            activity.initialize();
            verify(gameStatisticsMock, times(1)).getRecord();
        }
    }

    @Test
    public void testGameStateInitializeInteractions() {
        Intent intent = new Intent();
        intent.putExtra("new", false);
        intent.putExtra("n", 5);
        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = spy(controller.get());

            doReturn(gameStatisticsMock).when(activity).readStatisticsFromFile();
            when(gameStatisticsMock.getRecord()).thenReturn(25L);

            gameStateMock.points = 10;
            gameStateMock.last_points = 5;
            doReturn(gameStateMock).when(activity).readStateFromFile();

            activity.initialize();

            verify(gameStateMock, atLeast(1)).getNumber(any(int.class), any(int.class));
            verify(gameStateMock, never()).getLastNumber(any(int.class), any(int.class));

            activity.getFilesDir().delete();
        }
    }

    @Test
    public void testGameStateInitializeInteractionsWithUndoTrue() {
        Intent intent = new Intent();
        intent.putExtra("new", false);
        intent.putExtra("undo", true);
        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = spy(controller.get());

            doReturn(gameStatisticsMock).when(activity).readStatisticsFromFile();
            when(gameStatisticsMock.getRecord()).thenReturn(25L);

            gameStateMock.points = 10;
            gameStateMock.last_points = 5;
            doReturn(gameStateMock).when(activity).readStateFromFile();

            activity.initialize();
            verify(gameStateMock, atLeast(1)).getNumber(any(int.class), any(int.class));
            verify(gameStateMock, atLeast(1)).getLastNumber(any(int.class), any(int.class));

            activity.getFilesDir().delete();
        }
    }


    @Test
    public void testUpdateHighestNumber() {
        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class)) {
            controller.setup();
            GameActivity activity = spy(controller.get());
            doReturn(gameStatisticsMock).when(activity).readStatisticsFromFile();
            activity.highestNumber = -1;
            activity.initialize();
            activity.updateHighestNumber();
            assertTrue(activity.highestNumber > -1);
        }
    }


    @Test // calling on empty file should not crash the game
    public void readStatisticsFromFileEmpty() {
        int n = 2;
        Intent intent = new Intent();
        intent.putExtra("n", n);
        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent).setup()) {
            GameActivity activity = spy(controller.get());
            activity.initializeState();

            File file = new File(activity.getFilesDir(), "statistics" + n + ".txt");
            try {
                file.createNewFile();
                GameStatistics gs1 = activity.readStatisticsFromFile();
                GameStatistics gs2 = new GameStatistics(n);
                assertTrue(compareStatistics(gs1, gs2));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            activity.getFilesDir().delete();

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