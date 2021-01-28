package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.io.File
import java.io.FileInputStream


class HomeActivity : AppCompatActivity() {

    // FirebaseAuth instance
    private val authDb = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    // vars for profile photo
    private val REQUEST_CODE = 1000 // this is constant for the take picture intent in android
    private lateinit var filePhoto: File
    private val FILE_NAME = "photo.jpg"

    @SuppressLint("QueryPermissionsNeeded", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
//DLLKOMaqisb8vEivc5omCM4cLn33


        val loggedin_user = db.collection("users")
        var username = ""



        Toast.makeText(
                applicationContext,
                authDb.currentUser!!.uid.toString(),
                Toast.LENGTH_SHORT
        ).show()


        loggedin_user.whereEqualTo("id", authDb.currentUser!!.uid.toString()).get().addOnSuccessListener { user ->
            if (user != null) {
                user.getKey().equals("userName")
                username = user.getValue()
//                    if (user.id == authDb.currentUser!!.uid.toString()) {
                Log.d(TAG, "DocumentSnapshot data: ${user}")
//                Toast.makeText(
//                        applicationContext,
//                        authDb.currentUser!!.uid.toString(),
//                        Toast.LENGTH_SHORT
//                ).show()
            } else {
                Log.d("userName", "No such document")
            }
        }
        .addOnFailureListener { exception ->
            Log.d("userName", "get failed with ", exception)
        }

        //populate the textviews based on the current user's profile
        if (authDb.currentUser != null) {
//            textView_name.text = authDb.currentUser!!.displayName
//
//            if (!username.equals("")) {
//                textView_name.text = username
//                Toast.makeText(this, "YAYA", Toast.LENGTH_LONG).show()
//
//            }


//            users.document(auth.currentUser!!.uid).get()
//                    .addOnSuccessListener { user ->
//                        if (user.exists()) {
//                            textView_name.text = user.firstName
////                            textView_name.text = user.firstName + ' ' + user.lastName
//                        }
//                    }
            //textView_name.text = auth.currentUser!!.uid

//            // display photoUri if any
//            var profilePhoto: Uri? = authDb.currentUser!!.photoUrl
//            if (profilePhoto != null) {
//                profilePhoto.path?.let {
//                    loadProfileImage(it)
//                }
//            }
        } else {
            logout()
        }

        imageView_profile_picture.setImageResource(R.drawable.logout)

        imageView_profile_picture.setOnClickListener {
            startActivity(Intent(applicationContext, PersonalActivity::class.java))
        }

        //instantiate toolbar
        setSupportActionBar(topToolbar)
    }

    // 2 overrides to display menu and handle its action
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        // inflate the main menu to add the items to the toolbar
        menuInflater.inflate(R.menu.menu_bar_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // navigate based on which menu item was clicked
        when (item.itemId) {
            R.id.action_add -> {
                startActivity(Intent(applicationContext, HomeActivity::class.java))
                return true
            }
            R.id.action_logout -> {
                logout()
                return true
            }
            R.id.action_notification -> {
                startActivity(Intent(applicationContext, HomeActivity::class.java))
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    // load profile image if any
    private fun loadProfileImage(path: String) {
        var file: File = File(path)

        // convert to bitmap
        var bitmapImage = BitmapFactory.decodeStream(FileInputStream(file))
        // display in Image View
        imageView_profile_picture.setImageBitmap(bitmapImage)
    }

    private fun logout() {
        authDb.signOut()
        finish()

        //reload the login
        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
    }
}

