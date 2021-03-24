package com.bluegeeks.foodymoody

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_retrieve_password.*
import kotlinx.android.synthetic.main.toolbar_signup.*

class RetrievePasswordActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retrieve_password)

        mAuth = FirebaseAuth.getInstance()

        //Action bar to get the back button
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Forgot Password?"

        //User Has Forgotten Password and clicks "Forgot Password?"
        //Retrieve password code taken from https://grokonez.com/android/kotlin-firebase-authentication-send-reset-password-email-forgot-password-android#43_ResetPasswordActivity
        RetrievePasswordButton.setOnClickListener {

            val email = RetrievalEmailText.text.toString().trim()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(applicationContext, "Enter your email!", Toast.LENGTH_SHORT).show()
            } else {
                mAuth!!.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@RetrievePasswordActivity, "Check email to reset your password!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@RetrievePasswordActivity, "Fail to send reset password email!", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    //This is the action for back button
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}