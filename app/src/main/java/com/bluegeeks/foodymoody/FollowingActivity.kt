package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Color.red
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.authDb
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.rootDB
import com.google.firebase.firestore.FieldValue
import kotlinx.android.synthetic.main.activity_following.*
import kotlinx.android.synthetic.main.item_reviews.*
import kotlinx.android.synthetic.main.item_reviews.view.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FollowingActivity : AppCompatActivity() {

    private var adapterFollow: FollowingAdapter? = null
    var pageBack: String = ""
    var button: String = ""
    var following: ArrayList<String> = ArrayList()
    var followers: ArrayList<String> = ArrayList()
    var users: ArrayList<String> = ArrayList()
    var follow: Boolean = false
    private lateinit var layout: RelativeLayout
    private val requestMessageSuccess: String = "Request sent successfully"
    private val requestMessageFail: String = "Error, Request not sent!"

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_following)

        val postId = intent.getStringExtra("postId")
        button = intent.getStringExtra("button").toString()
        pageBack = intent.getStringExtra("pageBack").toString()

        followingsRecyclerView.layoutManager = LinearLayoutManager(this)
        rootDB.collection("users").document(authDb.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    following = document.get("following") as ArrayList<String>
                    followers = document.get("followers") as ArrayList<String>

                    if(button == "following") {
                        TextView_followingList.text = "FOLLOWING\'s"
                        users = following
                    } else {
                        TextView_followingList.text = "FOLLOWERS\'s"
                        users = followers
                        follow = true
                    }
                    followingsRecyclerView.layoutManager = LinearLayoutManager(this)
                    followingsRecyclerView.adapter = FollowingAdapter(users, this)
                }
            }

        layout = findViewById(R.id.following_linearLayout)
        layout.setOnTouchListener(object : OnSwipeTouchListener(this@FollowingActivity) {
            @SuppressLint("ClickableViewAccessibility")
            override fun onSwipeLeft() {
                super.onSwipeLeft()

                if(!follow) {
                    TextView_followingList.text = "FOLLOWER\'s"
                    users = followers
                    followingsRecyclerView.layoutManager = LinearLayoutManager(this@FollowingActivity)
                    followingsRecyclerView.adapter = FollowingAdapter(users, this@FollowingActivity)
                    follow = true
                } else {
                    TextView_followingList.text = "FOLLOWING\'s"
                    users = following
                    followingsRecyclerView.layoutManager = LinearLayoutManager(this@FollowingActivity)
                    followingsRecyclerView.adapter = FollowingAdapter(users, this@FollowingActivity)
                    follow = false
                }
            }
            @SuppressLint("ClickableViewAccessibility")
            override fun onSwipeRight() {
                super.onSwipeRight()

                if(!follow) {
                    TextView_followingList.text = "FOLLOWER\'s"
                    users = followers
                    followingsRecyclerView.layoutManager = LinearLayoutManager(this@FollowingActivity)
                    followingsRecyclerView.adapter = FollowingAdapter(users, this@FollowingActivity)
                    follow = true
                } else {
                    TextView_followingList.text = "FOLLOWING\'s"
                    users = following
                    followingsRecyclerView.layoutManager = LinearLayoutManager(this@FollowingActivity)
                    followingsRecyclerView.adapter = FollowingAdapter(users, this@FollowingActivity)
                    follow = false
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
//                    "personalUserSide" -> {
//                        val intent = Intent(this@FollowingActivity, PersonalActivityUserSide::class.java)
//                        intent.putExtra("userID", userId)
//                        startActivity(intent)
//                        return true
//                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // create inner classes needed to bind the data to the recyclerview
    private inner class FollowingViewHolder internal constructor(private val view: View) :
            RecyclerView.ViewHolder(view) {}

    private inner class FollowingAdapter internal constructor(options: java.util.ArrayList<String>, reviewActivity: FollowingActivity) :
            RecyclerView.Adapter<FollowingViewHolder>() {

        override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int

        ): FollowingViewHolder {
            return FollowingViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_reviews, parent, false))
        }

        override fun getItemCount(): Int {
            return users.size
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat", "ResourceType")
        override fun onBindViewHolder(
                holder: FollowingViewHolder,
                position: Int
        ) {

            rootDB.collection("users").document(users.get(position)).get().addOnSuccessListener { document ->
                if (document != null) {
                    holder.itemView.TextView_user.text = document.get("userName") as String
                    if (!follow) {
                        holder.itemView.Button_follow.text = "UnFollow"
                        holder.itemView.Button_follow.setBackgroundResource(R.drawable.button_unfollow)
                        holder.itemView.Button_follow.setTextColor(R.color.red)
                    } else {
                        holder.itemView.Button_follow.text = "Block"
                        holder.itemView.Button_follow.setBackgroundResource(R.drawable.button_block)
                    }
                }
            }

            holder.itemView.Button_follow.setOnClickListener {
                if (holder.itemView.Button_follow.text == "UnFollow") {
                    rootDB.collection("users").document(users.get(position)).get().addOnSuccessListener { document ->
                        if (document != null) {
                            rootDB.collection("users").document(authDb.currentUser!!.uid).update("following", (FieldValue.arrayRemove(document.id)))
                            rootDB.collection("users").document(users.get(position)).update("followers", (FieldValue.arrayRemove(authDb.currentUser!!.uid)))
                            rootDB.collection("posts").whereEqualTo("userId", users.get(position)).get().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    task.result?.forEach { doc ->
                                        doc.reference.update("sharedWithUsers", (FieldValue.arrayRemove(authDb.currentUser!!.uid)))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

//            rootDB.collection("users").document(users.get(position)).get()
//                    .addOnSuccessListener { document ->
//                if (document != null) {
//                    holder.itemView.TextView_user.text = document.get("userName") as String
//                    if ((document.get("followers") as java.util.ArrayList<String>).contains(authDb.currentUser!!.uid)) {
//                        holder.itemView.Button_follow.text = "UnFollow"
//                        holder.itemView.Button_follow.setBackgroundResource(R.drawable.button_unfollow)
//                    } else {
//                        holder.itemView.Button_follow.text = "Follow"
//                    }
//                }
//            }

//            holder.itemView.Button_follow.setOnClickListener {
//                if (holder.itemView.Button_follow.text == "Follow") {
//                    rootDB.collection("users").document(users.get(position)).get().addOnSuccessListener { document ->
//                        if (document != null) {
//                            if (document.get("private") == false) {
//                                rootDB.collection("users").document(authDb.currentUser!!.uid).update("following", (FieldValue.arrayUnion(document.id)))
//                                rootDB.collection("users").document(document.id).update("followers", (FieldValue.arrayUnion(authDb.currentUser!!.uid)))
//                                rootDB.collection("posts").whereEqualTo("userId", document.id).get().addOnCompleteListener { task ->
//                                    if (task.isSuccessful) {
//                                        task.result?.forEach { doc ->
//                                            doc.reference.update("sharedWithUsers", (FieldValue.arrayUnion(authDb.currentUser!!.uid)))
//                                        }
//                                    }
//                                }
//                                holder.itemView.Button_follow.text = "UnFollow"
//                                holder.itemView.Button_follow.setBackgroundResource(R.drawable.button_unfollow)
//                            } else {
//                                //If account is private add notification to user that follow request will be sent to
//                                rootDB.collection("notifications").document(authDb.currentUser!!.uid).get()
//                                        .addOnCompleteListener { user ->
//                                            if (user.isSuccessful) {
//                                                val userInfo = user.result
//                                                if (userInfo != null) {
//                                                    try {
//                                                        val notification = Notifications()
//                                                        //user that follow request will be sent to
//                                                        notification.userId = document.id
//                                                        //User that wants to follow private account
//                                                        notification.followerRequestId = authDb.currentUser!!.uid
//                                                        notification.time = getTime()
//                                                        notification.isFollowing = false
//                                                        notification.id = rootDB.collection("notifications").document().id
//                                                        rootDB.collection("notifications").document(notification.id!!).set(notification)
//                                                        Toast.makeText(this@FollowingActivity, requestMessageSuccess, Toast.LENGTH_LONG).show()
//                                                    } catch (e: Exception) {
//                                                        Log.e("TAG", e.message!!)
//                                                        Toast.makeText(this@FollowingActivity, requestMessageFail, Toast.LENGTH_LONG).show()
//                                                    }
//                                                }
//                                            }
//                                        }
//                                holder.itemView.Button_follow.text = "Request sent"
//                                holder.itemView.Button_follow.setBackgroundResource(R.drawable.button_follow)
//                            }
//                        }
//                    }
//                } else if (holder.itemView.Button_follow.text == "UnFollow") {
//                    rootDB.collection("users").document(users.get(position)).get().addOnSuccessListener { document ->
//                        if (document != null) {
//                            if (document.get("private") == true) {
//                                rootDB.collection("users").document(authDb.currentUser!!.uid).update("following", (FieldValue.arrayRemove(document.id)))
//                                rootDB.collection("users").document(users.get(position)).update("followers", (FieldValue.arrayRemove(authDb.currentUser!!.uid)))
//                                rootDB.collection("posts").whereEqualTo("userId", users.get(position)).get().addOnCompleteListener { task ->
//                                    if (task.isSuccessful) {
//                                        task.result?.forEach { doc ->
//                                            doc.reference.update("sharedWithUsers", (FieldValue.arrayRemove(authDb.currentUser!!.uid)))
//                                        }
//                                    }
//                                }
//                            }
//                            holder.itemView.Button_follow.text = "Follow"
//                            holder.itemView.Button_follow.setBackgroundResource(R.drawable.button_follow)
//                        } else {
//                            rootDB.collection("notifications").whereEqualTo("userId", users.get(position)).whereEqualTo("followerRequestId", authDb.currentUser!!.uid).get().addOnCompleteListener { task2 ->
//                                if (task2.isSuccessful) {
//                                    if (task2.result != null) {
//                                        task2.result?.forEach { doc ->
//                                            doc.reference.delete()
//                                        }
//                                    }
//                                } else {
//                                    Toast.makeText(applicationContext, "Error. Try Again", Toast.LENGTH_SHORT).show()
//                                }
//                            }
//                            holder.itemView.Button_follow.text = "Follow"
//                            holder.itemView.Button_follow.setBackgroundResource(R.drawable.button_follow)
//                        }
//                    }
//                }
//            }
//        }
//    }

    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")

        return sdf.format(Date())
    }
}