package com.bluegeeks.foodymoody

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties
import com.bluegeeks.foodymoody.entity.Post
import com.bluegeeks.foodymoody.entity.URIPathHelper
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.item_post.view.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PostActivity : BaseFirebaseProperties() {

    //bitmap image fore the post
    var imageURI: Uri? = null
    var videoURI: Uri? = null
    lateinit var currentPhotoPath: String

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        val successMessage = "Post submitted successfully"
        val unSuccessMessage = "Error, Cannot Post"

        // Code referred from - https://medium.com/@manuaravindpta/fetching-contacts-from-device-using-kotlin-6c6d3e76574f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (checkSelfPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) && checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //So if we don't have permission we request for permissions from the user, this will execute the overridden onRequestPermissionsResult
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                1001
            )
            //callback onRequestPermissionsResult
        }

        //Pick image from gallery or photo
        imageView_post.setOnClickListener {
            choosePost()
        }

        videoView_post.setOnClickListener {
            choosePost()
        }


        button_post.setOnClickListener {
            if (imageView_post.drawable != null && editText_description.text.isNotEmpty()) {
                progressBar_PostPage.visibility = View.VISIBLE
                rootDB.collection("users").document(authDb.currentUser!!.uid).get()
                    .addOnCompleteListener { user ->
                        if (user.isSuccessful) {
                            val userInfo = user.result
                            if (userInfo != null) {
                                val sharedWithUsersTemp =
                                    userInfo.get("followers") as ArrayList<String>
                                sharedWithUsersTemp.add(authDb.currentUser!!.uid)
                                try {
                                    val post = Post()
                                    post.userId = authDb.currentUser!!.uid
                                    post.userFullName = userInfo.get("userName") as String?
                                    post.description = editText_description.text.toString().trim()
                                    post.time = getTime()
                                    post.postIsPhoto = true
                                    post.review = hashMapOf(
                                        "Yummy" to ArrayList<String>(),
                                        "Sweet" to ArrayList<String>(),
                                        "Sour" to ArrayList<String>(),
                                        "Salty" to ArrayList<String>(),
                                        "Bitter" to ArrayList<String>(),
                                        "Like" to ArrayList<String>()
                                    )
                                    post.sharedWithUsers = sharedWithUsersTemp
                                    post.id = rootDB.collection("posts").document().id
                                    rootDB.collection("posts").document(post.id!!).set(post)
                                    rootDB.collection("users").document(authDb.currentUser!!.uid)
                                        .update("postsID", (FieldValue.arrayUnion(post.id)))

                                    //Upload the image to firebase storage
                                    val imagesRef: StorageReference =
                                        imageRef.child("images/" + post.id + ".jpeg")
                                    val uploadTask: UploadTask? = imagesRef.putFile(imageURI!!)
                                    uploadTask?.addOnFailureListener {
                                        Toast.makeText(this, unSuccessMessage, Toast.LENGTH_LONG)
                                            .show()
                                        progressBar_PostPage.visibility = View.GONE
                                    }?.addOnSuccessListener {
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
            } else if (videoURI != null && editText_description.text.isNotEmpty()) {
                progressBar_PostPage.visibility = View.VISIBLE
                rootDB.collection("users").document(authDb.currentUser!!.uid).get()
                    .addOnCompleteListener { user ->
                        if (user.isSuccessful) {
                            val userInfo = user.result
                            if (userInfo != null) {
                                val sharedWithUsersTemp =
                                    userInfo.get("followers") as ArrayList<String>
                                sharedWithUsersTemp.add(authDb.currentUser!!.uid)
                                try {
                                    val post = Post()
                                    post.userId = authDb.currentUser!!.uid
                                    post.userFullName = userInfo.get("userName") as String?
                                    post.description = editText_description.text.toString().trim()
                                    post.time = getTime()
                                    post.review = hashMapOf(
                                        "Yummy" to ArrayList<String>(),
                                        "Sweet" to ArrayList<String>(),
                                        "Sour" to ArrayList<String>(),
                                        "Salty" to ArrayList<String>(),
                                        "Bitter" to ArrayList<String>()
                                    )
                                    post.sharedWithUsers = sharedWithUsersTemp
                                    post.postIsPhoto = false
                                    post.id = rootDB.collection("posts").document().id
                                    rootDB.collection("posts").document(post.id!!).set(post)
                                    rootDB.collection("users").document(authDb.currentUser!!.uid)
                                        .update("postsID", (FieldValue.arrayUnion(post.id)))

                                    //Upload the image to firebase storage
                                    val videoRef: StorageReference =
                                        videoRef.child("videos/" + post.id)
                                    val uploadTask: UploadTask = videoRef.putFile(videoURI!!)
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
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Please select a image and add a description !",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
            }
        }
        //To play/pause video
        videoView_post.setOnClickListener {
            if (videoView_post.isPlaying) {
                videoView_post.pause()
            } else {
                videoView_post.start()
            }
        }
        //instantiate toolbar
        setSupportActionBar(topToolbar)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1001) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this,
                    "Cannot access storage !",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
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
            imageURI = data.data
            videoView_post.visibility = View.GONE
            imageView_post.visibility = View.VISIBLE
            imageView_post.setImageURI(imageURI)
        } else if (requestCode == 1002) {
            imageURI = Uri.fromFile(File(currentPhotoPath))
            videoView_post.visibility = View.GONE
            imageView_post.visibility = View.VISIBLE
            imageView_post.setImageURI(imageURI)
        }  else if (requestCode == 1003 && data != null) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(this, data.data)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val timeInSec = time!!.toLong()/1000
            retriever.release()
            val uriPathHelper = URIPathHelper()
            val videoFullPath =
                    data.data?.let { uriPathHelper.getPath(this, it) } // Use this video path according to your logic
            val file = File(videoFullPath)
            val fileSizeInMB = (file.length() / 1024)/ 1024
            if(fileSizeInMB <= 10 && timeInSec <= 20) {
                videoURI = data.data!!
                imageView_post.visibility = View.GONE
                videoView_post.visibility = View.VISIBLE
                videoView_post.setVideoURI(data.data)
                videoView_post.start()
            } else {
                Toast.makeText(this, "Please Select a video that is less than 10MB and 20 seconds", Toast.LENGTH_LONG)
                        .show()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
        val currentDateandTime: String = sdf.format(Date())
        return currentDateandTime
    }

    //This function is used to create a android dialog box and select the option
    private fun choosePost() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Choose Video From Gallery", "Cancel")
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Select an Option")
        builder.setItems(options) { dialog, item ->
            when (options[item]) {
                "Take Photo" -> {
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                        // Ensure that there's a camera activity to handle the intent
                        takePictureIntent.resolveActivity(packageManager)?.also {
                            // Create the File where the photo should go
                            val photoFile: File? = try {
                                createImageFile()
                            } catch (ex: IOException) {
                                Log.e("ERROR", ex.message.toString())
                                null
                            }
                            // Continue only if the File was successfully created
                            photoFile?.also {
                                val photoURI: Uri = FileProvider.getUriForFile(
                                        this,
                                        "com.example.android.fileprovider",
                                        it
                                )
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                                startActivityForResult(takePictureIntent, 1002)
                            }
                        }
                    }
                }
                "Choose from Gallery" -> {
                    val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(pickPhoto, 1001)
                }
                "Choose Video From Gallery" -> {
                    val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(pickPhoto, 1003)
                }
                "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }
}