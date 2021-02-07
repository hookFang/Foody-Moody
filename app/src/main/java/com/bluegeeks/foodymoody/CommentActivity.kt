package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluegeeks.foodymoody.BaseFirebaseProperties.Companion.authDb
import com.bluegeeks.foodymoody.BaseFirebaseProperties.Companion.rootDB
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.button_visiblity
import kotlinx.android.synthetic.main.item_comment.*
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.text.SimpleDateFormat
import java.util.*

class CommentActivity : AppCompatActivity() {

    private var adapter: CommentAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        val postId = intent.getStringExtra("postId")

        button_comment.setOnClickListener {
            try {
                // caoture inputs into on instance of our Restaurant class
                val comment = Comment()
                comment.userId = authDb.currentUser?.uid
                comment.comment = EditText_comment.text.toString().trim()
                comment.time = getTime()
                comment.postId = postId

                comment.id = rootDB.collection("comments").document().id
                rootDB.collection("comments").document(comment.id!!).set(comment)

                // show confirmation & clear inputs
                EditText_comment.setText("")

            } catch (e: Exception) {
                Toast.makeText(this, "Error" + " :" + e, Toast.LENGTH_LONG).show()
            }
        }

        commentsRecyclerView.layoutManager = LinearLayoutManager(this)

        val commentsQuery = rootDB.collection("comments").whereEqualTo("postId", postId).orderBy("time", Query.Direction.DESCENDING)

        val options =
                FirestoreRecyclerOptions.Builder<Comment>().setQuery(commentsQuery, Comment::class.java)
                        .build()

        adapter = CommentAdapter(options)

        commentsRecyclerView.adapter = adapter

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

    // tell adapter to start watching data for changes
    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        if (adapter != null) {
            adapter!!.stopListening()
        }
    }


    // create inner classes needed to bind the data to the recyclerview
    private inner class CommentViewHolder internal constructor(private val view: View) :
            RecyclerView.ViewHolder(view) {}

    private inner class CommentAdapter internal constructor(options: FirestoreRecyclerOptions<Comment>) :
            FirestoreRecyclerAdapter<Comment, CommentViewHolder>(options) {
        override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
        ): CommentViewHolder {

            val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CommentViewHolder(view)
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindViewHolder(
                holder: CommentViewHolder,
                position: Int,
                model: Comment
        ) {

            holder.itemView.TextView_user.text = model.userId
            holder.itemView.TextView_comment.text = model.comment!!

            if (model.whoLiked?.containsKey(authDb.currentUser!!.uid) == true &&
                    model.whoLiked?.get(authDb.currentUser!!.uid) == true) {

                holder.itemView.ImageView_like.setBackgroundResource(R.drawable.liked)
            } else {
                holder.itemView.ImageView_like.setBackgroundResource(R.drawable.heart)
            }

            holder.itemView.ImageView_like.setOnClickListener {

                if (model.whoLiked?.containsKey(authDb.currentUser!!.uid) == true &&
                        model.whoLiked?.get(authDb.currentUser!!.uid) == true) {

                    rootDB.collection("comments").document(model.id!!).update(

                        mapOf(
                                "whoLiked."+authDb.currentUser!!.uid to false
                        ))
                } else {

                    rootDB.collection("comments").document(model.id!!).update(

                        mapOf(
                                "whoLiked."+authDb.currentUser!!.uid to true
                        ))
                }
            }


            holder.itemView.TextView_comment.setOnClickListener {
                val intent = Intent(applicationContext, EditCommentActivity::class.java)
                intent.putExtra("commentId", model.id)
                intent.putExtra("comment", model.comment)
                intent.putExtra("postId", model.postId)
                startActivity(intent)
            }

            //holder.itemView.TextView_review.text = model.review.toString() // convert to float to match RatingBar.rating type


//            val spinner: Spinner = findViewById(R.id.spinner_review)
//            // Create an ArrayAdapter using the string array and a default spinner layout
//            ArrayAdapter.createFromResource(
//                    this,
//                    R.array.review,
//                    android.R.layout.simple_spinner_item
//            ).also { adapter ->
//                // Specify the layout to use when the list of choices appears
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                // Apply the adapter to the spinner
//                spinner.adapter = adapter
//            }
//
//            var review: String? = spinner.selectedItem as String
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")

        return sdf.format(Date())
    }
}