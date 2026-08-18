package com.ashar.snapquest

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PostAdapter(private val posts: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val pendingLikes = mutableSetOf<String>()

    private fun sendNotification(token: String, title: String, body: String) {
        FirebaseDatabase.getInstance().getReference("notifications").push().setValue(
            mapOf(
                "token" to token,
                "title" to title,
                "body" to body,
                "timestamp" to System.currentTimeMillis()
            )
        )
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        val ivUserProfilePic: ImageView = itemView.findViewById(R.id.ivUserProfilePic)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvCaption: TextView = itemView.findViewById(R.id.tvCaption)
        val tvLikes: TextView = itemView.findViewById(R.id.tvLikes)
        val tvComments: TextView = itemView.findViewById(R.id.tvComments)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.tvUsername.text = "@${post.username}"
        holder.tvDate.text = post.date
        holder.tvCaption.text = post.caption
        holder.tvLikes.text = "♡ ${post.likes}"
        holder.tvComments.text = "💬 ${post.comments}"

        Glide.with(holder.itemView.context)
            .load(post.imageUrl)
            .into(holder.ivPostImage)

        Glide.with(holder.itemView.context)
            .load(post.profilePicture.ifEmpty { null })
            .placeholder(R.drawable.default_avatar)
            .error(R.drawable.default_avatar)
            .circleCrop()
            .into(holder.ivUserProfilePic)

        holder.tvLikes.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser ?: return@setOnClickListener
            val uid = currentUser.uid
            val postKey = "${post.userId}_${post.date}"

            if (pendingLikes.contains(postKey)) return@setOnClickListener
            pendingLikes.add(postKey)

            val likesRef = FirebaseDatabase.getInstance().getReference("posts")
                .child(post.userId).child(post.date).child("likedBy").child(uid)
            val countRef = FirebaseDatabase.getInstance().getReference("posts")
                .child(post.userId).child(post.date).child("likes")

            likesRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    likesRef.removeValue()
                    countRef.get().addOnSuccessListener { countSnapshot ->
                        val currentLikes = countSnapshot.getValue(Int::class.java) ?: 0
                        val newLikes = if (currentLikes > 0) currentLikes - 1 else 0
                        countRef.setValue(newLikes)
                        post.likes = newLikes
                        holder.tvLikes.text = "♡ $newLikes"
                        pendingLikes.remove(postKey)
                    }
                } else {
                    likesRef.setValue(true)
                    countRef.get().addOnSuccessListener { countSnapshot ->
                        val currentLikes = countSnapshot.getValue(Int::class.java) ?: 0
                        val newLikes = currentLikes + 1
                        countRef.setValue(newLikes)
                        post.likes = newLikes
                        holder.tvLikes.text = "♡ $newLikes"
                        pendingLikes.remove(postKey)

                        FirebaseDatabase.getInstance().getReference("users")
                            .child(uid).child("username").get()
                            .addOnSuccessListener { usernameSnapshot ->
                                val username = usernameSnapshot.getValue(String::class.java) ?: "Someone"
                                FirebaseDatabase.getInstance().getReference("users")
                                    .child(post.userId).child("fcmToken").get()
                                    .addOnSuccessListener { tokenSnapshot ->
                                        val token = tokenSnapshot.getValue(String::class.java)
                                        if (token != null && post.userId != uid) {
                                            sendNotification(token, "New Like!", "$username liked your photo")
                                        }
                                    }
                            }
                    }
                }
            }.addOnFailureListener {
                pendingLikes.remove(postKey)
            }
        }

        holder.tvComments.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, CommentsActivity::class.java)
            intent.putExtra("postUserId", post.userId)
            intent.putExtra("postDate", post.date)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = posts.size
}