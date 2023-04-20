package org.secuso.privacyfriendly2048;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.app.Application;
import android.test.ApplicationTestCase;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.secuso.privacyfriendly2048.activities.MainActivity;
import org.secuso.privacyfriendly2048.activities.SplashActivity;

@RunWith(AndroidJUnit4.class)
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Rule public ActivityScenarioRule<SplashActivity> activityScenarioRule
            = new ActivityScenarioRule<>(SplashActivity.class);

    @Before
    public void setup() {
        Intents.init();
    }

    @After
    public void teardown() {
        Intents.release();
    }

    @Test
    public void testTutorialSkipLaunchesMainActivity() {
        // press skip button
        onView(withId(R.id.btn_skip)).perform(click());

        // check that the MainActivity intent has been sent
        // intended works similarly to verify from mockito
        intended(hasComponent(MainActivity.class.getName()));

        // another sanity check; check that the buttons on the MainActivity view are present
        onView(withId(R.id.view_pager)).check(matches(isDisplayed()));
        onView(withId(R.id.button_continueGame)).check(matches(isDisplayed()));
    }

    @Test
    public void testTutorialNextGoesToNextPage() {
        // sanity check, make sure current image being displayed is the first tutorial image
        onView(withId(R.id.image1))
                .check(matches(isCompletelyDisplayed()));

        // press next button
        onView(withId(R.id.btn_next)).perform(click());

        // now check that image 1 is no longer visible, but image 2 is
        onView(withId(R.id.image1))
                .check(matches(not(isCompletelyDisplayed())));
         onView(withId(R.id.image2))
                .check(matches(isCompletelyDisplayed()));

        // press next button
        onView(withId(R.id.btn_next)).perform(click());

        // now check that image 2 is no longer visible, but image 3 is
        onView(withId(R.id.image2))
                .check(matches(not(isCompletelyDisplayed())));
        onView(withId(R.id.image3))
                .check(matches(isCompletelyDisplayed()));
        // press next button
        onView(withId(R.id.btn_next)).perform(click());

        // now check that image 3 is no longer visible, but image 4 is
        onView(withId(R.id.image3))
                .check(matches(not(isCompletelyDisplayed())));
        onView(withId(R.id.image4))
                .check(matches(isCompletelyDisplayed()));
    }

    @Test
    public void testTutorialFinishingTutorialChangesNextButtonText() {
        // there are three pages of tutorial before the last one,
        // so click "next" three times
        onView(withId(R.id.btn_next))
                .perform(click())
                .perform(click())
                .perform(click());

        // text of the next button should become "Okay"
        onView(withId(R.id.btn_next)).check(matches(withText("Okay")));
    }

    @Test
    public void testTutorialFinishingTutorialMakesSkipButtonInvisible() {
        // there are three pages of tutorial before the last one,
        // so click "next" three times
        onView(withId(R.id.btn_next))
                .perform(click())
                .perform(click())
                .perform(click());

        // the skip button should become invisible
        onView(withId(R.id.btn_skip)).check(matches(withEffectiveVisibility(GONE)));
    }

    @Test
    public void testTutorialFinishingTutorialLaunchesMainActivity() {
        // there are three pages of tutorial before the last one,
        // so click "next" three times
        onView(withId(R.id.btn_next))
                .perform(click())
                .perform(click())
                .perform(click())
                // text of the next button should become "Okay"
                .check(matches(withText("Okay")));

        // now click okay and check that the MainActivity intent has been sent
        onView(withId(R.id.btn_next)).perform(click());
        intended(hasComponent(MainActivity.class.getName()));

        // another sanity check; check that the buttons on the MainActivity view are present
        onView(withId(R.id.button_newGame)).check(matches(isDisplayed()));
        onView(withId(R.id.button_continueGame)).check(matches(isDisplayed()));
    }
}