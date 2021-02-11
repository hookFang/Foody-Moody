package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.authDb
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.rootDB
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.toolbar_main.*


class ProfileActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        rootDB.collection("users").document(authDb.currentUser!!.uid).get().addOnCompleteListener { userInfo ->
            Toast.makeText(this, userInfo.result?.get("email").toString(), Toast.LENGTH_LONG).show()
            if (userInfo.result?.get("userName") != "" && userInfo.result?.get("userName") != null) {
                TextView_username.text = userInfo.result?.get("userName").toString()
            }
            if (userInfo.result?.get("firstName") != "" && userInfo.result?.get("firstName") != null && userInfo.result?.get("lastName") != "" && userInfo.result?.get("lastName") != null) {
                TextView_fullname.text = userInfo.result?.get("firstName").toString() + " " + userInfo.result?.get("lastName").toString()
            } else if (userInfo.result?.get("lastName") != "" && userInfo.result?.get("lastName") != null) {
                TextView_fullname.text = userInfo.result?.get("lastName").toString()
            } else if (userInfo.result?.get("firstName") != "" && userInfo.result?.get("firstName") != null) {
                TextView_fullname.text = userInfo.result?.get("firstName").toString()
            }
            if (userInfo.result?.get("fullName") != "" && userInfo.result?.get("fullName") != null) {
                TextView_fullname.text = userInfo.result?.get("fullName").toString()
            }

            if (userInfo.result?.get("email") != "" && userInfo.result?.get("email") != null) {
                TextView_email.text = userInfo.result?.get("email").toString()
            }
            if (userInfo.result?.get("birthDay") != "" && userInfo.result?.get("birthDay") != null) {
                TextView_birthday.text = userInfo.result?.get("birthDay").toString()
            }
        }

        //instantiate toolbar
        setSupportActionBar(topToolbar)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bar_back, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_back -> {
                finish()
                startActivity(Intent(applicationContext, PersonalActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

