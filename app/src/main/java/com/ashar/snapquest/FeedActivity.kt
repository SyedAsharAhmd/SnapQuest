package com.ashar.snapquest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FeedActivity : AppCompatActivity() {

    private lateinit var rvFeed: RecyclerView
    private val posts = mutableListOf<Post>()
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        rvFeed = findViewById(R.id.rvFeed)
        adapter = PostAdapter(posts)
        rvFeed.layoutManager = LinearLayoutManager(this)
        rvFeed.adapter = adapter

        loadPosts()
    }

    private fun loadPosts() {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("posts")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                posts.clear()
                for (userSnapshot in snapshot.children) {
                    for (dateSnapshot in userSnapshot.children) {
                        val imageUrl = dateSnapshot.child("imageUrl").getValue(String::class.java) ?: ""
                        val userId = dateSnapshot.child("userId").getValue(String::class.java) ?: ""
                        val timestamp = dateSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                        val caption = dateSnapshot.child("caption").getValue(String::class.java) ?: ""
                        val date = dateSnapshot.key ?: ""

                        val post = Post(
                            userId = userId,
                            username = dateSnapshot.child("username").getValue(String::class.java) ?: userId.take(8),                            imageUrl = imageUrl,
                            caption = caption,
                            date = date,
                            likes = dateSnapshot.child("likes").getValue(Int::class.java) ?: 0,
                            comments = dateSnapshot.child("commentCount").getValue(Int::class.java) ?: 0,
                        )
                        posts.add(post)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}