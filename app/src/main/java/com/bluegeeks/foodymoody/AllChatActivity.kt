package com.bluegeeks.foodymoody

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.realtimeDB
import com.bluegeeks.foodymoody.entity.ChatUsers
import java.util.ArrayList


class AllChatActivity : AppCompatActivity() {
    //private var adapter: AllChatActivity.ChatAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_chat)
        val chatUsersList = ArrayList<ChatUsers>()

        realtimeDB.reference.child(BaseFirebaseProperties.authDb.currentUser?.uid!!).get().addOnCompleteListener {task ->
            if (task.isSuccessful) {
                val res = task.result
                if (res != null) {
                    for (snapshot in res.children) {
                        chatUsersList.add(ChatUsers(snapshot.key))
                    }
                }

            }
        }

//
//        val chatQuery = realtimeDB.reference.child(BaseFirebaseProperties.authDb.currentUser?.uid!!).get()
//        // set our recyclerview to use LinearLayout
//        allChatRecycler.layoutManager = LinearLayoutManager(this)
//
//        val options = chatQuery?.let { FirebaseRecyclerOptions.Builder<ChatUsers>().setQuery(it, ChatUsers::class.java).build() }
//        adapter = options?.let { ChatAdapter(it) }
//        allChatRecycler.adapter = adapter
    }

//    override fun onPause() {
//        adapter?.stopListening()
//        super.onPause()
//    }
//    override fun onResume() {
//        super.onResume()
//        adapter?.startListening()
//    }
//
//
//    // create inner classes needed to bind the data to the recyclerview
//    private inner class ChatViewHolder internal constructor(private val view: View) :
//            RecyclerView.ViewHolder(view) {}
//
//    private inner class ChatAdapter internal constructor(options: FirebaseRecyclerOptions<ChatUsers>) :
//            FirebaseRecyclerAdapter<ChatUsers, ChatViewHolder>(options) {
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
//                val view =
//                        LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
//                return ChatViewHolder(view)
//        }
//
//        override fun onBindViewHolder(holder: ChatViewHolder, position: Int, model: ChatUsers) {
//            holder.itemView.chat_user_name.text =
//        }
//    }
}