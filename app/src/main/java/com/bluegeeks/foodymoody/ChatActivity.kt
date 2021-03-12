package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.authDb
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.realtimeDB
import com.bluegeeks.foodymoody.entity.ChatMessage
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.item_left_chat.view.*
import kotlinx.android.synthetic.main.toolbar_main.*
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private var adapter: ChatActivity.ChatAdapter? = null
    private val RIGHT_MESSAGE = 0
    private val LEFT_MESSAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val userID = intent.getStringExtra("chatReceiverID")
        //Instantiate the tool bar to show the username of the chat user
        if (userID != null) {
            BaseFirebaseProperties.rootDB.collection("users").document(userID).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userInfo = task.result
                    if (userInfo != null) {
                        setSupportActionBar(topToolbar)
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        supportActionBar?.title = userInfo.get("userName").toString()
                        }
                    }
                }
            }

        val chatQuery = userID?.let { realtimeDB.reference.child(authDb.currentUser?.uid!!).child(it).child("messages").orderByChild("sendAt") }
        // set our recyclerview to use LinearLayout
        chat_recycler.layoutManager = LinearLayoutManager(this)
        val options =
                chatQuery?.let { FirebaseRecyclerOptions.Builder<ChatMessage>().setQuery(it, ChatMessage::class.java).build() }
        adapter = options?.let { ChatAdapter(it) }
        chat_recycler.adapter = adapter
        
        // Scroll to bottom on new messages
        adapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                chat_recycler.smoothScrollToPosition(positionStart)
            }
        })

        button_chat_send.setOnClickListener {
            if (edit_chat_message.text.isNotEmpty()) {
                val tempChatMessage = ChatMessage(edit_chat_message.text.toString(), getTime(),
                    authDb.currentUser?.uid
                )
                userID?.let { realtimeDB.reference.child(authDb.currentUser?.uid!!).child(it).child("messages").push().setValue(tempChatMessage) }
                if (userID != null) {
                    realtimeDB.reference.child(userID).child(authDb.currentUser?.uid!!).child("messages").push().setValue(tempChatMessage)
                }
                edit_chat_message.setText("");
            }
        }
    }

    override fun onPause() {
        adapter?.stopListening()
        super.onPause()
    }
    override fun onResume() {
        super.onResume()
        adapter?.startListening()
    }

    // create inner classes needed to bind the data to the recyclerview
    private inner class ChatViewHolder internal constructor(private val view: View) :
            RecyclerView.ViewHolder(view) {}

    private inner class ChatAdapter internal constructor(options: FirebaseRecyclerOptions<ChatMessage>) :
            FirebaseRecyclerAdapter<ChatMessage, ChatViewHolder>(options) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            return if (viewType == RIGHT_MESSAGE) {
                val view =
                        LayoutInflater.from(parent.context).inflate(R.layout.item_right_chat, parent, false)
                ChatViewHolder(view)
            } else {
                val view =
                        LayoutInflater.from(parent.context).inflate(R.layout.item_left_chat, parent, false)
                ChatViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int, model: ChatMessage) {
            holder.itemView.chatMessage.text = model.chatValue
        }

        override fun getItemViewType(position: Int): Int {
            val temporary = getItem(position)
            return if(temporary.senderID == authDb.currentUser?.uid!!) {
                RIGHT_MESSAGE
            } else {
                LEFT_MESSAGE
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
        val currentDateandTime: String = sdf.format(Date())
        return currentDateandTime
    }

    //This is the action for back button
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}