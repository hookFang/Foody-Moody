package com.bluegeeks.foodymoody

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.authDb
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.realtimeDB
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.rootDB
import com.bluegeeks.foodymoody.entity.ChatUsers
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_all_chat.*
import kotlinx.android.synthetic.main.activity_personal.*
import kotlinx.android.synthetic.main.activity_personal_user_side.*
import kotlinx.android.synthetic.main.item_chat_message.view.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.util.ArrayList


class AllChatActivity : AppCompatActivity() {
    private lateinit var adapter: ChatUsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_chat)
        val chatUsersList = ArrayList<ChatUsers>()

        realtimeDB.reference.child(BaseFirebaseProperties.authDb.currentUser?.uid!!).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val res = task.result
                if (res != null) {
                    for (snapshot in res.children) {
                        snapshot.key?.let {
                            rootDB.collection("users").document(it).get().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userInfo = task.result
                                    if (userInfo != null) {
                                        chatUsersList.add(ChatUsers(snapshot.key, userInfo.get("userName").toString(), userInfo.get("photoURI").toString(), snapshot.child("messages").children.last().child("chatValue").value.toString(), snapshot.child("messages").children.last().child("sendAt").value.toString()))
                                    }
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
            }
        }
        adapter = ChatUsersAdapter(chatUsersList)
        allChatRecycler.layoutManager = LinearLayoutManager(this)
        allChatRecycler.adapter = adapter

        //instantiate toolbar
        setSupportActionBar(topToolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bar_message, menu)
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
            R.id.action_notification -> {
                startActivity(Intent(applicationContext, NotificationActivity::class.java))
                return true
            }
            R.id.action_home -> {
                startActivity(Intent(applicationContext, HomeActivity::class.java))
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        authDb.signOut()
        finish()
        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
    }

    // create inner classes needed to bind the data to the recyclerview
    private inner class ChatUsersViewHolder internal constructor(private val view: View) :
            RecyclerView.ViewHolder(view) {}

    private inner class ChatUsersAdapter(private var chatUserList: ArrayList<ChatUsers>) : RecyclerView.Adapter<ChatUsersViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatUsersViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
            return ChatUsersViewHolder(view)
        }

        override fun getItemCount(): Int {
            return chatUserList.size
        }

        override fun onBindViewHolder(holder: ChatUsersViewHolder, position: Int) {
            val chatUser = chatUserList[position]
            holder.itemView.chat_user_name.text = chatUser.username
            holder.itemView.latest_chat_message.text = chatUser.lastMessage
            Glide.with(this@AllChatActivity).load(chatUser.photoURI).into(holder.itemView.chat_user_image)
            //Takes the user to message activity
            holder.itemView.setOnClickListener {
                    val intent = Intent(applicationContext, ChatActivity::class.java)
                    intent.putExtra("chatReceiverID", chatUser.id)
                    startActivity(intent)
            }
        }
    }
}