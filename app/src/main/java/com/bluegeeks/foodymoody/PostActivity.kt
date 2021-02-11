package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class PostActivity : BaseFirebaseProperties() {

    //bitmap image fore the post
    var bitmapImage: Bitmap? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        val successMessage = "Post submitted successfully"
        val unSuccessMessage = "Error, Cannot Post"

        //Pick image from gallery or photo
        imageView_post.setOnClickListener {
            val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")

            val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Select an Option")
            builder.setItems(options) { dialog, item ->
                when (options[item]) {
                    "Take Photo" -> {
                        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(takePicture, 1002)
                    }
                    "Choose from Gallery" -> {
                        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(pickPhoto, 1001)
                    }
                    "Cancel" -> {
                        dialog.dismiss()
                    }
                }
            }
            builder.show()
        }

        button_post.setOnClickListener {
            progressBar_PostPage.visibility = View.VISIBLE
            var userName = ""

            rootDB.collection("users").document(authDb.currentUser!!.uid).get().addOnCompleteListener { user ->
                if(user.isSuccessful){
                    val userInfo = user.result
                    if (userInfo != null) {
                        if (userInfo.get("firstName") != null && userInfo.get("lastName") != null) {
                            userName = userInfo.get("firstName").toString() + " " + userInfo.get("lastName").toString()
                        } else if (userInfo.get("firstName") != null) {
                            userName = userInfo.get("firstName").toString()
                        } else if (userInfo.get("lastName") != null) {
                            userName = userInfo.get("lastName").toString()
                        } else if (userInfo.get("fullName") != null) {
                            userName = userInfo.get("fullName").toString()
                        } else if (userInfo.get("userName") != null) {
                            userName = userInfo.get("uerName").toString()
                        }
                    } else {
                        userName = authDb.currentUser!!.uid
                    }
                }
            }
            try {
                    val post = Post()
                    post.userId = authDb.currentUser!!.uid
                    post.userFullName = userName
                    post.description = editText_description.text.toString().trim()
                    post.time = getTime()
                    post.id = rootDB.collection("posts").document().id
                    rootDB.collection("posts").document(post.id!!).set(post)

                    //Upload the image to firebase storage
                    val imagesRef : StorageReference = imageRef.child("images/" + post.id + ".jpeg")
                    val baos = ByteArrayOutputStream()
                    bitmapImage?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()
                    val uploadTask: UploadTask = imagesRef.putBytes(data)
                    uploadTask.addOnFailureListener {
                        Toast.makeText(this, unSuccessMessage, Toast.LENGTH_LONG).show()
                        progressBar_PostPage.visibility = View.GONE
                    }.addOnSuccessListener {
                        Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()
                        progressBar_PostPage.visibility = View.GONE
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e("TAG", e.message!!)
                    Toast.makeText(this, unSuccessMessage , Toast.LENGTH_LONG).show()
                    progressBar_PostPage.visibility = View.GONE
                    finish()
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
                startActivity(Intent(applicationContext, HomeActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        authDb.signOut()
        finish()
        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
    }

    //To get the image from the gallery
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && data != null) {
            val selectedImage: Uri? = data.data
            bitmapImage = selectedImage?.let { ImageDecoder.createSource(this.contentResolver, it) }?.let { ImageDecoder.decodeBitmap(it) }
            imageView_post.setImageBitmap(bitmapImage)
        } else if (requestCode == 1002 && data != null) {
            bitmapImage = data.extras?.get("data") as Bitmap
            print(bitmapImage)
            imageView_post.setImageBitmap(bitmapImage)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
        val currentDateandTime: String = sdf.format(Date())
        return currentDateandTime
    }
}