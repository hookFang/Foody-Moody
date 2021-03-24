package com.bluegeeks.foodymoody.entity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


open class BaseFirebaseProperties: AppCompatActivity() {
    //We use companion object here because we want these objects to be static
    companion object {
        // FirebaseAuth instance
        val authDb = FirebaseAuth.getInstance()
        @SuppressLint("StaticFieldLeak")
        val rootDB = FirebaseFirestore.getInstance()
        val imageRef = FirebaseStorage.getInstance().reference
        val realtimeDB = FirebaseDatabase.getInstance()
    }
}