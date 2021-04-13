package com.bluegeeks.foodymoody

import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties
import junit.framework.AssertionFailedError
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessengerInstrumentedTest {

    companion object {
        @AfterClass @JvmStatic fun teardown() {
            BaseFirebaseProperties.realtimeDB.reference.child("7LPevX6rGvfvYIskt6mLL4TuzHR2").removeValue()
            BaseFirebaseProperties.realtimeDB.reference.child("HfBoG48043bKGh5p7t57l917b1p1").removeValue()
        }
    }

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
    fun check1MessageWasSend() {
        try {
            onView((withId(R.id.postsRecyclerView)))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            onView((withId(R.id.action_logout))).perform(click())
            Thread.sleep(1000)
        } catch (e: AssertionFailedError) {
            //DO NOTHING
        } catch (e: NoMatchingViewException) {
            //DO NOTHING
        }
        onView(withId(R.id.editText_email))
            .perform(
                typeText("programmingtutorials45@gmail.com"),
                closeSoftKeyboard()
            )
        onView(withId(R.id.editText_email))
            .check(ViewAssertions.matches(ViewMatchers.withText("programmingtutorials45@gmail.com")))
        onView(withId(R.id.editText_password))
            .perform(typeText("Google8667%@"), closeSoftKeyboard())
        onView(withId(R.id.editText_password))
            .check(ViewAssertions.matches(ViewMatchers.withText("Google8667%@")))
        onView(withId(R.id.login_button)).perform(click())
        Thread.sleep(2000)
        onView((withId(R.id.action_serach))).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.searchUser)).perform(typeText("00.08.053\n"));
        onView(withId(R.id.searchUser)).perform(typeText("testPartThirtyFour"));
        Thread.sleep(2000)
        onView(withId(R.id.searchResultRecyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.message_button)).perform(click())
        onView(withId(R.id.edit_chat_message))
            .perform(
                typeText("Hi, this is a test message !"),
                closeSoftKeyboard()
            )
        onView(withId(R.id.button_chat_send)).perform(click())
        onView(withId(R.id.chat_recycler)).check(matches(hasChildCount(1)))
    }

    /**
     * Instrumented Unit test- Checks if a message was received properly
     */
    @Test
    fun check2IfTheOtherReceivedTheMessage() {
        try {
            onView((withId(R.id.postsRecyclerView)))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            onView((withId(R.id.action_logout))).perform(click())
            Thread.sleep(1000)
        } catch (e: AssertionFailedError) {
            //DO NOTHING
        } catch (e: NoMatchingViewException) {
            //DO NOTHING
        }
        onView(withId(R.id.editText_email))
            .perform(
                typeText("foodymoodytest@protonmail.com"),
                closeSoftKeyboard()
            )
        onView(withId(R.id.editText_email))
            .check(ViewAssertions.matches(ViewMatchers.withText("foodymoodytest@protonmail.com")))
        onView(withId(R.id.editText_password))
            .perform(typeText("Google8667%@"), closeSoftKeyboard())
        onView(withId(R.id.editText_password))
            .check(ViewAssertions.matches(ViewMatchers.withText("Google8667%@")))
        onView(withId(R.id.login_button)).perform(click())
        Thread.sleep(2000)
        onView((withId(R.id.action_serach))).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.searchUser)).perform(typeText("00.08.053\n"));
        onView(withId(R.id.searchUser)).perform(typeText("testerPartTwoThirtyOne"));
        Thread.sleep(2000)
        onView(withId(R.id.searchResultRecyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.message_button)).perform(click())
        onView(withId(R.id.edit_chat_message))
            .perform(
                typeText("Hi, I Have Recieved the message"),
                closeSoftKeyboard()
            )
        onView(withId(R.id.button_chat_send)).perform(click())
        onView(withId(R.id.chat_recycler)).check(matches(hasChildCount(2)))
    }
}