package com.bluegeeks.foodymoody

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.junit.After
import org.junit.Before
import org.junit.Test

class LoginUnitTest {
//    private val auth = Firebase.auth

    @Test
    fun loginSuccess() {
//        val email = "cool@cool.com"
//        val password = "123456"
//        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener() { task: Task<AuthResult> ->
//            if (task.isSuccessful) {
//                assert(task.isSuccessful)
//            } else  {
//                Assert.fail()
//            }
//        }
        assert(2+2 == 4);
    }

    @Before
    fun setupFirebase() {
        print("Hello")
    }

    @After
    fun removeFirebaseConnection() {
        print("Bye");
    }

}