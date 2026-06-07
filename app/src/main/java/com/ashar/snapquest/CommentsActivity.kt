package com.ashar.snapquest

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommentsActivity : AppCompatActivity() {

    private lateinit var rvComments: RecyclerView
    private lateinit var etComment: EditText
    private lateinit var btnPostComment: Button
    private val comments = mutableListOf<Comment>()
    private lateinit var adapter: CommentAdapter
    private lateinit var postUserId: String
    private lateinit var postDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        postUserId = intent.getStringExtra("postUserId") ?: ""
        postDate = intent.getStringExtra("postDate") ?: ""

        rvComments = findViewById(R.id.rvComments)
        etComment = findViewById(R.id.etComment)
        btnPostComment = findViewById(R.id.btnPostComment)

        adapter = CommentAdapter(comments)
        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter = adapter

        loadComments()

        btnPostComment.setOnClickListener {
            val text = etComment.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Write a comment first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            postComment(text)
        }
    }

    private fun loadComments() {
        val ref = FirebaseDatabase.getInstance().getReference("posts")
            .child(postUserId).child(postDate).child("comments")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                comments.clear()
                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(Comment::class.java)
                    if (comment != null) comments.add(comment)
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun postComment(text: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        // fetch username from database instead of using Auth
        FirebaseDatabase.getInstance().getReference("users")
            .child(currentUser.uid).child("username").get()
            .addOnSuccessListener { snapshot ->
                val username = snapshot.getValue(String::class.java) ?: "user"

                val ref = FirebaseDatabase.getInstance().getReference("posts")
                    .child(postUserId).child(postDate).child("comments").push()

                val comment = Comment(
                    username = username,
                    text = text,
                    timestamp = System.currentTimeMillis(),
                    userId = currentUser.uid
                )

                ref.setValue(comment).addOnSuccessListener {
                    etComment.text.clear()
                    val countRef = FirebaseDatabase.getInstance().getReference("posts")
                        .child(postUserId).child(postDate).child("commentCount")
                    countRef.get().addOnSuccessListener { countSnapshot ->
                        val current = countSnapshot.getValue(Int::class.java) ?: 0
                        countRef.setValue(current + 1)
                    }
                }
            }
    }
}