package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties
import com.bluegeeks.foodymoody.entity.Notifications
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_notification.*
import kotlinx.android.synthetic.main.activity_personal_user_side.*
import kotlinx.android.synthetic.main.item_notifications.*
import kotlinx.android.synthetic.main.item_notifications.view.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.text.SimpleDateFormat
import java.util.*

class NotificationActivity : BaseFirebaseProperties() {
    private var adapter: NotificationsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val notificationsQuery = rootDB.collection("notifications").orderBy("following").orderBy("time", Query.Direction.DESCENDING).whereEqualTo("userId", authDb.currentUser!!.uid).whereNotEqualTo("following", true)
        // set our recyclerview to use LinearLayout
        notificationRecyclerView.layoutManager = LinearLayoutManager(this)
        val options =
            FirestoreRecyclerOptions.Builder<Notifications>().setQuery(notificationsQuery, Notifications::class.java)
                .build()
        adapter = NotificationsAdapter(options)
        notificationRecyclerView.adapter = adapter
        //instantiate toolbar
        setSupportActionBar(topToolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bar_notification, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.action_logout -> {
                logout()
                return true
            }
            R.id.action_personal -> {
                startActivity(Intent(applicationContext, PersonalActivity::class.java))
                return true
            }
            R.id.action_home -> {
                startActivity(Intent(applicationContext, HomeActivity::class.java))
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
    private inner class NotificationsViewHolder internal constructor(private val view: View) :
        RecyclerView.ViewHolder(view) {}

    private inner class NotificationsAdapter internal constructor(options: FirestoreRecyclerOptions<Notifications>) :
        FirestoreRecyclerAdapter<Notifications, NotificationsViewHolder>(options) {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): NotificationsViewHolder {

            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_notifications, parent, false)
            return NotificationsViewHolder(view)
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindViewHolder(
            holder: NotificationsViewHolder,
            position: Int,
            model: Notifications
        ) {

            rootDB.collection("users").whereEqualTo("id", model.followerRequestId).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userNotification = task.result
                    if (userNotification != null) {
                        task.result?.forEach { doc ->
                            var userName: String = doc.get("userName").toString()

                            holder.itemView.username.text = userName

                            //This for if the user approves the follower request
                            holder.itemView.btnApprove.setOnClickListener {
                                rootDB.collection("users").document(authDb.currentUser!!.uid).update("followers", (FieldValue.arrayUnion(model.followerRequestId)))
                                rootDB.collection("users").document(model.followerRequestId.toString()).update("following", (FieldValue.arrayUnion(authDb.currentUser!!.uid)))
                                //rootDB.collection("notifications").whereEqualTo("userId", authDb.currentUser!!.uid).whereEqualTo("followerRequestId", model.followerRequestId)
                                rootDB.collection("posts").whereEqualTo("userId", authDb.currentUser!!.uid).get().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        task.result?.forEach { doc ->
                                            doc.reference.update("sharedWithUsers", (FieldValue.arrayUnion(model.followerRequestId.toString())))
                                        }
                                    }
                                }
                                rootDB.collection("notifications").document(model.id.toString())
                                    .update(
                                        mapOf(
                                            "following" to true
                                        )
                                    )
                                    .addOnSuccessListener {
                                        val intent = Intent(applicationContext, NotificationActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(applicationContext, "Error. Could not approve", Toast.LENGTH_SHORT).show()
                                    }
                            }

                            //This for if the user rejects the follower request
                            holder.itemView.btnReject.setOnClickListener {
                                rootDB.collection("notifications").document(model.id.toString())
                                    .delete()
                                    .addOnSuccessListener {
                                        val intent = Intent(applicationContext, NotificationActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(applicationContext, "Error. Could not reject", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                }
            }
        }
    }


    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")

        return sdf.format(Date())
    }
}
