package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.item_post.view.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : BaseFirebaseProperties() {
        private var adapter: PostAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (authDb.currentUser != null) {
            if(authDb.currentUser!!.displayName != null) {
                textView_name.text = authDb.currentUser!!.displayName
            }
        }

        imageView_profile_picture.setImageResource(R.drawable.logout)
        imageView_profile_picture.setOnClickListener {
            startActivity(Intent(applicationContext, PersonalActivity::class.java))
        }

        val postsQuery = rootDB.collection("posts").orderBy("time", Query.Direction.DESCENDING)
        // set our recyclerview to use LinearLayout
        postsRecyclerView.layoutManager = LinearLayoutManager(this)
        val options =
                FirestoreRecyclerOptions.Builder<Post>().setQuery(postsQuery, Post::class.java)
                        .build()
        adapter = PostAdapter(options)
        postsRecyclerView.adapter = adapter
        //instantiate toolbar
        setSupportActionBar(topToolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bar_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_add -> {
                startActivity(Intent(applicationContext, PostActivity::class.java))
                return true
            }
            R.id.action_logout -> {
                logout()
                return true
            }
            R.id.action_notification -> {
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

    private fun logout() {
        authDb.signOut()
        finish()
        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
    }


    // create inner classes needed to bind the data to the recyclerview
    private inner class PostViewHolder internal constructor(private val view: View) :
            RecyclerView.ViewHolder(view) {}

    private inner class PostAdapter internal constructor(options: FirestoreRecyclerOptions<Post>) :
            FirestoreRecyclerAdapter<Post, PostViewHolder>(options) {
        override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
        ): PostViewHolder {

            val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
            return PostViewHolder(view)
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindViewHolder(
                holder: PostViewHolder,
                position: Int,
                model: Post
        ) {

            val dateStart = model.time
            val dateStop = getTime()

            val format = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")

            var d1: Date? = null
            var d2: Date? = null
            var time: String? = null
            var user_name: String? = null

            try {
                d1 = format.parse(dateStart);
                d2 = format.parse(dateStop);
                val diff: Long = d2.getTime() - d1.getTime()
                val seconds = diff.toInt() / 1000
                val minutes = seconds / 60
                val hours = minutes / 60
                val days = hours / 24

                if (days >= 1) {
                    time = days.toString() + " Day(s) ago"
                } else if (hours >= 1) {
                    time = hours.toString() + " Hour(s) ago"
                } else if (minutes >= 1) {
                    time = minutes.toString() + " Minute(s) ago"
                } else if (seconds >= 1) {
                    time = seconds.toString() + " Second(s) ago"
                }
            } catch  (e: Exception) {
                e.printStackTrace();
            }

            Glide.with(this@HomeActivity).load(imageRef.child("images/" + model.id + ".jpeg")).into(holder.itemView.ImageView_post);
            holder.itemView.textView_time.text = time
            holder.itemView.TextView_name.text = model.userId!!
            holder.itemView.TextView_description.text = model.description // convert to float to match RatingBar.rating type


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


            holder.itemView.imageView_comment.setOnClickListener {
                val intent = Intent(applicationContext, CommentActivity::class.java)
                intent.putExtra("postId", model.id)
                startActivity(intent)
            }

        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")

        return sdf.format(Date())
    }
}

