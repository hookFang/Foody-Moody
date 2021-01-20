package com.bluegeeks.foodymoody

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var clicked = false

        button_visiblity.setOnClickListener {
            if(editText_password.text.toString().isNotEmpty()) {
                if (!clicked) {
                    editText_password.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                    button_visiblity.setBackgroundResource(R.drawable.invisible)
                    clicked = true
                } else {
                    editText_password.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                    button_visiblity.setBackgroundResource(R.drawable.visible)
                    clicked = false
                }
            }
        }
    }
}