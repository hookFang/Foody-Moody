package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.bluegeeks.foodymoody.BaseFirebaseProperties.Companion.rootDB
import kotlinx.android.synthetic.main.activity_edit_comment.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.text.SimpleDateFormat
import java.util.*

class EditCommentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_comment)

        val commentId = intent.getStringExtra("commentId")
        val comment = intent.getStringExtra("comment")
        val postId = intent.getStringExtra("postId")

        editText_comment.setText(comment.toString())
        editText_comment.setSelection(editText_comment.length());

        val unSuccessMessage_information = "Enter new information"
        val unSuccessMessage_error = "Comment did not updated"

        button_update_comment.setOnClickListener {
            try {
                var newComment = editText_comment.text.toString()
                if (newComment.isNotEmpty() && comment != newComment) {
                    rootDB.collection("comments").document(commentId!!)
                        .update(
                            mapOf(
                                "comment" to newComment,
                                "time" to getTime()
                            )
                        )
                        .addOnSuccessListener {
                            val intent = Intent(applicationContext, CommentActivity::class.java)
                            intent.putExtra("postId", postId)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                applicationContext,
                                unSuccessMessage_error,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        applicationContext,
                        unSuccessMessage_information,
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this, "Error :" + e, Toast.LENGTH_LONG).show()
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
                finish()
                startActivity(Intent(applicationContext, CommentActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")

        return sdf.format(Date())
    }
}