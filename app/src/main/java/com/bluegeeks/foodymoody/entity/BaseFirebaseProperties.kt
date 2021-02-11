package com.bluegeeks.foodymoody.entity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.InputStream


open class BaseFirebaseProperties: AppCompatActivity() {
    //We use companion object here because we want these objects to be static
    companion object {
        // FirebaseAuth instance
        val authDb = FirebaseAuth.getInstance()
        val rootDB = FirebaseFirestore.getInstance()
        val imageRef = FirebaseStorage.getInstance().reference
    }
}