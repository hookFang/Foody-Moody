package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.authDb
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.rootDB
import com.bluegeeks.foodymoody.entity.Notifications
import com.bluegeeks.foodymoody.entity.Post
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_personal.*
import kotlinx.android.synthetic.main.activity_personal_user_side.*
import kotlinx.android.synthetic.main.activity_personal_user_side.TextView_bio_content
import kotlinx.android.synthetic.main.activity_personal_user_side.button_change_format
import kotlinx.android.synthetic.main.activity_personal_user_side.followers_text_view
import kotlinx.android.synthetic.main.activity_personal_user_side.following_text_view
import kotlinx.android.synthetic.main.activity_personal_user_side.imageView_profile_picture
import kotlinx.android.synthetic.main.activity_personal_user_side.postsRecyclerView
import kotlinx.android.synthetic.main.activity_personal_user_side.posts_text_view
import kotlinx.android.synthetic.main.activity_personal_user_side.textView_name
import kotlinx.android.synthetic.main.item_post.view.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.text.SimpleDateFormat
import java.util.*

class PersonalActivityUserSide : AppCompatActivity() {

    private var adapter: PostAdapter? = null
    var oldSize: Int = 0
    var targetId: Int = 0
    var displayStatus: Boolean = false
    val grid = 0
    val list = 1
    var isPrivate: Boolean = false
    var isFollowing: Boolean = false
    private val requestMessageSuccess = "Request sent successfully"
    private val requestMessageFail = "Error, Request not sent!"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_user_side)

        val userID = intent.getStringExtra("userID")
        isPrivate = intent.getBooleanExtra("isPrivate", false)
        isFollowing = intent.getBooleanExtra("isFollowing", false)

        rootDB.collection("users").document(authDb.currentUser!!.uid).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userInfo = task.result
                if (userInfo != null) {
                    val following: ArrayList<String> = userInfo.get("following") as ArrayList<String>
                    if (following.contains(userID)) {
                        follow_button.text = "UnFollow"
                    } else {
                        //Checking to see if user already sent follow request
                        rootDB.collection("notifications").whereEqualTo("userId", userID).whereEqualTo("followerRequestId", authDb.currentUser!!.uid).get().addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                val userNotification = task2.result
                                if (userNotification != null) {
                                    task2.result?.forEach { doc ->
                                        val notificationFollowing: Boolean = doc.get("following").toString().toBoolean()
                                        if (notificationFollowing === false) {
                                            follow_button.text = "Request sent"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (userID != null) {
            rootDB.collection("users").document(userID).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userInfo = task.result
                    if (userInfo != null) {
                        val following: ArrayList<String> = userInfo.get("following") as ArrayList<String>
                        val followers: ArrayList<String> = userInfo.get("followers") as ArrayList<String>
                        val postsNumber: ArrayList<String> = userInfo.get("postsID") as ArrayList<String>
                        following_text_view.text = following.size.toString() + "\nFollowing"
                        followers_text_view.text = followers.size.toString() + "\nFollowers"
                        posts_text_view.text = postsNumber.size.toString() + "\nPosts"
                        textView_name.text = userInfo.get("userName").toString()
                        if (userInfo.get("bio") != null && userInfo.get("bio") != "") {
                            TextView_bio_content.text = userInfo.get("bio") as CharSequence?
                        }
                    }
                }
            }
        }

        imageView_profile_picture.setImageResource(R.drawable.logout)

        //Follow button add the user to the array list in firebase
        follow_button.setOnClickListener {
            if (follow_button.text == "Follow") {
                if (isPrivate === false) {
                    rootDB.collection("users").document(authDb.currentUser!!.uid).update("following", (FieldValue.arrayUnion(userID)))
                    if (userID != null) {
                        rootDB.collection("users").document(userID).update("followers", (FieldValue.arrayUnion(authDb.currentUser!!.uid)))
                        rootDB.collection("posts").whereEqualTo("userId", userID).get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                task.result?.forEach { doc ->
                                    doc.reference.update("sharedWithUsers", (FieldValue.arrayUnion(authDb.currentUser!!.uid)))
                                }
                            }
                        }
                    }
                    follow_button.text = "UnFollow"
                } else {
                    //If account is private add notification to user that follow request will be sent to
                    rootDB.collection("notifications").document(authDb.currentUser!!.uid).get()
                            .addOnCompleteListener { user ->
                                if (user.isSuccessful) {
                                    val userInfo = user.result
                                    if (userInfo != null) {
                                        try {
                                            val notification = Notifications()
                                            //user that follow request will be sent to
                                            notification.userId = userID
                                            //User that wants to follow private account
                                            notification.followerRequestId = authDb.currentUser!!.uid
                                            notification.time = getTime()
                                            notification.id = rootDB.collection("notifications").document().id
                                            rootDB.collection("notifications").document(notification.id!!).set(notification)
                                            Toast.makeText(this, requestMessageSuccess, Toast.LENGTH_LONG).show()
                                        } catch (e: Exception) {
                                            Log.e("TAG", e.message!!)
                                            Toast.makeText(this, requestMessageFail, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                    follow_button.text = "Request sent"
                }
            } else if (follow_button.text == "UnFollow") {
                rootDB.collection("users").document(authDb.currentUser!!.uid).update("following", (FieldValue.arrayRemove(userID)))
                if (userID != null) {
                    rootDB.collection("users").document(userID).update("followers", (FieldValue.arrayRemove(authDb.currentUser!!.uid)))
                    rootDB.collection("posts").whereEqualTo("userId", userID).get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result?.forEach { doc ->
                                doc.reference.update("sharedWithUsers", (FieldValue.arrayRemove(authDb.currentUser!!.uid)))
                            }
                        }
                    }
                }
                follow_button.text = "Follow"
            } else {
                rootDB.collection("notifications").whereEqualTo("userId", userID).whereEqualTo("followerRequestId", authDb.currentUser!!.uid).get().addOnCompleteListener { task2 ->
                    if (task2.isSuccessful) {
                        if (task2.result != null) {
                            task2.result?.forEach { doc ->
                                doc.reference.delete()
                            }
                        }
                    } else {
                        Toast.makeText(applicationContext, "Error. Try Again", Toast.LENGTH_SHORT).show()
                    }
                }
                follow_button.text = "Follow"
            }
        }

        //Message button click takes the user to chat page
        message_button.setOnClickListener {
            val intent = Intent(applicationContext, ChatActivity::class.java)
            intent.putExtra("chatReceiverID", userID)
            startActivity(intent)
        }

        // set our recyclerview to use LinearLayout
        postsRecyclerView.layoutManager = LinearLayoutManager(this)
        val postsQuery = rootDB.collection("posts").whereEqualTo("userId", userID).orderBy("time", Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<Post>().setQuery(postsQuery, Post::class.java).build()
        adapter = PostAdapter(options)
        if (!isPrivate || (isPrivate && isFollowing)) {
            postsRecyclerView.adapter = adapter
        }

        button_change_format.setOnClickListener {
            if (!displayStatus) {
                postsRecyclerView.layoutManager = GridLayoutManager(this, 3)
            } else {
                postsRecyclerView.layoutManager = LinearLayoutManager(this)
            }
            displayStatus = !displayStatus
        }

        followers_text_view.setOnClickListener {
            if (userID != "null") {
                rootDB.collection("users").document(userID.toString()).get().addOnSuccessListener { task ->
                    if (task != null) {
                        if (task.get("followListIsVisible") == true) {
                            val intent = Intent(applicationContext, FollowingActivity::class.java)
                            intent.putExtra("button", "followers")
                            intent.putExtra("pageBack", "personalUserSide")
                            intent.putExtra("userID", userID)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "List is protected", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        following_text_view.setOnClickListener{
            if (userID != "null") {
                rootDB.collection("users").document(userID.toString()).get().addOnSuccessListener { task ->
                    if (task != null) {
                        if (task.get("followListIsVisible") == true) {
                            val intent = Intent(applicationContext, FollowingActivity::class.java)
                            intent.putExtra("button", "followers")
                            intent.putExtra("pageBack", "personalUserSide")
                            intent.putExtra("userID", userID)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "List is protected", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        //instantiate toolbar
        setSupportActionBar(topToolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bar_personal, menu)
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
            R.id.action_home -> {
                startActivity(Intent(applicationContext, HomeActivity::class.java))
                return true
            }
            R.id.action_notification -> {
                startActivity(Intent(applicationContext, NotificationActivity::class.java))
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

            val view: View
            return if (viewType == list) {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
                PostViewHolder(view)
            } else {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_changed, parent, false)
                PostViewHolder(view)
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (displayStatus) {
                grid
            } else {
                list
            }
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindViewHolder(
                holder: PostViewHolder,
                position: Int,
                model: Post
        ) {
            if (!displayStatus) {
                val dateStart = model.time
                val dateStop = getTime()

                val format = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")

                var d1: Date? = null
                var d2: Date? = null
                var time: String? = null

                try {
                    d1 = format.parse(dateStart)
                    d2 = format.parse(dateStop)
                    val diff: Long = d2.getTime() - d1.getTime()
                    val seconds = diff.toInt() / 1000
                    val minutes = seconds / 60
                    val hours = minutes / 60
                    val days = hours / 24

                    if (days >= 1) {
                        time = "$days Day(s) ago"
                    } else if (hours >= 1) {
                        time = "$hours Hour(s) ago"
                    } else if (minutes >= 1) {
                        time = "$minutes Minute(s) ago"
                    } else if (seconds >= 10) {
                        time = "$seconds Second(s) ago"
                    } else if (seconds < 10) {
                        time = "Recently"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if(model.postIsPhoto!!) {
                    holder.itemView.videoView_post_home.visibility = View.GONE
                    holder.itemView.ImageView_post.visibility = View.VISIBLE
                    Glide.with(this@PersonalActivityUserSide).load(BaseFirebaseProperties.imageRef.child("images/" + model.id + ".jpeg")).into(holder.itemView.ImageView_post);
                } else {
                    holder.itemView.ImageView_post.visibility = View.GONE
                    holder.itemView.videoView_post_home.visibility = View.VISIBLE
                    BaseFirebaseProperties.videoRef.child("videos/" + model.id).downloadUrl.addOnSuccessListener {
                        print(it)
                        holder.itemView.videoView_post_home.setVideoURI(it)
                        holder.itemView.videoView_post_home.start()
                    }.addOnFailureListener {
                        Log.i("TAG", "Error loading video, Wrong URI")
                    }
                    //To play/pause video
                    holder.itemView.videoView_post_home.setOnClickListener {
                        if (holder.itemView.videoView_post_home.isPlaying) {
                            holder.itemView.videoView_post_home.pause()
                        } else {
                            holder.itemView.videoView_post_home.start()
                        }
                    }
                }
                holder.itemView.textView_time.text = time
                holder.itemView.TextView_name.text = model.userFullName
                holder.itemView.TextView_description.text = model.description // convert to float to match RatingBar.rating type

                if (model.review!!["Like"]?.contains(authDb.currentUser!!.uid) == true) {
                    holder.itemView.ImageView_like.setBackgroundResource(R.drawable.hatheart)
                } else {
                    holder.itemView.ImageView_like.setBackgroundResource(R.drawable.hat)
                }

                var yummySize = 0
                var sweetSize = 0
                var saltySize = 0
                var sourSize = 0
                var bitterSize = 0
                var review = ""
                var row = ""

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
                                    val rev = document.get("review") as HashMap<String, java.util.ArrayList<String>>
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
                    intent.putExtra("pageBack", "personalUserSide")
                    intent.putExtra("userId", model.userId)
                    intent.putExtra("postId", model.id)
                    startActivity(intent)
                }

                holder.itemView.ImageView_fork.setOnClickListener {
                    val intent = Intent(applicationContext, ReviewActivity::class.java)
                    intent.putExtra("pageBack", "personalUserSide")
                    intent.putExtra("userId", model.userId)
                    intent.putExtra("postId", model.id)
                    startActivity(intent)
                }
            } else {
                val param = postsRecyclerView.layoutParams as ViewGroup.MarginLayoutParams
                param.setMargins(3, 0, 8, 0)
                Glide.with(this@PersonalActivityUserSide).load(BaseFirebaseProperties.imageRef.child("images/" + model.id + ".jpeg")).into(holder.itemView.ImageView_post)

                holder.itemView.ImageView_post.setOnClickListener {
                    finish()
                    startActivity(intent)
                }
            }
        }
    }

    private fun updateReviews(review: String, row: String, holder: PostViewHolder, targetId: Int, oldSize: Int, model: Post) {
        holder.itemView.imageView_yummy.setBackgroundResource(R.drawable.yummy)
        holder.itemView.imageView_sweet.setBackgroundResource(R.drawable.sweet)
        holder.itemView.imageView_salty.setBackgroundResource(R.drawable.salty)
        holder.itemView.imageView_sour.setBackgroundResource(R.drawable.sour)
        holder.itemView.imageView_bitter.setBackgroundResource(R.drawable.bitter)
        val reviewTextView = "holder.itemView.TextView_" + review
        val textViewId = resources.getIdentifier(
            reviewTextView, "id",
            packageName
        )
        val textViewTarget = findViewById<View>(textViewId) as? TextView

        val reviewImageView = "holder.itemView.ImageView_" + review
        val imageViewId = resources.getIdentifier(
            reviewImageView, "id",
            packageName
        )
        val imageViewTarget = findViewById<View>(imageViewId) as? ImageView

        rootDB.collection("posts").document(row).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val reviewed = document.get("review") as HashMap<String, java.util.ArrayList<String>>

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
                                this@PersonalActivityUserSide.targetId = resources.getIdentifier(
                                    textViewTarget.toString(), "id",
                                    packageName
                                )
                                model.review!![review]?.size?.let {
                                    this@PersonalActivityUserSide.oldSize = model.review!![review]?.size!!
                                }
                            } else if (!value.contains(authDb.currentUser!!.uid) && key == review) {
                                value.add(authDb.currentUser!!.uid)
                                rootDB.collection("posts").document(model.id!!)
                                    .update("review", reviewed)
                                when (review) {
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