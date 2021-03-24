package com.bluegeeks.foodymoody

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.authDb
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.rootDB
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignUpInstrumentedTest {
    private lateinit var signUpScuccess: String
    private lateinit var passwordStrengthError: String
    private lateinit var emailAlreadyInUse: String

    @Rule
    @JvmField
    var activityScenarioRule: ActivityScenarioRule<LoginActivity> = ActivityScenarioRule<LoginActivity>(LoginActivity::class.java)

    @Before
    fun setUp(){
        signUpScuccess = "A confirmation e-mail has been send."
        passwordStrengthError = "Your password must contain Minimum eight characters, at least one uppercase letter, \n one lowercase letter, one number and one special character:"
        emailAlreadyInUse = "The email address is already in use by another account."
    }

    /**
     * Instrumented Unit test- Verifies creation of an account
     */
    @Test
    fun createNewAccountSuccess() {
        //Go to sign Up page
        Espresso.onView(ViewMatchers.withId(R.id.button_signUp)).perform(ViewActions.click())
        //Enter values
        Espresso.onView(ViewMatchers.withId(R.id.signUpUsername)).perform(ViewActions.typeText("tester007"),ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.signUpUsername))
            .check(ViewAssertions.matches(ViewMatchers.withText("tester007")))
        Espresso.onView(ViewMatchers.withId(R.id.signUpEmail))
            .perform(ViewActions.typeText("tester007@gmail.com"), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.signUpEmail))
            .check(ViewAssertions.matches(ViewMatchers.withText("tester007@gmail.com")))
        Espresso.onView(ViewMatchers.withId(R.id.signUpPassword))
            .perform(ViewActions.typeText("EJej1234*987&"), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.signUpPassword))
            .check(ViewAssertions.matches(ViewMatchers.withText("EJej1234*987&")))
        Espresso.onView(ViewMatchers.withId(R.id.signUpConfirmPassword))
            .perform(ViewActions.typeText("EJej1234*987&"), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.signUpConfirmPassword))
            .check(ViewAssertions.matches(ViewMatchers.withText("EJej1234*987&")))
        Espresso.onView(ViewMatchers.withId(R.id.signUpButton)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(signUpScuccess)).inRoot(ToastMatcher())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    /**
     * Instrumented Unit test- Verifies Password strength for user sign up
     */
    @Test
    fun passwordStrength() {
        //Go to sign Up page
        Espresso.onView(ViewMatchers.withId(R.id.button_signUp)).perform(ViewActions.click())
        //Enter values
        Espresso.onView(ViewMatchers.withId(R.id.signUpUsername)).perform(ViewActions.typeText("tester007"),ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.signUpUsername))
            .check(ViewAssertions.matches(ViewMatchers.withText("tester007")))
        Espresso.onView(ViewMatchers.withId(R.id.signUpEmail))
            .perform(ViewActions.typeText("tester007@gmail.com"), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.signUpEmail))
            .check(ViewAssertions.matches(ViewMatchers.withText("tester007@gmail.com")))
        Espresso.onView(ViewMatchers.withId(R.id.signUpPassword))
            .perform(ViewActions.typeText("hi"), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.signUpPassword))
            .check(ViewAssertions.matches(ViewMatchers.withText("hi")))
        Espresso.onView(ViewMatchers.withId(R.id.signUpConfirmPassword))
            .perform(ViewActions.typeText("hi"), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.signUpConfirmPassword))
            .check(ViewAssertions.matches(ViewMatchers.withText("hi")))
        Espresso.onView(ViewMatchers.withId(R.id.signUpButton)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(passwordStrengthError)).inRoot(ToastMatcher())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    /**
     * Instrumented Unit test- Verifies if e-mail ID already exists
     */
    @Test
    fun checkEmailAlreadyExist() {
        //Go to sign Up page
        Espresso.onView(ViewMatchers.withId(R.id.button_signUp)).perform(ViewActions.click())
        //Enter values
        Espresso.onView(ViewMatchers.withId(R.id.signUpUsername)).perform(ViewActions.typeText("tester977665655"),ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.signUpUsername))
            .check(ViewAssertions.matches(ViewMatchers.withText("tester977665655")))
        Espresso.onView(ViewMatchers.withId(R.id.signUpEmail))
            .perform(ViewActions.typeText("tester977665655@test.com"), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.signUpEmail))
            .check(ViewAssertions.matches(ViewMatchers.withText("tester977665655@test.com")))
        Espresso.onView(ViewMatchers.withId(R.id.signUpPassword))
            .perform(ViewActions.typeText("EJej1234*987&"), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.signUpPassword))
            .check(ViewAssertions.matches(ViewMatchers.withText("EJej1234*987&")))
        Espresso.onView(ViewMatchers.withId(R.id.signUpConfirmPassword))
            .perform(ViewActions.typeText("EJej1234*987&"), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.signUpConfirmPassword))
            .check(ViewAssertions.matches(ViewMatchers.withText("EJej1234*987&")))
        Espresso.onView(ViewMatchers.withId(R.id.signUpButton)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(emailAlreadyInUse)).inRoot(ToastMatcher())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @After
    fun deleteTheAccountCreated() {
        //Removing the user
        authDb.currentUser?.uid?.let { rootDB.collection("users").document(it).delete() }
        authDb.currentUser?.delete()
        authDb.signOut()
    }
}