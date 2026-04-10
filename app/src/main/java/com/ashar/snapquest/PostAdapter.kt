package com.ashar.snapquest

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PostAdapter(private val posts: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
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

        holder.tvLikes.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser ?: return@setOnClickListener
            val uid = currentUser.uid
            val likesRef = FirebaseDatabase.getInstance().getReference("posts")
                .child(post.userId).child(post.date).child("likedBy").child(uid)
            val countRef = FirebaseDatabase.getInstance().getReference("posts")
                .child(post.userId).child(post.date).child("likes")

            likesRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    likesRef.removeValue()
                    countRef.get().addOnSuccessListener { countSnapshot ->
                        val currentLikes = countSnapshot.getValue(Int::class.java) ?: 0
                        if (currentLikes > 0) countRef.setValue(currentLikes - 1)
                    }
                } else {
                    likesRef.setValue(true)
                    countRef.get().addOnSuccessListener { countSnapshot ->
                        val currentLikes = countSnapshot.getValue(Int::class.java) ?: 0
                        countRef.setValue(currentLikes + 1)
                    }
                }
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