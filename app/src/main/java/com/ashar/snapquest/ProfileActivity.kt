package com.ashar.snapquest

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var rvGallery: RecyclerView
    private lateinit var tvProfileName: TextView
    private lateinit var tvPostCount: TextView
    private lateinit var ivProfilePicture: ImageView
    private val imageUrls = mutableListOf<String>()
    private lateinit var adapter: GalleryAdapter
    private lateinit var auth: FirebaseAuth
    private var viewingUserId: String? = null
    private var isOwnProfile = false

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) uploadProfilePicture(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        rvGallery = findViewById(R.id.rvGallery)
        tvProfileName = findViewById(R.id.tvProfileName)
        tvPostCount = findViewById(R.id.tvPostCount)
        ivProfilePicture = findViewById(R.id.ivProfilePicture)

        viewingUserId = intent.getStringExtra("userId") ?: auth.currentUser?.uid
        isOwnProfile = viewingUserId == auth.currentUser?.uid

        if (isOwnProfile) {
            ivProfilePicture.setOnClickListener {
                pickImage.launch("image/*")
            }
        }

        adapter = GalleryAdapter(imageUrls)
        rvGallery.layoutManager = GridLayoutManager(this, 3)
        rvGallery.adapter = adapter

        loadProfile()
        loadGallery()
    }

    private fun loadProfile() {
        val userId = viewingUserId ?: return
        val ref = FirebaseDatabase.getInstance().getReference("users").child(userId)
        ref.get().addOnSuccessListener { snapshot ->
            val username = snapshot.child("username").getValue(String::class.java) ?: "User"
            tvProfileName.text = username
            val profilePicUrl = snapshot.child("profilePicture").getValue(String::class.java)
            if (!profilePicUrl.isNullOrEmpty()) {
                Glide.with(this).load(profilePicUrl).circleCrop().into(ivProfilePicture)
            }
        }
    }

    private fun uploadProfilePicture(uri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("profiles/$userId.jpg")

        storageRef.putFile(uri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                FirebaseDatabase.getInstance().getReference("users")
                    .child(userId).child("profilePicture").setValue(downloadUrl.toString())
                Glide.with(this).load(downloadUrl).circleCrop().into(ivProfilePicture)
                Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadGallery() {
        val userId = viewingUserId ?: return
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