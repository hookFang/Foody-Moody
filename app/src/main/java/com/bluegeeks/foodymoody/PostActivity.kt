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
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties
import com.bluegeeks.foodymoody.entity.Post
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


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
            if (imageView_post.drawable != null && editText_description.text.isNotEmpty()) {
                progressBar_PostPage.visibility = View.VISIBLE
                rootDB.collection("users").document(authDb.currentUser!!.uid).get()
                    .addOnCompleteListener { user ->
                        if (user.isSuccessful) {
                            val userInfo = user.result
                            if (userInfo != null) {
                                val sharedWithUsersTemp = userInfo.get("followers") as ArrayList<String>
                                sharedWithUsersTemp.add(authDb.currentUser!!.uid)
                                try {
                                    val post =  Post()
                                    post.userId = authDb.currentUser!!.uid
                                    post.userFullName = userInfo.get("userName") as String?
                                    post.description = editText_description.text.toString().trim()
                                    post.time = getTime()
                                    post.review = hashMapOf("Yummy" to ArrayList<String>(), "Sweet" to ArrayList<String>(),
                                            "Sour" to ArrayList<String>(), "Salty" to ArrayList<String>(), "Bitter"  to ArrayList<String>(),
                                            "Like"  to ArrayList<String>())
                                    post.sharedWithUsers = sharedWithUsersTemp
                                    post.id = rootDB.collection("posts").document().id
                                    rootDB.collection("posts").document(post.id!!).set(post)
                                    rootDB.collection("users").document(authDb.currentUser!!.uid).update("postsID", (FieldValue.arrayUnion(post.id)))

                                    //Upload the image to firebase storage
                                    val imagesRef: StorageReference =
                                        imageRef.child("images/" + post.id + ".jpeg")
                                    val baos = ByteArrayOutputStream()
                                    bitmapImage?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                                    val data = baos.toByteArray()
                                    val uploadTask: UploadTask = imagesRef.putBytes(data)
                                    uploadTask.addOnFailureListener {
                                        Toast.makeText(this, unSuccessMessage, Toast.LENGTH_LONG)
                                            .show()
                                        progressBar_PostPage.visibility = View.GONE
                                    }.addOnSuccessListener {
                                        Toast.makeText(this, successMessage, Toast.LENGTH_LONG)
                                            .show()
                                        progressBar_PostPage.visibility = View.GONE
                                        finish()
                                    }
                                } catch (e: Exception) {
                                    Log.e("TAG", e.message!!)
                                    Toast.makeText(this, unSuccessMessage, Toast.LENGTH_LONG).show()
                                    progressBar_PostPage.visibility = View.GONE
                                    finish()
                                }
                            }
                        }
                    }
            }  else {
                Toast.makeText(this, "Please select a image and add a description !", Toast.LENGTH_LONG).show()
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