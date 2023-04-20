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
import java.io.FileOutputStream;
import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class GameActivityTest {

    @Test // calling readStatisticsFromFile on empty file should return a new GameStatistics object
    public void readStatisticsFromFileEmpty() {
        int n = 2;
        String filename = "stateFile.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent).setup()) {
            GameActivity activity = controller.get();
            activity.initializeState();

            try {
                File file = new File(activity.getFilesDir(), "statistics" + n + ".txt");
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

    @Test // calling readStatisticsFromFile on nonexistent file should return a new GameStatistics object
    public void readStatisticsFromFileOnNonexistentFile() {
        int n = 2;
        String filename = "stateFile.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = controller.get();
            activity.initializeState();

            GameStatistics gs1 = activity.readStatisticsFromFile();
            GameStatistics gs2 = new GameStatistics(n);
            assertTrue(compareStatistics(gs1, gs2));

            activity.getFilesDir().delete();

        }
    }

    @Test // calling readStatisticsFromFile on corrupted file should return new GameStatistics object
    public void readStatisticsFromFileOnCorruptedFile() {
        int n = 2;
        String filename = "stateFile.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = (controller.get());
            activity.initializeState();

            try {
                File file = new File(activity.getFilesDir(), "statistics" + n + ".txt");
                file.createNewFile();

                String str = "Hello";
                FileOutputStream outputStream = new FileOutputStream(file.getAbsolutePath());
                byte[] strToBytes = str.getBytes();
                outputStream.write(strToBytes);
                outputStream.close();

                GameStatistics gs1 = activity.readStatisticsFromFile();
                GameStatistics gs2 = new GameStatistics(n);
                assertTrue(compareStatistics(gs1, gs2));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            activity.getFilesDir().delete();
        }
    }

    @Test // writes stats to file and file exists
    public void saveStatisticsToFile() {
        int n = 2;
        String filename = "stateFile.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = controller.get();
            activity.initializeState();

            GameStatistics gs1 = new GameStatistics(n);
            gs1.setHighestNumber(25);
            gs1.setRecord(100);

            activity.saveStatisticsToFile(gs1);

            File file = new File(activity.getFilesDir(), gs1.getFilename());
            assertTrue(file.exists());

            activity.getFilesDir().delete();
        }
    }

    @Test(expected = NullPointerException.class)
    // saving null object throws NullPointerException exception
    public void saveStatisticsToFileNullStats() {
        int n = 2;
        String filename = "stateFile.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = controller.get();
            activity.initializeState();

            activity.saveStatisticsToFile(null);
            activity.getFilesDir().delete();

        }
    }

    @Test // calling saveStatisticsToFile on a GameStatistics object then calling readStatisticsFromFile should return same object
    public void integrationTestOnSaveStatisticsToFileAndReadStatisticsFromFile() {
        int n = 2;
        String filename = "stateFile.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = controller.get();
            activity.initializeState();

            GameStatistics gs1 = new GameStatistics(n);
            gs1.setHighestNumber(25);
            gs1.setRecord(100);

            activity.saveStatisticsToFile(gs1);
            GameStatistics gs2 = activity.readStatisticsFromFile();
            assertTrue(compareStatistics(gs1, gs2));

            activity.getFilesDir().delete();
        }
    }


    @Test // reading state from nonexistent file should return new GameState object
    public void readStateFromFileNonExistent() {
        int n = 2;
        String filename = "stateFile.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = (controller.get());
            activity.initializeState();

            GameState ms1 = new GameState(n);
            GameState ms2 = activity.readStateFromFile();
            assertTrue(compareStates(ms1, ms2));

            activity.getFilesDir().delete();
        }
    }

    @Test // reading state from empty file should return new GameState object
    public void readStateFromFileEmpty() {
        int n = 2;
        String filename = "stateFile.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = (controller.get());
            activity.initializeState();

            try {
                File file = new File(activity.getFilesDir(), filename);
                file.createNewFile();

                GameState ms1 = new GameState(n);
                GameState ms2 = activity.readStateFromFile();
                assertTrue(compareStates(ms1, ms2));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            activity.getFilesDir().delete();
        }
    }

    @Test // reading state from corrupted file should return new GameState object
    public void readStateFromFileCorrupted() {
        int n = 2;
        String filename = "stateFile.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = (controller.get());
            activity.initializeState();

            try {
                File file = new File(activity.getFilesDir(), filename);
                file.createNewFile();

                String str = "Hello";
                FileOutputStream outputStream = new FileOutputStream(file.getAbsolutePath());
                byte[] strToBytes = str.getBytes();
                outputStream.write(strToBytes);
                outputStream.close();

                GameState ms1 = new GameState(n);
                GameState ms2 = activity.readStateFromFile();
                assertTrue(compareStates(ms1, ms2));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            activity.getFilesDir().delete();
        }
    }

    @Test // saving state to file when file name provided actually creates file under given name
    public void saveStateToFileFilenameGiven() {
        int n = 2;
        String filename = "stateFile.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = controller.get();
            activity.initializeState();

            GameState ms = new GameState(n);
            activity.saveStateToFile(ms);

            File file = new File(activity.getFilesDir(), filename);
            assertTrue(file.exists());

            activity.getFilesDir().delete();
        }
    }


    @Test // saving state to file when file name not provided actually creates file under new name
    public void saveStateToFileFilenameNotGiven() {
        int n = 2;

        Intent intent = new Intent();
        intent.putExtra("n", n);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = controller.get();
            activity.initializeState();

            GameState ms = new GameState(n);
            activity.saveStateToFile(ms);

            File file = new File(activity.getFilesDir(), "state"+n+".txt");
            assertTrue(file.exists());

            activity.getFilesDir().delete();
        }
    }

    @Test(expected = NullPointerException.class) // writes to file
    public void saveStateToFileNullState() {
        int n = 2;
        String filename = "stateFile.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = controller.get();

            activity.initializeState();
            activity.saveStateToFile(null);

            File file = new File(activity.getFilesDir(), filename);
            assertTrue(file.exists());

            activity.getFilesDir().delete();

        }
    }


    @Test // calling saveStateToFile on a GameState object then calling readStateFromFile should return same object
    public void integrationTestOnSaveStateToFileAndReadStateFromFile() {
        int n = 2;
        String filename = "stateFile.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = (controller.get());
            activity.initializeState();

            GameState ms1 = new GameState(n);
            ms1.points = 30;
            ms1.n = n;
            ms1.undo = true;
            ms1.numbers = new int[]{1,2};

            activity.saveStateToFile(ms1);
            GameState ms2 = activity.readStateFromFile();
            assertTrue(compareStates(ms1, ms2));

            activity.getFilesDir().delete();
        }
    }

    @Test // reading file with null object should not crash game
    public void integrationTestOnSaveStateToFileAndReadStateFromFileWithNullState() {
        int n = 2;
        String filename = "stateFile.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = (controller.get());
            activity.initializeState();

            activity.saveStateToFile(null);
            GameState ms1 = new GameState(n);
            GameState ms2 = activity.readStateFromFile();
            assertTrue(compareStates(ms1, ms2));

            activity.getFilesDir().delete();
        }
    }

    @Test // deleting a nonexistent state file should return false
    public void deleteStateFileNonexistentFilenameGiven() {
        int n = 2;
        String filename = "stateFile.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = (controller.get());
            activity.initializeState();

            assertFalse(activity.deleteStateFile());
            activity.getFilesDir().delete();
        }
    }

    @Test // deleting a nonexistent state file should return false
    public void deleteStateFileNonexistentFilenameNotGiven() {
        int n = 2;

        Intent intent = new Intent();
        intent.putExtra("n", n);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = (controller.get());
            activity.initializeState();

            assertFalse(activity.deleteStateFile());
            activity.getFilesDir().delete();
        }
    }

    @Test // deleting a state file that exist should return true
    public void deleteStateFileExist() {
        int n = 2;
        String filename = "stateFile.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = (controller.get());
            activity.initializeState();

            try {
                File file = new File(activity.getFilesDir(), filename);
                file.createNewFile();
                assertTrue(activity.deleteStateFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            activity.getFilesDir().delete();
        }
    }

    @Test // saving a state to file and then deleting that file should return true
    public void integrationTestSaveStateToFileAndDeleteStateFile () {
        int n = 2;
        String filename = "stateFile.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = (controller.get());
            activity.initializeState();

            GameState ms = new GameState(n);
            activity.saveStateToFile(ms);
            assertTrue(activity.deleteStateFile());

            activity.getFilesDir().delete();
        }
    }

    private boolean compareStatistics(GameStatistics gs1, GameStatistics gs2) {
        return gs1.getFilename().equals(gs2.getFilename()) &&
                gs1.toString().equals(gs2.toString());
    }

    private boolean compareStates(GameState gs1, GameState gs2) {
        return gs1.toString().equals(gs2.toString());
    }
}