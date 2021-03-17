package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties
import com.bluegeeks.foodymoody.entity.Post
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_home.postsRecyclerView
import kotlinx.android.synthetic.main.activity_personal.*
import kotlinx.android.synthetic.main.activity_personal.TextView_bio_content
import kotlinx.android.synthetic.main.activity_personal.button_change_format
import kotlinx.android.synthetic.main.activity_personal.followers_text_view
import kotlinx.android.synthetic.main.activity_personal.following_text_view
import kotlinx.android.synthetic.main.activity_personal.imageView_profile_picture
import kotlinx.android.synthetic.main.activity_personal.posts_text_view
import kotlinx.android.synthetic.main.activity_personal.textView_name
import kotlinx.android.synthetic.main.activity_personal_user_side.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.bio_dialogue.*
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.item_post.view.*
import kotlinx.android.synthetic.main.item_post_changed.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.text.SimpleDateFormat
import java.util.*


class PersonalActivity : BaseFirebaseProperties() {

    private var adapter: PostAdapter? = null
    var newBio: String = ""
    var oldSize: Int = 0
    var targetId: Int = 0
    var displayStatus: Boolean = false
    val GRID_LAYOUT = 0
    val LINEAR_LAYOUT = 1

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal)

        rootDB.collection("users").document(authDb.currentUser!!.uid).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userInfo = task.result
                if (userInfo != null) {
                    textView_name.text = userInfo.get("userName") as CharSequence?
                    if(userInfo.get("bio") != null && userInfo.get("bio") != "") {
                        TextView_bio_content.text = userInfo.get("bio") as CharSequence?
                        newBio = (userInfo.get("bio") as CharSequence?).toString()
                    }

                    val following: ArrayList<String> = userInfo.get("following") as ArrayList<String>
                    val followers: ArrayList<String> = userInfo.get("followers") as ArrayList<String>
                    val postsNumber: ArrayList<String> = userInfo.get("postsID") as ArrayList<String>
                    following_text_view.text = following.size.toString() + "\nFollowing"
                    followers_text_view.text = followers.size.toString() + "\nFollowers"
                    posts_text_view.text = postsNumber.size.toString() + "\nPosts"

                    if(userInfo.get("photoURI") != "") {
                        //Glide.with(this@ProfileActivity).load(BaseFirebaseProperties.imageRef.child("images/" + model.id + ".jpeg")).into(holder.itemView.ImageView_post);
                        Glide.with(this).load(userInfo.get("photoURI").toString()).into(imageView_profile_picture);
                    }
                } else {
                    textView_name.text = authDb.currentUser!!.displayName
                }
            }
        }

        imageView_profile_picture.setOnClickListener {
            startActivity(Intent(applicationContext, ProfileActivity::class.java))
        }

        val postsQuery = rootDB.collection("posts").whereEqualTo("userId", authDb.currentUser!!.uid).orderBy("time", Query.Direction.DESCENDING)

        // set our recyclerview to use LinearLayout
        postsRecyclerView.layoutManager = LinearLayoutManager(this)
        val options =
                FirestoreRecyclerOptions.Builder<Post>().setQuery(postsQuery, Post::class.java).build()
        adapter = PostAdapter(options)
        postsRecyclerView.adapter = adapter

        button_change_format.setOnClickListener {
            if(!displayStatus) {
                postsRecyclerView.layoutManager = GridLayoutManager(this, 3)
            } else {
                postsRecyclerView.layoutManager = LinearLayoutManager(this)
            }
            displayStatus = !displayStatus
        }

        TextView_bio_content.setOnClickListener {

            val alert = AlertDialog.Builder(this@PersonalActivity)
            val mView: View = layoutInflater.inflate(R.layout.bio_dialogue, null)
            val button_bio_edit: Button = mView.findViewById(R.id.Button_bio_edit)

            val editText_bio_edit: EditText = mView.findViewById(R.id.EditText_bio_edit)
            alert.setView(mView)
            val alertDialog: AlertDialog = alert.create()

            editText_bio_edit.setText(newBio.toString())
            editText_bio_edit.setSelection(editText_bio_edit.length())
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            alertDialog.show()

            button_bio_edit.setOnClickListener{

                rootDB.collection("users").document(authDb.currentUser!!.uid)
                        .update(
                                mapOf(
                                        "bio" to editText_bio_edit.text.toString()
                                )
                        )
                        .addOnSuccessListener {
                            val intent = Intent(applicationContext, PersonalActivity::class.java)
                            finish()
                            startActivity(intent)
                        }
                        .addOnFailureListener {
                            Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
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
                startActivity(Intent(applicationContext, HomeActivity::class.java))
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

            val view: View
            if (viewType == LINEAR_LAYOUT) {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
                return PostViewHolder(view)
            } else {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_changed, parent, false)
                return PostViewHolder(view)
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if(displayStatus) {
                GRID_LAYOUT
            } else {
                LINEAR_LAYOUT
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

                Glide.with(this@PersonalActivity).load(imageRef.child("images/" + model.id + ".jpeg")).into(holder.itemView.ImageView_post)
                holder.itemView.textView_time.text = time
                holder.itemView.TextView_name.text = model.userFullName
                holder.itemView.TextView_description.text = model.description // convert to float to match RatingBar.rating type

                if (model.whoLiked?.contains(authDb.currentUser!!.uid) == true) {
                    holder.itemView.ImageView_hat.setBackgroundResource(R.drawable.hatheart)
                } else {
                    holder.itemView.ImageView_hat.setBackgroundResource(R.drawable.hat)
                }

                holder.itemView.ImageView_hat.setOnClickListener {
                    if (model.whoLiked?.contains(authDb.currentUser!!.uid) == true) {
                        rootDB.collection("posts").document(model.id!!).update(
                                "whoLiked", (FieldValue.arrayRemove(authDb.currentUser!!.uid)))
                    } else {
                        rootDB.collection("posts").document(model.id!!).update(
                                "whoLiked", (FieldValue.arrayUnion(authDb.currentUser!!.uid)))
                    }
                }

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

                if (model.review!!["Yummy"]?.contains(authDb.currentUser!!.uid) == true) {
                    targetId = resources.getIdentifier(
                            "TextView_yummy", "id",
                            packageName
                    )
                    oldSize = yummySize
                    holder.itemView.imageView_yummy.setBackgroundResource(R.drawable.yummyr)
                } else if (model.review!!["Sweet"]?.contains(authDb.currentUser!!.uid) == true) {
                    targetId = resources.getIdentifier(
                            "TextView_sweet", "id",
                            packageName
                    )
                    oldSize = sweetSize
                    holder.itemView.imageView_sweet.setBackgroundResource(R.drawable.sweetr)
                } else if (model.review!!["Salty"]?.contains(authDb.currentUser!!.uid) == true) {
                    targetId = resources.getIdentifier(
                            "TextView_salty", "id",
                            packageName
                    )
                    oldSize = saltySize
                    holder.itemView.imageView_salty.setBackgroundResource(R.drawable.saltyr)
                } else if (model.review!!["Sour"]?.contains(authDb.currentUser!!.uid) == true) {
                    targetId = resources.getIdentifier(
                            "TextView_sour", "id",
                            packageName
                    )
                    oldSize = sourSize
                    holder.itemView.imageView_sour.setBackgroundResource(R.drawable.sourr)
                } else if (model.review!!["Bitter"]?.contains(authDb.currentUser!!.uid) == true) {
                    targetId = resources.getIdentifier(
                            "TextView_bitter", "id",
                            packageName
                    )
                    oldSize = bitterSize
                    holder.itemView.imageView_bitter.setBackgroundResource(R.drawable.bitterr)
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

                holder.itemView.imageView_comment.setOnClickListener {
                    val intent = Intent(applicationContext, CommentActivity::class.java)
                    intent.putExtra("postId", model.id)
                    startActivity(intent)
                }
            } else {
                val param = postsRecyclerView.layoutParams as ViewGroup.MarginLayoutParams
                param.setMargins(3, 0, 8, 0)
                Glide.with(this@PersonalActivity).load(imageRef.child("images/" + model.id + ".jpeg")).into(holder.itemView.ImageView_post)

                holder.itemView.ImageView_post.setOnClickListener {
                    finish();
                    startActivity(intent);
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
        var reviewTextView = "holder.itemView.TextView_"+review
        val textViewId = resources.getIdentifier(
                reviewTextView, "id",
                packageName
        )
        val textViewTarget = findViewById<View>(textViewId) as? TextView

        var reviewImageView = "holder.itemView.ImageView_"+review
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
                                    this@PersonalActivity.targetId = resources.getIdentifier(
                                            textViewTarget.toString(), "id",
                                            packageName
                                    )
                                    model.review!![review]?.size?.let {
                                        this@PersonalActivity.oldSize = model.review!![review]?.size!!
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

