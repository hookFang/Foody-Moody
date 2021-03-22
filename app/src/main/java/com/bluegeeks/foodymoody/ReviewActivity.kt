package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.authDb
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.rootDB
import kotlinx.android.synthetic.main.activity_review.*
import kotlinx.android.synthetic.main.item_reviews.view.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.util.*
import kotlin.collections.ArrayList

class ReviewActivity : AppCompatActivity() {

    private var adapter: ReviewAdapter? = null
    var pageBack: String = ""
    var userId: String = ""
    var review: Array<String> = arrayOf("Yummy", "Sweet", "Sour", "Salty", "Bitter", "Like")
    var position: Int = 0
    var users: ArrayList<String> = ArrayList()
    private lateinit var layout: RelativeLayout

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

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindViewHolder(
                holder: ReviewViewHolder,
                position: Int
        ) {

            rootDB.collection("users").document(users.get(position)).get().
            addOnSuccessListener { document ->
                if(document != null) {
                    holder.itemView.TextView_user.text = document.get("userName") as String
                    if((document.get("followers") as ArrayList<String>).contains(authDb.currentUser!!.uid)) {
                        holder.itemView.Button_follow.text = "Unfollow"
                        holder.itemView.Button_follow.setBackgroundResource(R.drawable.button_unfollow)
                    } else {
                        holder.itemView.Button_follow.text = "Follow"
                    }
                }
            }
        }
    }
}

