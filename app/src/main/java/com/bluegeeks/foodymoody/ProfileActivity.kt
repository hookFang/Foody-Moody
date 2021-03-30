package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.authDb
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.rootDB
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.checkBoxFollowListIsVisible
import kotlinx.android.synthetic.main.activity_profile.checkBoxPrivate
import kotlinx.android.synthetic.main.activity_profile.imageView_profile_picture
import kotlinx.android.synthetic.main.profile_security_dialogue.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    var firstName = ""
    var lastName = ""
    var birthDate = ""
    var security = false
    var private = false

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
                //Glide.with(this@ProfileActivity).load(BaseFirebaseProperties.imageRef.child("images/" + model.id + ".jpeg")).into(holder.itemView.ImageView_post);
                Glide.with(this).load(userInfo?.get("photoURI").toString()).into(imageView_profile_picture)
            }
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

                rootDB.collection("users").document(authDb.currentUser!!.uid)
                        .update(
                                mapOf(
                                        "firstName" to firstName,
                                        "lastName" to lastName,
                                        "birthDay" to birthDate
                                )
                        )
                        .addOnSuccessListener {
                            val intent = Intent(applicationContext, ProfileActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
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
}

