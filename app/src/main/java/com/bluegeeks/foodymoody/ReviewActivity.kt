package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.authDb
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.rootDB
import com.bluegeeks.foodymoody.entity.Notifications
import com.google.firebase.firestore.FieldValue
import kotlinx.android.synthetic.main.activity_personal_user_side.*
import kotlinx.android.synthetic.main.activity_review.*
import kotlinx.android.synthetic.main.item_reviews.*
import kotlinx.android.synthetic.main.item_reviews.view.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ReviewActivity : AppCompatActivity() {

    var pageBack: String = ""
    var userId: String = ""
    var review: Array<String> = arrayOf("Yummy", "Sweet", "Sour", "Salty", "Bitter", "Like")
    var position: Int = 0
    var users: ArrayList<String> = ArrayList()
    private lateinit var layout: RelativeLayout
    private val requestMessageSuccess: String = "Request sent successfully"
    private val requestMessageFail: String = "Error, Request not sent!"

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        val postId = intent.getStringExtra("postId")
        pageBack = intent.getStringExtra("pageBack").toString()
        userId = intent.getStringExtra("userId").toString()

        TextView_review.text = review[position].toUpperCase(Locale.ROOT) + "\'s"
        reviewsRecyclerView.layoutManager = LinearLayoutManager(this)
        postId?.let {
            rootDB.collection("posts").document(it).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val reviewed = document.get("review") as HashMap<String, ArrayList<String>>
                        reviewed.forEach { (key, value) ->
                            if (key == review[position]) {
                                users = value
                                reviewsRecyclerView.layoutManager = LinearLayoutManager(this)
                                reviewsRecyclerView.adapter = ReviewAdapter(users, this)
                            }
                        }
                    }
                }
        }

        layout = findViewById(R.id.review_linearLayout)
        layout.setOnTouchListener(object : OnSwipeTouchListener(this@ReviewActivity) {
            @SuppressLint("ClickableViewAccessibility")
            override fun onSwipeLeft() {
                super.onSwipeLeft()

                if(position < 5) {
                    position++
                } else {
                    position = 0
                }
                TextView_review.text = review[position].toUpperCase(Locale.ROOT) + "\'s"
                postId?.let {
                    rootDB.collection("posts").document(it).get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                val reviewed = document.get("review") as HashMap<String, ArrayList<String>>
                                reviewed.forEach { (key, value) ->
                                    if (key == review[position]) {
                                        users = value
                                        reviewsRecyclerView.layoutManager = LinearLayoutManager(this@ReviewActivity)
                                        reviewsRecyclerView.adapter = ReviewAdapter(users, this@ReviewActivity)
                                    }
                                }
                            }
                        }
                }

            }
            @SuppressLint("ClickableViewAccessibility")
            override fun onSwipeRight() {
                super.onSwipeRight()
                if(position > 0) {
                    position--
                } else {
                    position = 5
                }
                TextView_review.text = review[position].toUpperCase(Locale.ROOT) + "\'s"
                postId?.let {
                    TextView_review.text = review[position].toUpperCase(Locale.ROOT) + "\'s"
                    rootDB.collection("posts").document(it).get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                val reviewed = document.get("review") as HashMap<String, ArrayList<String>>
                                reviewed.forEach { (key, value) ->
                                    if (key == review[position]) {
                                        users = value
                                        reviewsRecyclerView.layoutManager = LinearLayoutManager(this@ReviewActivity)
                                        reviewsRecyclerView.adapter = ReviewAdapter(users, this@ReviewActivity)
                                    }
                                }
                            }
                        }
                }
            }
        })

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
                when (pageBack) {
                    "home" -> {
                        startActivity(Intent(applicationContext, HomeActivity::class.java))
                        return true
                    }
                    "personal" -> {
                        startActivity(Intent(applicationContext, PersonalActivity::class.java))
                        return true
                    }
                    "personalUserSide" -> {
                        val intent = Intent(this@ReviewActivity, PersonalActivityUserSide::class.java)
                        intent.putExtra("userID", userId)
                        startActivity(intent)
                        return true
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // create inner classes needed to bind the data to the recyclerview
    private inner class ReviewViewHolder internal constructor(private val view: View) :
        RecyclerView.ViewHolder(view) {}

    private inner class ReviewAdapter internal constructor(options: ArrayList<String>, reviewActivity: ReviewActivity) :
        RecyclerView.Adapter<ReviewViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int

        ): ReviewViewHolder {
            return ReviewViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_reviews, parent, false))
        }

        override fun getItemCount(): Int {
            return users.size
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat", "ResourceAsColor")
        override fun onBindViewHolder(
            holder: ReviewViewHolder,
            position: Int
        ) {

            rootDB.collection("users").document(users.get(position)).get().addOnSuccessListener { document ->
                if (document != null) {
                    holder.itemView.TextView_user.text = document.get("userName") as String
                    if ((document.get("followers") as ArrayList<String>).contains(authDb.currentUser!!.uid)) {
                        holder.itemView.Button_follow.text = "UnFollow"
                        holder.itemView.Button_follow.setBackgroundResource(R.drawable.button_unfollow)
                        holder.itemView.Button_follow.setTextColor(R.color.red)
                    } else {
                        // It cannot find if user requested follow or not because of the id in notifications
//                        rootDB.collection("users").document(users.get(position)+authDb.currentUser!!.uid).get().addOnSuccessListener { document ->
//                            if (document != null) {
//                                    holder.itemView.Button_follow.text = "Requested"
//                                    holder.itemView.Button_follow.setBackgroundResource(R.drawable.button_follow)
//                                } else {
                                    holder.itemView.Button_follow.text = "Follow"
                                    holder.itemView.Button_follow.setBackgroundResource(R.drawable.button_follow)
//                                }
//                            }
                    }
                }
            }
            holder.itemView.Button_follow.setOnClickListener {
                when (holder.itemView.Button_follow.text) {
                    "Follow" -> {
                        rootDB.collection("users").document(users.get(position)).get().addOnSuccessListener { document ->
                            if (document != null) {
                                if (document.get("private") == false) {
                                    rootDB.collection("users").document(authDb.currentUser!!.uid).update("following", (FieldValue.arrayUnion(document.id)))
                                    rootDB.collection("users").document(document.id).update("followers", (FieldValue.arrayUnion(authDb.currentUser!!.uid)))
                                    rootDB.collection("posts").whereEqualTo("userId", document.id).get().addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            task.result?.forEach { doc ->
                                                doc.reference.update("sharedWithUsers", (FieldValue.arrayUnion(authDb.currentUser!!.uid)))
                                            }
                                        }
                                    }
                                    holder.itemView.Button_follow.text = "UnFollow"
                                    holder.itemView.Button_follow.setBackgroundResource(R.drawable.button_unfollow)
                                    holder.itemView.Button_follow.setTextColor(R.color.red)
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
                                                        notification.userId = document.id
                                                        //User that wants to follow private account
                                                        notification.followerRequestId = authDb.currentUser!!.uid
                                                        notification.time = getTime()
                                                        notification.isFollowing = false
                                                        notification.id = rootDB.collection("notifications").document().id
                                                        rootDB.collection("notifications").document(notification.id!!).set(notification)
                                                        Toast.makeText(this@ReviewActivity, requestMessageSuccess, Toast.LENGTH_LONG).show()
                                                    } catch (e: Exception) {
                                                        Log.e("TAG", e.message!!)
                                                        Toast.makeText(this@ReviewActivity, requestMessageFail, Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                        }
                                    holder.itemView.Button_follow.text = "Requested"
                                    holder.itemView.Button_follow.setBackgroundResource(R.drawable.button_follow)
                                }
                            }
                        }
                    }
                    "UnFollow" -> {
                        rootDB.collection("users").document(users.get(position)).get().addOnSuccessListener { document ->
                            if (document != null) {
                                rootDB.collection("users").document(authDb.currentUser!!.uid)
                                    .update("following", (FieldValue.arrayRemove(document.id)))
                                rootDB.collection("users").document(users.get(position)).update(
                                    "followers",
                                    (FieldValue.arrayRemove(authDb.currentUser!!.uid))
                                )
                                rootDB.collection("posts")
                                    .whereEqualTo("userId", users.get(position)).get()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            task.result?.forEach { doc ->
                                                doc.reference.update(
                                                    "sharedWithUsers",
                                                    (FieldValue.arrayRemove(authDb.currentUser!!.uid))
                                                )
                                            }
                                        }
                                    }
                                holder.itemView.Button_follow.text = "Follow"
                                holder.itemView.Button_follow.setBackgroundResource(R.drawable.button_follow)
                            }
                        }
                    }
                    "Requested" -> {
                        rootDB.collection("notifications")
                            .whereEqualTo("userId", users.get(position))
                            .whereEqualTo("followerRequestId", authDb.currentUser!!.uid)
                            .get().addOnCompleteListener { task2 ->
                                if (task2.isSuccessful) {
                                    if (task2.result != null) {
                                        task2.result?.forEach { doc ->
                                            doc.reference.delete()
                                        }
                                    }
                                }
                            }
                        holder.itemView.Button_follow.text = "Follow"
                        holder.itemView.Button_follow.setBackgroundResource(R.drawable.button_follow)
                    }
                }
            }
        }
    }
    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
        val currentDateandTime: String = sdf.format(Date())
        return currentDateandTime
    }
}