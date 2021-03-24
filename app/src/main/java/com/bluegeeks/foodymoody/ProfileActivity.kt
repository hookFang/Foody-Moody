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
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.authDb
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.rootDB
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    var firstName = ""
    var lastName = ""
    var birthDate = ""

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

            if (userInfo?.get("private") != false)
            {
                checkBoxPrivate.isChecked = true
            }

            if(userInfo?.get("photoURI") != "") {
                //Glide.with(this@ProfileActivity).load(BaseFirebaseProperties.imageRef.child("images/" + model.id + ".jpeg")).into(holder.itemView.ImageView_post);
                Glide.with(this).load(userInfo?.get("photoURI").toString()).into(imageView_profile_picture)
            }
        }

        Button_updateProfile.setOnClickListener {
            val alert = AlertDialog.Builder(this@ProfileActivity)
            val mView: View = layoutInflater.inflate(R.layout.profile_dialogue, null)
            val editText_firstName: EditText = mView.findViewById(R.id.EditText_firstName)
            val editText_lastName: EditText = mView.findViewById(R.id.EditText_lastName)
            val textView_birthDate: TextView = mView.findViewById(R.id.TextView_birthDate)
            val button_calender: Button = mView.findViewById(R.id.Button_calender)
            val button_calender_reset: Button = mView.findViewById(R.id.Button_calender_reset)
            val button_profile_update: Button = mView.findViewById(R.id.Button_profile_update)
            var date = ""
            alert.setView(mView)
            val alertDialog: AlertDialog = alert.create()

            editText_firstName.setText(firstName)
            editText_lastName.setText(lastName)
            textView_birthDate.text = birthDate

            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.show()

            button_calender.setOnClickListener {
                val now = Calendar.getInstance()
                val datePicker = DatePickerDialog(this, DatePickerDialog.OnDateSetListener {
                    view, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(Calendar.YEAR, year)
                    selectedDate.set(Calendar.MONTH, month)
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    date = format.format(selectedDate.time)
                    textView_birthDate.text = date
                },
                        now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH))

                datePicker.show()
            }

            button_calender_reset.setOnClickListener(View.OnClickListener() {
                date = ""
                textView_birthDate.text = date
            })

            button_profile_update.setOnClickListener(View.OnClickListener() {
                val first_name = editText_firstName.text.toString()
                val last_name = editText_lastName.text.toString()
                val birth_date = textView_birthDate.text.toString()
                val privateCheck = checkBoxPrivate.isChecked

                rootDB.collection("users").document(authDb.currentUser!!.uid)
                        .update(
                                mapOf(
                                        "firstName" to first_name,
                                        "lastName" to last_name,
                                        "birthDay" to birth_date,
                                        "private" to privateCheck
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

