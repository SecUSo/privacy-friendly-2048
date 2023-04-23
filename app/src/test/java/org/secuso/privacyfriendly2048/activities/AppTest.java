package org.secuso.privacyfriendly2048.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
//import org.robolectric.shadows.support.v4.Shadows;
import org.secuso.privacyfriendly2048.R;
import org.secuso.privacyfriendly2048.activities.helper.GameStatistics;
import org.secuso.privacyfriendly2048.activities.helper.GameState;
import org.secuso.privacyfriendly2048.helpers.FirstLaunchManager;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RunWith(RobolectricTestRunner.class)
public class AppTest {

    /*
    Test Helper Functions
     */
    private boolean compareStatistics(GameStatistics gs1, GameStatistics gs2) {
        return gs1.getFilename().equals(gs2.getFilename()) &&
                gs1.toString().equals(gs2.toString());
    }

    private boolean compareStates(GameState gs1, GameState gs2) {
        return gs1.toString().equals(gs2.toString());
    }

    /*
    GameActivity Tests
     */

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

    @Test // reading file with null object should not return null object
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

    @Test // calling readStatisticsFRomFile on GameState Object file should recognize wrong object and return new Stats object
    public void integrationTestOnSaveStateToFileAndReadStatisticsFromFileWithStateObject() {
        int n = 2;
        String filename = "statistics2.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = (controller.get());
            activity.initializeState();

            activity.saveStateToFile(new GameState(n));
            GameStatistics gs1 = new GameStatistics(n);
            GameStatistics gs2 = activity.readStatisticsFromFile();
            assertTrue(compareStatistics(gs1, gs2));

            activity.getFilesDir().delete();
        }
    }

    @Test // calling readStateFromFile on GameStatistics Object file should recognize wrong object and return new State object
    public void integrationTestOnSaveStatisticsToFileAndReadStateFromFileWithStatsObject() {
        int n = 2;
        String filename = "statistics2.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = (controller.get());
            activity.initializeState();

            activity.saveStatisticsToFile(new GameStatistics(n));
            GameState ms1 = new GameState(n);
            GameState ms2 = activity.readStateFromFile();
            assertTrue(compareStates(ms1, ms2));

            activity.getFilesDir().delete();
        }
    }

    @Test // reading file with null object should not return null object
    public void integrationTestOnSaveStateToFileAndReadStatisticsFromFileWithNullState() {
        int n = 2;
        String filename = "statistics2.txt";

        Intent intent = new Intent();
        intent.putExtra("n", n);
        intent.putExtra("filename", filename);

        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent)) {
            controller.setup();
            GameActivity activity = (controller.get());
            activity.initializeState();

            activity.saveStateToFile(null);
            GameStatistics gs1 = new GameStatistics(n);
            GameStatistics gs2 = activity.readStatisticsFromFile();
            assertTrue(compareStatistics(gs1, gs2));

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

    /*
    FirstLaunchManager Tests
     */

    @Test
    public void testIsFirstTimeLaunchTrue() {
        Context context = ApplicationProvider.getApplicationContext();
        FirstLaunchManager manager = new FirstLaunchManager(context);
        manager.setFirstTimeLaunch(true);
        assertTrue(manager.isFirstTimeLaunch());
    }

    @Test
    public void testIsFirstTimeLaunchFalse() {
        Context context = ApplicationProvider.getApplicationContext();
        FirstLaunchManager manager = new FirstLaunchManager(context);
        manager.setFirstTimeLaunch(false);
        assertFalse(manager.isFirstTimeLaunch());

    }

    /*
    Main Activity Tests
     */

    // the following three tests simply tests when the next or prev button are visible

    @Test
    public void testUpdateMovingButtonsBtnPrevInvisibleBtnNextVisible() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class)) {
            controller.setup();
            MainActivity activity = controller.get();

            activity.updateMovingButtons(0);
            assertEquals(4, activity.findViewById(R.id.btn_prev).getVisibility());
            assertEquals(0, activity.findViewById(R.id.btn_next).getVisibility());
        }

    }

    @Test
    public void testUpdateMovingButtonsBtnPrevVisibleBtnNextInvisible() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class)) {
            controller.setup();
            MainActivity activity = controller.get();

            activity.updateMovingButtons(3);
            assertEquals(0, activity.findViewById(R.id.btn_prev).getVisibility());
            assertEquals(4, activity.findViewById(R.id.btn_next).getVisibility());
        }

    }

    @Test
    public void testUpdateMovingButtonsBtnPrevVisibleBtnNextVisible() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class)) {
            controller.setup();
            MainActivity activity = controller.get();

            activity.updateMovingButtons(1);
            assertEquals(0, activity.findViewById(R.id.btn_prev).getVisibility());
            assertEquals(0, activity.findViewById(R.id.btn_next).getVisibility());
        }

    }


    /*
    Stats Activity Tests
     */

    @Test
    public void testResetGameStatisticsNotDeleteNonStatsFile() {
        try (ActivityController<StatsActivity> controller = Robolectric.buildActivity(StatsActivity.class)) {
            controller.setup();
            StatsActivity activity = controller.get();

            File file = new File(activity.getFilesDir(), "Statistics3.txt");
            file.createNewFile();
            activity.resetGameStatistics();
            assertTrue(file.exists());

        } catch (IOException e) {
//            throw new RuntimeException(e);
        }

    }

    @Test
    public void testResetGameStatisticsNotDeleteStatsFile() {
        try (ActivityController<StatsActivity> controller = Robolectric.buildActivity(StatsActivity.class)) {
            controller.setup();
            StatsActivity activity = controller.get();

            File file = new File(activity.getFilesDir(), "Statistics4.txt");
            File file2 = new File(activity.getFilesDir(), "Statistics7.txt");
            file.createNewFile();
            file2.createNewFile();


            activity.resetGameStatistics();
            assertFalse(file.exists());
            assertFalse(file2.exists());

        } catch (IOException e) {
//            throw new RuntimeException(e);
        }

    }

    // the next 6 tests just checks that no exception is thrown when the functions take the boundary values
    @Test
    public void testFormatSmallMillis0() {
        try (ActivityController<StatsActivity> controller = Robolectric.buildActivity(StatsActivity.class)) {
            controller.setup();
            StatsActivity activity = controller.get();

            assertEquals("0.00 s", activity.mSectionsPagerAdapter.formatSmallMillis(0L));
        }
    }

    @Test
    public void testFormatSmallMillisMaxLong() {
        try (ActivityController<StatsActivity> controller = Robolectric.buildActivity(StatsActivity.class)) {
            controller.setup();
            StatsActivity activity = controller.get();

            activity.mSectionsPagerAdapter.formatSmallMillis(Long.MAX_VALUE);
        }
    }

    @Test
    public void testFormatSmallMillisMinLong() {
        try (ActivityController<StatsActivity> controller = Robolectric.buildActivity(StatsActivity.class)) {
            controller.setup();
            StatsActivity activity = controller.get();

            activity.mSectionsPagerAdapter.formatSmallMillis(Long.MIN_VALUE);
        }
    }

    @Test
    public void testFormatMillis0() {
        try (ActivityController<StatsActivity> controller = Robolectric.buildActivity(StatsActivity.class)) {
            controller.create();
            StatsActivity activity = controller.get();

            assertEquals("0.00 h", activity.mSectionsPagerAdapter.formatMillis(0L));
        }
    }

    @Test
    public void testFormatMillisMaxLong() {
        try (ActivityController<StatsActivity> controller = Robolectric.buildActivity(StatsActivity.class)) {
            controller.setup();
            StatsActivity activity = controller.get();

            activity.mSectionsPagerAdapter.formatMillis(Long.MAX_VALUE);
        }
    }

    @Test
    public void testFormatMillisMinLong() {
        try (ActivityController<StatsActivity> controller = Robolectric.buildActivity(StatsActivity.class)) {
            controller.setup();
            StatsActivity activity = controller.get();

            activity.mSectionsPagerAdapter.formatMillis(Long.MIN_VALUE);
        }
    }

    @Test // calling readStatisticsFromFile in Statistics.MyViewPagerAdapter when theres no file to load returns new GameStatistics object
    public void testReadStatsFromFilePagerNonexistentFile() {
        try (ActivityController<StatsActivity> controller = Robolectric.buildActivity(StatsActivity.class)) {
            controller.setup();
            StatsActivity activity = controller.get();

            GameStatistics gs1 = activity.mSectionsPagerAdapter.readStatisticsFromFile(2);
            GameStatistics gs2 = new GameStatistics(2);
            assertTrue(compareStatistics(gs1, gs2));
        }
    }

    @Test // calling readStatisticsFromFile in Statistics.MyViewPagerAdapter when theres is file to load returns that stats object
    public void testReadStatsFromFilePagerExistentFile() {
        try (ActivityController<StatsActivity> controller = Robolectric.buildActivity(StatsActivity.class)) {
            controller.setup();
            StatsActivity activity = controller.get();
            int n = 2;

            File file = new File(activity.getFilesDir(), "Statistics"+n+".txt");
            file.createNewFile();

            GameStatistics gs1 = activity.mSectionsPagerAdapter.readStatisticsFromFile(n);
            GameStatistics gs2 = new GameStatistics(n);
            assertTrue(compareStatistics(gs1, gs2));
        } catch (IOException e) {

        }
    }
