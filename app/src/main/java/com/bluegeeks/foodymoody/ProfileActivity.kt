package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.authDb
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.rootDB
import com.bluegeeks.foodymoody.entity.URIPathHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_personal.*
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.checkBoxFollowListIsVisible
import kotlinx.android.synthetic.main.activity_profile.checkBoxPrivate
import kotlinx.android.synthetic.main.activity_profile.imageView_profile_picture
import kotlinx.android.synthetic.main.profile_security_dialogue.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    var firstName = ""
    var lastName = ""
    var birthDate = ""
    var security = false
    var private = false
    lateinit var imageURI: Uri
    lateinit var currentPhotoPath: String


    @SuppressLint("WeekBasedYear")
    var format = SimpleDateFormat("dd MMM, YYYY", Locale.US)

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        rootDB.collection("users").document(authDb.currentUser!!.uid).get().addOnCompleteListener { user ->
            val userInfo = user.result
            firstName = userInfo?.get("firstName").toString()
            lastName = userInfo?.get("lastName").toString()
            birthDate = userInfo?.get("birthDay").toString()

            if (userInfo?.get("userName") != "" && userInfo?.get("userName") != null) {
                TextView_username.text = userInfo.get("userName").toString()
            }
            if (firstName != "" && lastName != "") {
                TextView_fullname.text = firstName + " " + lastName
            } else if (lastName != "") {
                TextView_fullname.text = lastName
            } else if (firstName != "") {
                TextView_fullname.text = firstName
            }

            if (userInfo?.get("email") != "" && userInfo?.get("email") != null) {
                TextView_email.text = userInfo.get("email").toString()
            }
            if (birthDate != "") {
                TextView_birthday.text = birthDate
            }

            if (userInfo?.get("followListIsVisible") != false) {
                checkBoxFollowListIsVisible.isChecked = true
                security = true
            }

            if (userInfo?.get("private") != false) {
                checkBoxPrivate.isChecked = true
                private = true
            }

            if(userInfo?.get("photoURI") != "") {
                val tempPhotoUri: String = userInfo?.get("photoURI") as String
                val photoTemp = tempPhotoUri.split(".")
                if(photoTemp[0] == authDb.currentUser!!.uid) {
                    Glide.with(this)
                        .load(BaseFirebaseProperties.imageRef.child(
                            "profilePictures/$tempPhotoUri"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(imageView_profile_picture)
                } else {
                    Glide.with(this).load(userInfo.get("photoURI").toString())
                            .into(imageView_profile_picture)
                }
            }
        }

        //Update  User profile
        imageView_profile_picture.setOnClickListener {
            chooseProfilePhoto()
        }

        Button_updateProfile.setOnClickListener {
            val alert = AlertDialog.Builder(this@ProfileActivity)
            val profileUpdating: View = layoutInflater.inflate(R.layout.profile_dialogue, null)
            val editTextFirstName: EditText = profileUpdating.findViewById(R.id.EditText_firstName)
            val editTextLastName: EditText = profileUpdating.findViewById(R.id.EditText_lastName)
            val textViewBirthDate: TextView = profileUpdating.findViewById(R.id.TextView_birthDate)
            val buttonCalender: Button = profileUpdating.findViewById(R.id.Button_calender)
            val buttonCalenderReset: Button = profileUpdating.findViewById(R.id.Button_calender_reset)
            val buttonProfileUpdate: Button = profileUpdating.findViewById(R.id.Button_profile_update)
            var date = ""
            alert.setView(profileUpdating)
            val alertDialog: AlertDialog = alert.create()

            editTextFirstName.setText(firstName)
            editTextLastName.setText(lastName)
            textViewBirthDate.text = birthDate

            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.show()

            buttonCalender.setOnClickListener {
                val now = Calendar.getInstance()
                val datePicker = DatePickerDialog(this, DatePickerDialog.OnDateSetListener {
                    view, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(Calendar.YEAR, year)
                    selectedDate.set(Calendar.MONTH, month)
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    date = format.format(selectedDate.time)
                    buttonCalender.text = date
                },
                        now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH))

                datePicker.show()
            }

            buttonCalenderReset.setOnClickListener(View.OnClickListener() {
                date = ""
                textViewBirthDate.text = date
            })

            buttonProfileUpdate.setOnClickListener(View.OnClickListener() {
                val firstName = editTextFirstName.text.toString()
                val lastName = editTextLastName.text.toString()
                val birthDate = textViewBirthDate.text.toString()
                val profilePhotoPath = authDb.currentUser!!.uid + ".jpeg"
                val imagesRef: StorageReference =
                    BaseFirebaseProperties.imageRef.child("profilePictures/$profilePhotoPath")
                imagesRef.delete()
                val uploadTask: UploadTask? = imagesRef.putFile(imageURI!!)
                uploadTask?.addOnFailureListener {
                    Toast.makeText(this, "Failed to update Profile", Toast.LENGTH_LONG)
                        .show()
                }?.addOnSuccessListener {
                    rootDB.collection("users").document(authDb.currentUser!!.uid)
                        .update(
                            mapOf(
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "birthDay" to birthDate,
                                "photoURI" to profilePhotoPath
                            )
                        )
                        .addOnSuccessListener {
                            alertDialog.dismiss()
                            val intent = Intent(applicationContext, ProfileActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
                        }
                }
            })
        }

        Button_updateSecurity.setOnClickListener {

            val alert = AlertDialog.Builder(this@ProfileActivity)
            val securityUpdating: View = layoutInflater.inflate(R.layout.profile_security_dialogue, null)
            val privateCheck: CheckBox = securityUpdating.findViewById(R.id.checkBoxPrivate)
            val privateSecurityCheck: CheckBox = securityUpdating.findViewById(R.id.checkBoxFollowListIsVisible)
            val buttonProfileSecurityUpdate: Button = securityUpdating.findViewById(R.id.Button_profile_security_update)

            if (private) {
                privateCheck.isChecked = true
            }

            if (security) {
                privateSecurityCheck.isChecked = true
            }
            alert.setView(securityUpdating)
            val alertDialog: AlertDialog = alert.create()

            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.show()

            buttonProfileSecurityUpdate.setOnClickListener(View.OnClickListener() {

                rootDB.collection("users").document(authDb.currentUser!!.uid)
                    .update(
                        mapOf(
                            "private" to privateCheck.isChecked,
                            "followListIsVisible" to privateSecurityCheck.isChecked
                        )
                    )
                    .addOnSuccessListener {
                        private = privateCheck.isChecked
                        security = privateSecurityCheck.isChecked
                        val intent = Intent(applicationContext, ProfileActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
                    }
            })
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

    //To get the image from the gallery
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && data != null) {
            imageURI = data.data!!
            imageView_profile_picture.setImageDrawable(null)
            imageView_profile_picture.setImageURI(imageURI)
        } else if (requestCode == 1002) {
            imageURI = Uri.fromFile(File(currentPhotoPath))
            imageView_profile_picture.setImageDrawable(null)
            imageView_profile_picture.setImageURI(imageURI)
        }
    }

    //This function is used to create a android dialog box and select the option
    @SuppressLint("QueryPermissionsNeeded")
    private fun chooseProfilePhoto() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
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
                "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    @SuppressLint("SimpleDateFormat")
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

