package com.bluegeeks.foodymoody

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.authDb
import com.bluegeeks.foodymoody.entity.BaseFirebaseProperties.Companion.rootDB
import com.bluegeeks.foodymoody.entity.User
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.item_search_user.view.*
import kotlinx.android.synthetic.main.toolbar_main.*

class SearchActivity : AppCompatActivity() {

    private var adapter: SearchActivity.UserAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //Set RecyclerView to use linear layout
        searchResultRecyclerView.layoutManager = LinearLayoutManager(this)
        val query = rootDB.collection("users").whereNotEqualTo("userName", null).orderBy("userName")
        //Get all the contacts in phone
        //pass query results to the recycler adapter
        val options = query.let {
            FirestoreRecyclerOptions.Builder<User>().setQuery(
                it,
                User::class.java
            ).build()
        }
        adapter = UserAdapter(options)
        searchResultRecyclerView.adapter = adapter
        searchUser.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            //this is when you press submit
            override fun onQueryTextSubmit(searchString: String): Boolean {
                val newQuery = rootDB.collection("users").whereNotEqualTo("userName", null).orderBy("userName")
                    .whereGreaterThanOrEqualTo(
                        "userName",
                        searchString
                    )
                val newOptions = newQuery.let {
                    FirestoreRecyclerOptions.Builder<User>().setQuery(
                        it,
                        User::class.java
                    ).build()
                }
                adapter?.updateOptions(newOptions)
                return true
            }

            //this actually runs each time a text change is found
            override fun onQueryTextChange(searchString: String): Boolean {
                val newQuery = rootDB.collection("users").whereNotEqualTo("userName", null).orderBy("userName")
                    .whereGreaterThanOrEqualTo(
                        "userName",
                        searchString
                    )
                val newOptions = newQuery.let {
                    FirestoreRecyclerOptions.Builder<User>().setQuery(
                        it,
                        User::class.java
                    ).build()
                }
                adapter?.updateOptions(newOptions)
                return true
            }
        })
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
        }

        return super.onOptionsItemSelected(item)
    }

    private inner class UserViewHolder internal constructor(private val view: View) : RecyclerView.ViewHolder(
        view
    )

    //Adapter class
    private inner class UserAdapter internal constructor(options: FirestoreRecyclerOptions<User>) :
        FirestoreRecyclerAdapter<User, SearchActivity.UserViewHolder>(options) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_search_user, parent, false)
            return UserViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
            Glide.with(this@SearchActivity).load(model.photoURI).into(holder.itemView.imageView_profile_picture)
            holder.itemView.user_full_name.text = model.firstName + model.lastName
            holder.itemView.username.text = model.userName

            //When clicked on the user it should take them to the personal_user_side page
            holder.itemView.setOnClickListener{
                if(model.id == authDb.currentUser?.uid) {
                    startActivity(Intent(applicationContext, PersonalActivity::class.java))
                } else {
                    val intent = Intent(applicationContext, PersonalActivityUserSide::class.java)
                    intent.putExtra("userID", model.id)
                    intent.putExtra("isPrivate", model.private)
                    if (model.followers?.contains(authDb.currentUser?.uid)!!) {
                        intent.putExtra("isFollowing", true)
                    }
                    startActivity(intent)
                }
            }
        }
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
}