package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties
import com.bluegeeks.foodymoody.entity.Post
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.item_post.*
import kotlinx.android.synthetic.main.item_post.view.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : BaseFirebaseProperties() {

    private var adapter: PostAdapter? = null
    var oldSize: Int = 0
    var targetId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val postsQuery = rootDB.collection("posts").orderBy("time", Query.Direction.DESCENDING).whereArrayContains("sharedWithUsers", authDb.currentUser!!.uid)
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
            R.id.action_personal -> {
                startActivity(Intent(applicationContext, PersonalActivity::class.java))
                return true
            }
            R.id.action_notification -> {
                startActivity(Intent(applicationContext, NotificationActivity::class.java))
                return true
            }
            R.id.action_serach -> {
                startActivity(Intent(applicationContext, SearchActivity::class.java))
                return true
            }
            R.id.action_message -> {
                startActivity(Intent(applicationContext, AllChatActivity::class.java))
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    // tell adapter to start watching data for changes
    override fun onPause() {
        adapter?.stopListening()
        super.onPause()
    }
    override fun onResume() {
        super.onResume()
        adapter?.startListening()
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
                } else if (seconds >= 10) {
                    time = seconds.toString() + " Second(s) ago"
                } else if (seconds < 10) {
                    time = "Recently"
                }
            } catch (e: Exception) {
                e.printStackTrace();
            }

            Glide.with(this@HomeActivity).load(imageRef.child("images/" + model.id + ".jpeg")).into(holder.itemView.ImageView_post);
            holder.itemView.textView_time.text = time
            holder.itemView.TextView_name.text = model.userFullName
            holder.itemView.TextView_description.text = model.description // convert to float to match RatingBar.rating type

            var yummySize: Int = 0
            var sweetSize: Int = 0
            var saltySize: Int = 0
            var sourSize: Int = 0
            var bitterSize: Int = 0
            var review: String = ""
            var row: String = ""

            val reviews = model.review as HashMap<String, ArrayList<String>>

            model.review!!["Yummy"]?.size?.let {
                yummySize = model.review!!["Yummy"]?.size!!
            }
            holder.itemView.TextView_yummy.text = yummySize.toString()

            model.review!!["Sweet"]?.size?.let {
                sweetSize = model.review!!["Sweet"]?.size!!
            }
            holder.itemView.TextView_sweet.text = sweetSize.toString()

            model.review!!["Sour"]?.size?.let {
                sourSize = model.review!!["Sour"]?.size!!
            }
            holder.itemView.TextView_sour.text = sourSize.toString()

            model.review!!["Salty"]?.size?.let {
                saltySize = model.review!!["Salty"]?.size!!
            }
            holder.itemView.TextView_salty.text = saltySize.toString()

            model.review!!["Bitter"]?.size?.let {
                bitterSize = model.review!!["Bitter"]?.size!!
            }
            holder.itemView.TextView_bitter.text = bitterSize.toString()

            when {
                model.review!!["Yummy"]?.contains(authDb.currentUser!!.uid) == true -> {
                    targetId = resources.getIdentifier(
                        "TextView_yummy", "id",
                        packageName
                    )
                    oldSize = yummySize
                    holder.itemView.imageView_yummy.setBackgroundResource(R.drawable.yummyr)
                }
                model.review!!["Sweet"]?.contains(authDb.currentUser!!.uid) == true -> {
                    targetId = resources.getIdentifier(
                        "TextView_sweet", "id",
                        packageName
                    )
                    oldSize = sweetSize
                    holder.itemView.imageView_sweet.setBackgroundResource(R.drawable.sweetr)
                }
                model.review!!["Salty"]?.contains(authDb.currentUser!!.uid) == true -> {
                    targetId = resources.getIdentifier(
                        "TextView_salty", "id",
                        packageName
                    )
                    oldSize = saltySize
                    holder.itemView.imageView_salty.setBackgroundResource(R.drawable.saltyr)
                }
                model.review!!["Sour"]?.contains(authDb.currentUser!!.uid) == true -> {
                    targetId = resources.getIdentifier(
                        "TextView_sour", "id",
                        packageName
                    )
                    oldSize = sourSize
                    holder.itemView.imageView_sour.setBackgroundResource(R.drawable.sourr)
                }
                model.review!!["Bitter"]?.contains(authDb.currentUser!!.uid) == true -> {
                    targetId = resources.getIdentifier(
                        "TextView_bitter", "id",
                        packageName
                    )
                    oldSize = bitterSize
                    holder.itemView.imageView_bitter.setBackgroundResource(R.drawable.bitterr)
                }
            }

            if (model.review!!["Like"]?.contains(authDb.currentUser!!.uid) == true) {
                holder.itemView.ImageView_like.setBackgroundResource(R.drawable.hatheart)
            } else {
                holder.itemView.ImageView_like.setBackgroundResource(R.drawable.hat)
            }

            holder.itemView.imageView_yummy.setOnClickListener {
                review = "Yummy"
                row = model.id.toString()
                updateReviews(review, row, holder, targetId, oldSize, model)
            }

            holder.itemView.imageView_sweet.setOnClickListener {
                review = "Sweet"
                row = model.id.toString()
                updateReviews(review, row, holder, targetId, oldSize, model)
            }

            holder.itemView.imageView_salty.setOnClickListener {
                review = "Salty"
                row = model.id.toString()
                updateReviews(review, row, holder, targetId, oldSize, model)
            }

            holder.itemView.imageView_sour.setOnClickListener {
                review = "Sour"
                row = model.id.toString()
                updateReviews(review, row, holder, targetId, oldSize, model)
            }

            holder.itemView.imageView_bitter.setOnClickListener {
                review = "Bitter"
                row = model.id.toString()
                updateReviews(review, row, holder, targetId, oldSize, model)
            }

            holder.itemView.ImageView_like.setOnClickListener {

                rootDB.collection("posts").document(model.id.toString()).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            try {
                                val rev = document.get("review") as HashMap<String, ArrayList<String>>
                                rev.forEach { (key, value) ->
                                    if (value.contains(authDb.currentUser!!.uid) && key == "Like") {
                                        value.remove(authDb.currentUser!!.uid)
                                        rootDB.collection("posts").document(model.id.toString())
                                            .update("review", rev)
                                    } else if (!value.contains(authDb.currentUser!!.uid) && key == "Like") {
                                        value.add(authDb.currentUser!!.uid)
                                        rootDB.collection("posts").document(model.id.toString())
                                            .update("review", rev)
                                    }
                                }
                            } catch (e: Throwable) {
                                Toast.makeText(applicationContext, "Error" + e , Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }

            holder.itemView.imageView_comment.setOnClickListener {
                val intent = Intent(applicationContext, CommentActivity::class.java)
                intent.putExtra("pageBack", "home")
                intent.putExtra("postId", model.id)
                startActivity(intent)
            }

            //This for the the username click taking the user to the other users profile page
            holder.itemView.TextView_name.setOnClickListener {
                if(model.userId == authDb.currentUser?.uid) {
                    startActivity(Intent(applicationContext, PersonalActivity::class.java))
                } else {
                    val intent = Intent(applicationContext, PersonalActivityUserSide::class.java)
                    intent.putExtra("userID", model.userId)
                    startActivity(intent)
                }
            }

            holder.itemView.ImageView_fork.setOnClickListener {
                val intent = Intent(applicationContext, ReviewActivity::class.java)
                intent.putExtra("pageBack", "home")
                intent.putExtra("postId", model.id)
                startActivity(intent)
            }
        }
    }

    private fun updateReviews(review: String, row: String, holder: PostViewHolder, targetId: Int, oldSize: Int, model: Post) {
        holder.itemView.imageView_yummy.setBackgroundResource(R.drawable.yummy)
        holder.itemView.imageView_sweet.setBackgroundResource(R.drawable.sweet)
        holder.itemView.imageView_salty.setBackgroundResource(R.drawable.salty)
        holder.itemView.imageView_sour.setBackgroundResource(R.drawable.sour)
        holder.itemView.imageView_bitter.setBackgroundResource(R.drawable.bitter)
        val reviewTextView = "holder.itemView.TextView_"+review
        val textViewId = resources.getIdentifier(
            reviewTextView, "id",
            packageName
        )
        val textViewTarget = findViewById<View>(textViewId) as? TextView

        val reviewImageView = "holder.itemView.ImageView_"+review
        val imageViewId = resources.getIdentifier(
            reviewImageView, "id",
            packageName
        )
        val imageViewTarget = findViewById<View>(imageViewId) as? ImageView

        rootDB.collection("posts").document(row).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val reviewed = document.get("review") as HashMap<String, ArrayList<String>>

                    try {
                        reviewed.forEach { (key, value) ->
                            if (value.contains(authDb.currentUser!!.uid) && key == review) {
                                value.remove(authDb.currentUser!!.uid)
                                rootDB.collection("posts").document(row)
                                    .update("review", reviewed)
                                textViewTarget?.text = value.size.toString()
                            } else if (value.contains(authDb.currentUser!!.uid) && key != review) {
                                value.remove(authDb.currentUser!!.uid)
                                rootDB.collection("posts").document(row)
                                    .update("review", reviewed)
                                val target = findViewById<View>(targetId) as TextView
                                target.text = oldSize.toString()
                                this@HomeActivity.targetId = resources.getIdentifier(
                                    textViewTarget.toString(), "id",
                                    packageName
                                )
                                model.review!![review]?.size?.let {
                                    this@HomeActivity.oldSize = model.review!![review]?.size!!
                                }
                            } else if (!value.contains(authDb.currentUser!!.uid) && key == review) {
                                value.add(authDb.currentUser!!.uid)
                                rootDB.collection("posts").document(model.id!!)
                                    .update("review", reviewed)
                                when(review) {
                                    "Yummy" -> imageViewTarget?.setBackgroundResource(R.drawable.yummyr)
                                    "Sweet" -> imageViewTarget?.setBackgroundResource(R.drawable.sweetr)
                                    "Salty" -> imageViewTarget?.setBackgroundResource(R.drawable.saltyr)
                                    "Sour" -> imageViewTarget?.setBackgroundResource(R.drawable.sourr)
                                    "Bitter" -> imageViewTarget?.setBackgroundResource(R.drawable.bitterr)
                                }
                            }
                        }
                    } catch (e: Throwable) {
                        Toast.makeText(applicationContext, "Error" + e, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")

        return sdf.format(Date())
    }
}
