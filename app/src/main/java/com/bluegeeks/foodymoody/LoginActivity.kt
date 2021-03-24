package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.rootDB
import com.bluegeeks.foodymoody.entity.User
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class LoginActivity : AppCompatActivity() {

    private val auth = Firebase.auth
    private val RC_SIGN_IN = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (FirebaseAuth.getInstance().currentUser != null) {
            // Sign in success, update UI with the signed-in user's information
            Log.i("TAG", "Sign In was Successful")
            val user = auth.currentUser
            if (user != null) {
                if (!user.isEmailVerified) {
                    Toast.makeText(
                            this,
                            "Please Verify you're email before you can Login",
                            Toast.LENGTH_LONG
                    ).show()
                } else {
                    val i = Intent(applicationContext, HomeActivity::class.java)
                    startActivity(i)
                }
            }
        }

        var clicked = false
        //This is for the password visibility
        button_visiblity.setOnClickListener {
            if (editText_password.text.toString().isNotEmpty()) {
                if (!clicked) {
                    editText_password.transformationMethod =
                            HideReturnsTransformationMethod.getInstance()
                    button_visiblity.setBackgroundResource(R.drawable.invisible)
                    //setSelection is added so that the cursor stays at the end when the hide button is pressed
                    editText_password.setSelection(editText_password.length())
                    clicked = true
                } else {
                    editText_password.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                    button_visiblity.setBackgroundResource(R.drawable.visible)
                    editText_password.setSelection(editText_password.length())
                    clicked = false
                }
            }
        }

        //Sign Up button is clicked
        button_signUp.setOnClickListener {
            //When the button is pressed the Sign Up activity is opened
            val i = Intent(applicationContext, SignUpActivity::class.java)
            startActivity(i)
        }

        //Login button is clicked
        //Login Button onClick listener verifies the login details
        login_button.setOnClickListener {
            val username = editText_email.text.toString().trim()
            val passwordSecurity = editText_password.text.toString().trim()

            //If the values or not empty proceed with authentication process.
            //Firebase login code referred from https://firebase.google.com/docs/auth/android/password-auth
            if (username.isNotEmpty() && passwordSecurity.isNotEmpty()) {
                auth.signInWithEmailAndPassword(username, passwordSecurity)
                        .addOnCompleteListener(this) { task: Task<AuthResult> ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.i("TAG", "Sign In was Successful")
                                val user = auth.currentUser
                                if (user != null) {
                                    if (!user.isEmailVerified) {
                                        Toast.makeText(
                                                this,
                                                "Please Verify you're email before you can Login",
                                                Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        val i = Intent(applicationContext, HomeActivity::class.java)
                                        startActivity(i)
                                        finish()
                                    }
                                }
                            } else {
                                try {
                                    throw task.exception!!
                                } catch (e: FirebaseAuthInvalidUserException) {
                                    Toast.makeText(this, "The user with this email does not exist, Please sign Up for a Account", Toast.LENGTH_LONG)
                                            .show()
                                } catch (e: FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(this, "Wrong Password Please Try Again", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Log.e("TAG", e.message!!)
                                }
                            }
                        }
            } else {
                Toast.makeText(this, "Please enter you're Username and Password.", Toast.LENGTH_LONG).show()
            }
        }

        signin_google_button.setOnClickListener {
            // Choose authentication providers
            val providers = arrayListOf(
                    AuthUI.IdpConfig.GoogleBuilder().build()
            )
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(true)
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN
            )
        }

        signin_twitter_button.setOnClickListener {
            // Choose authentication providers
            val providers = arrayListOf(
                    AuthUI.IdpConfig.TwitterBuilder().build()
            )
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(true)
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN
            )
        }


        signin_facebook_button.setOnClickListener {
            // Choose authentication providers
            val providers = arrayListOf(
                    AuthUI.IdpConfig.FacebookBuilder().build()
            )
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(true)
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN
            )
        }

        //User Has Forgotten Password and clicks "Forgot Password?"
        button_retrieving.setOnClickListener {
            //When the button is pressed the Forgot Password is opened
            val i = Intent(applicationContext, RetrievePasswordActivity::class.java)
            startActivity(i)
        }

    }


    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                // send to list activity
                val intent = Intent(applicationContext, HomeActivity::class.java)

                rootDB.collection("users").whereEqualTo("email", user?.email).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (!(document?.isEmpty!!)) {
                            Log.i("TAG", "User is registered")
                        } else {
                            try {
                                val email = user?.email
                                val firstName =  ""
                                val lastName = ""
                                val userName = user?.displayName
                                val birthDay = ""
                                val photoURI = user?.photoUrl.toString()
                                val id = BaseFirebaseProperties.authDb.currentUser!!.uid
                                val followers = ArrayList<String>()
                                val following = ArrayList<String>()
                                val postsID = ArrayList<String>()
                                val bio = ""
                                val time = getTime()
                                val private = false

                                val newUser =
                                    User(
                                        id,
                                        email,
                                        firstName,
                                        lastName,
                                        userName,
                                        birthDay,
                                        photoURI,
                                        followers,
                                        following,
                                        postsID,
                                        bio,
                                        time,
                                        private
                                    )
                                rootDB.collection("users").document(newUser.id!!).set(newUser)
                            } catch (e: Exception) {
                                Log.e("TAG", e.message!!)
                                finish()
                            }
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Log.i("TAG", "get failed with ", task.exception)
                        Toast.makeText(this, "Please try again", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, "Invalid Login", Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
        return sdf.format(Date())
    }
}