//
//    @Test // calling saveStatisticsToFile in gameActivity and then readStatisticsFromFile in Statistics.MyViewPagerAdapter when theres is file to load returns that stats object
//    public void testReadStatsFromFileIntegrationWithSaveStatisticsFromFileValidObject() {
//        int n = 2;
//
//        GameStatistics gs1 = new GameStatistics(n);
//        gs1.setHighestNumber(100);
//        gs1.setRecord(500);
//
//        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class)) {
//            controller.setup();
//            GameActivity activity = (controller.get());
//            activity.initializeState();
//
//            activity.saveStatisticsToFile(gs1);
//            File file = new File (activity.getFilesDir(), gs1.getFilename());
//            assertTrue(file.exists());
//        }
//
//        try (ActivityController<StatsActivity> controller = Robolectric.buildActivity(StatsActivity.class)) {
//            controller.setup();
//            StatsActivity activity = controller.get();
//
//            GameStatistics gs2 = activity.mSectionsPagerAdapter.readStatisticsFromFile(n);
//
//            assertTrue(compareStatistics(gs1, gs2));
//        }
//    }

//    @Test // calling saveStatisticsToFile in gameActivity to save null object and then readStatisticsFromFile in Statistics.MyViewPagerAdapter when theres is file to load returns that stats object
//    public void testReadStatsFromFileIntegrationWithSaveStatisticsFromFileNullObject() {
//        int n = 2;
//
//        GameStatistics gs1 = new GameStatistics(n);
//        gs1.setHighestNumber(100);
//        gs1.setRecord(500);
//
//        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class)) {
//            controller.setup();
//            GameActivity activity = (controller.get());
//            activity.initializeState();
//
//            activity.saveStatisticsToFile(gs1);
//            File file = new File (activity.getFilesDir(), gs1.getFilename());
//            assertTrue(file.exists());
//        }
//
//        try (ActivityController<StatsActivity> controller = Robolectric.buildActivity(StatsActivity.class)) {
//            controller.setup();
//            StatsActivity activity = controller.get();
//
//            GameStatistics gs2 = activity.mSectionsPagerAdapter.readStatisticsFromFile(n);
//
//            assertTrue(compareStatistics(gs1, gs2));
//        }
//    }



}