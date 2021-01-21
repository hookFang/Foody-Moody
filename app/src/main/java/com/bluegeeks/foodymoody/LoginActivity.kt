package com.bluegeeks.foodymoody

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var clicked = false
        //This is for the password visibility
        button_visiblity.setOnClickListener {
            if (editText_password.text.toString().isNotEmpty()) {
                if (!clicked) {
                    editText_password.transformationMethod =
                            HideReturnsTransformationMethod.getInstance()
                    button_visiblity.setBackgroundResource(R.drawable.invisible)
                    //setSelection is added so that the cursor stays at the end when the hide button is pressed
                    editText_password.setSelection(editText_password.length());
                    clicked = true
                } else {
                    editText_password.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                    button_visiblity.setBackgroundResource(R.drawable.visible)
                    editText_password.setSelection(editText_password.length());
                    clicked = false
                }
            }
        }

        //Login button is clicked
        //Login Button onClick listener verifies the login details
        login_button.setOnClickListener {
            val username = editText_username.text.toString().trim()
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
                                        val i = Intent(applicationContext, MainActivity::class.java)
                                        startActivity(i)
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
    }
}