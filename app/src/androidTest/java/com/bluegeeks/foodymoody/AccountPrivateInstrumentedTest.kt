package com.bluegeeks.foodymoody

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties
import junit.framework.AssertionFailedError
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountPrivateInstrumentedTest {
    @Rule
    @JvmField
    var activityScenarioRule: ActivityScenarioRule<LoginActivity> =
        ActivityScenarioRule<LoginActivity>(LoginActivity::class.java)

    @Before
    fun setUp() {
        activityScenarioRule.scenario
    }

    /**
     * Instrumented Unit test- Checks if a message was properly send
     */
    @Test
    fun makeAccountPrivate() {
        try {
            onView((withId(R.id.postsRecyclerView)))
                .check(matches(ViewMatchers.isDisplayed()))
            onView((withId(R.id.action_logout))).perform(click())
            Thread.sleep(1000)
        } catch (e: AssertionFailedError) {
            //DO NOTHING
        } catch (e: NoMatchingViewException) {
            //DO NOTHING
        }
        onView(withId(R.id.editText_email))
            .perform(
                ViewActions.typeText("programmingtutorials45@gmail.com"),
                ViewActions.closeSoftKeyboard()
            )
        onView(withId(R.id.editText_email))
            .check(matches(ViewMatchers.withText("programmingtutorials45@gmail.com")))
        onView(withId(R.id.editText_password))
            .perform(ViewActions.typeText("Google8667%@"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.editText_password))
            .check(matches(ViewMatchers.withText("Google8667%@")))
        onView(withId(R.id.login_button)).perform(click())
        Thread.sleep(2000)
        onView((withId(R.id.action_personal))).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.imageView_profile_picture)).perform(click())
        onView((withId(R.id.Button_updateSecurity))).perform(scrollTo(), click())
        onView(withId(R.id.checkBoxPrivate)).perform(click())
        onView(withId(R.id.Button_profile_security_update)).perform(click())
        onView(withId(R.id.action_back)).perform(click())
        onView((withId(R.id.action_logout))).perform(click())
        Thread.sleep(1000)
        onView(withId(R.id.editText_email))
            .perform(
                ViewActions.typeText("foodymoodytest@protonmail.com"),
                ViewActions.closeSoftKeyboard()
            )
        onView(withId(R.id.editText_email))
            .check(matches(ViewMatchers.withText("foodymoodytest@protonmail.com")))
        onView(withId(R.id.editText_password))
            .perform(ViewActions.typeText("Google8667%@"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.editText_password))
            .check(matches(ViewMatchers.withText("Google8667%@")))
        onView(withId(R.id.login_button)).perform(click())
        Thread.sleep(2000)
        onView((withId(R.id.action_serach))).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.searchUser)).perform(ViewActions.typeText("00.08.053\n"));
        onView(withId(R.id.searchUser)).perform(ViewActions.typeText("testerPartTwoThirtyOne"));
        Thread.sleep(2000)
        onView(withId(R.id.searchResultRecyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        onView((withId(R.id.follow_button))).perform(click())
        onView(withId(R.id.follow_button))
            .check(matches(ViewMatchers.withText("Request sent")))
        onView((withId(R.id.follow_button))).perform(click())
        onView(withId(R.id.follow_button))
            .check(matches(ViewMatchers.withText("Follow")))
        onView((withId(R.id.action_logout))).perform(click())
    }

    @After
    fun destroy() {
        //Removing the user
            BaseFirebaseProperties.rootDB.collection("users").document("7LPevX6rGvfvYIskt6mLL4TuzHR2")
            .update(
                mapOf(
                    "private" to false
                )
            )
    }
}