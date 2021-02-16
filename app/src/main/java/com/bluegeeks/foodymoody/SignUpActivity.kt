package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.authDb
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.rootDB
import com.bluegeeks.foodymoody.entity.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.toolbar_signup.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class SignUpActivity : AppCompatActivity() {

    //Code referred from https://firebase.google.com/docs/auth/android/password-auth#create_a_password-based_account
    var PASSWORD_REGEX = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$".toRegex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        //Action bar to get the back button
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Sign Up"

        //Sign Up button onclick -  saves new users to database
        signUpButton.setOnClickListener {
            val email = signUpEmail.text.toString().trim()
            val password = signUpPassword.text.toString().trim()
            val confirmPassword = signUpConfirmPassword.text.toString().trim()
            val firstName = ""
            val lastName = ""
            val userName = signUpUsername.text.toString().trim()
            val birthDay = ""
            val photoURI = ""
            val followers = ArrayList<String>()
            val following = ArrayList<String>()
            val postsID = ArrayList<String>()
            val bio = ""
            val time = getTime()

            rootDB.collection("users").whereEqualTo("userName", userName).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (!(document?.isEmpty!!)) {
                        Log.i("TAG", "Username already exist")
                        Toast.makeText(this, "A user with same username already exist. Please use a different username", Toast.LENGTH_LONG).show()
                    } else {
                        if (checkValues(email, userName, password, confirmPassword)) {
                            if (PASSWORD_REGEX.matches(password)) {
                                //Code referred from https://firebase.google.com/docs/auth/android/password-auth#create_a_password-based_account
                                authDb.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener() { task: Task<AuthResult> ->
                                            if (task.isSuccessful) {
                                                FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                                                //Show confirmation and clear inputs
                                                Toast.makeText(this, "A confirmation e-mail has been send.", Toast.LENGTH_LONG).show()
                                                //A user variable is created and added to the db collection
                                                val user =
                                                    User(
                                                        authDb.currentUser?.uid,
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
                                                        time
                                                    )
                                                rootDB.collection("users").document(user.id!!).set(user)
                                                finish()
                                            } else {
                                                try {
                                                    throw task.exception!!
                                                } catch (e: FirebaseAuthWeakPasswordException) {
                                                    Toast.makeText(this, "The password you entered is weak, please try again with a new password", Toast.LENGTH_LONG).show()
                                                } catch (e: FirebaseAuthUserCollisionException) {
                                                    Toast.makeText(this, "The email address is already in use by another account.", Toast.LENGTH_LONG).show()
                                                } catch (e: Exception) {
                                                    Log.e("TAG", e.message!!)
                                                }
                                            }
                                        }
                            } else {
                                Toast.makeText(this, "Your password must contain Minimum eight characters, at least one uppercase letter, \n one lowercase letter, one number and one special character:", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this, "Please make sure the fields are all filled in", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Log.i("TAG", "get failed with ", task.exception)
                    Toast.makeText(this, "Please try again", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    //This is the action for back button
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    //Function to check if all values are filled in TODO- will be implementing features to check if the same username exists
    private fun checkValues(email: String, userName: String, password: String, confirmPassword: String): Boolean {
        if (email.isNotEmpty() && userName.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && (confirmPassword == password)) {
            return true
        }
        return false
    }

    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
        return sdf.format(Date())
    }
}