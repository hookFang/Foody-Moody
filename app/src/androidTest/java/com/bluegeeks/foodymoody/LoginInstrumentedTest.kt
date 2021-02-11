package com.bluegeeks.foodymoody

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.authDb
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {
    private lateinit var emailNotExistError: String
    private lateinit var blankValues: String

    @Rule
    @JvmField
    var activityScenarioRule: ActivityScenarioRule<LoginActivity> = ActivityScenarioRule<LoginActivity>(LoginActivity::class.java)

    @Before
    fun setUp(){
        activityScenarioRule.scenario
        emailNotExistError = "The user with this email does not exist, Please sign Up for a Account"
        blankValues = "Please enter you're Username and Password."
    }

    /**
     * Instrumented Unit test- checks the login credentials
     */
    @Test
    fun successfullLoginCreds() {
        onView(withId(R.id.editText_email))
            .perform(typeText("programmingtutorials45@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.editText_email)).check(matches(withText("programmingtutorials45@gmail.com")))
        onView(withId(R.id.editText_password))
            .perform(typeText("Google8667%@"), closeSoftKeyboard());
        onView(withId(R.id.editText_password)).check(matches(withText("Google8667%@")))
        onView(withId(R.id.login_button)).perform(click())
        Thread.sleep(2000)
        onView((withId(R.id.postsRecyclerView))).check(matches(isDisplayed()))
    }

    /**
     * Instrumented Unit test- Invalid Login credentials
     */
    @Test
    fun invalidLoginCreds() {
        onView(withId(R.id.editText_email))
            .perform(typeText("edwinchrist52ie100@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.editText_password))
            .perform(typeText("Google8667%@"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click())
        onView(withText(emailNotExistError)).inRoot(ToastMatcher())
            .check(matches(isDisplayed()))
    }

    /**
     * Instrumented Unit Test - Blank fields
     */
    @Test
    fun blankCredentials() {
        onView(withId(R.id.login_button)).perform(click())
        onView(withText(blankValues)).inRoot(ToastMatcher())
                .check(matches(isDisplayed()))
    }

    @After
    fun signOut() {
        authDb.signOut()
    }
}