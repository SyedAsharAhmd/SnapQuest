package com.ashar.snapquest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrendingActivity : AppCompatActivity() {

    private lateinit var rvTrending: RecyclerView
    private val posts = mutableListOf<Post>()
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trending)

        rvTrending = findViewById(R.id.rvTrending)
        adapter = PostAdapter(posts)
        rvTrending.layoutManager = LinearLayoutManager(this)
        rvTrending.adapter = adapter

        loadTrending()
    }

    private fun loadTrending() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val ref = FirebaseDatabase.getInstance().getReference("posts")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                posts.clear()
                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: ""
                    val dateSnapshot = userSnapshot.child(today)
                    if (dateSnapshot.exists()) {
                        val imageUrl = dateSnapshot.child("imageUrl").getValue(String::class.java) ?: ""
                        val uid = dateSnapshot.child("userId").getValue(String::class.java) ?: ""
                        val caption = dateSnapshot.child("caption").getValue(String::class.java) ?: ""
                        val likes = dateSnapshot.child("likes").getValue(Int::class.java) ?: 0
                        val comments = dateSnapshot.child("commentCount").getValue(Int::class.java) ?: 0

                        FirebaseDatabase.getInstance().getReference("users").child(userId).get()
                            .addOnSuccessListener { userSnap ->
                                val username = userSnap.child("username").getValue(String::class.java) ?: uid.take(8)
                                val profilePic = userSnap.child("profilePicture").getValue(String::class.java) ?: ""
                                posts.add(Post(
                                    userId = uid,
                                    username = username,
                                    imageUrl = imageUrl,
                                    caption = caption,
                                    date = today,
                                    likes = likes,
                                    comments = comments,
                                    profilePicture = profilePic
                                ))
                                posts.sortByDescending { it.likes }
                                adapter.notifyDataSetChanged()
                            }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}