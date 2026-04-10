package com.ashar.snapquest

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var rvGallery: RecyclerView
    private lateinit var tvProfileName: TextView
    private lateinit var tvPostCount: TextView
    private val imageUrls = mutableListOf<String>()
    private lateinit var adapter: GalleryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        rvGallery = findViewById(R.id.rvGallery)
        tvProfileName = findViewById(R.id.tvProfileName)
        tvPostCount = findViewById(R.id.tvPostCount)

        val currentUser = FirebaseAuth.getInstance().currentUser
        tvProfileName.text = currentUser?.displayName ?: currentUser?.email ?: "User"

        adapter = GalleryAdapter(imageUrls)
        rvGallery.layoutManager = GridLayoutManager(this, 3)
        rvGallery.adapter = adapter

        loadGallery()
    }

    private fun loadGallery() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("posts").child(userId)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                imageUrls.clear()
                for (dateSnapshot in snapshot.children) {
                    val imageUrl = dateSnapshot.child("imageUrl").getValue(String::class.java)
                    if (imageUrl != null) imageUrls.add(imageUrl)
                }
                tvPostCount.text = "${imageUrls.size} photos"
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}