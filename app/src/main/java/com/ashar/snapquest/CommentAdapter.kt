package com.ashar.snapquest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase

data class Comment(
    val username: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    val userId: String = ""
)

class CommentAdapter(private val comments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCommentUsername: TextView = itemView.findViewById(R.id.tvCommentUsername)
        val tvCommentText: TextView = itemView.findViewById(R.id.tvCommentText)
        val ivCommentProfilePic: ImageView = itemView.findViewById(R.id.ivCommentProfilePic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.tvCommentUsername.text = "@${comment.username}"
        holder.tvCommentText.text = comment.text

        if (comment.userId.isNotEmpty()) {
            FirebaseDatabase.getInstance().getReference("users")
                .child(comment.userId).child("profilePicture").get()
                .addOnSuccessListener { snapshot ->
                    val url = snapshot.getValue(String::class.java)
                    Glide.with(holder.itemView.context)
                        .load(url)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .circleCrop()
                        .into(holder.ivCommentProfilePic)
                }
        }
    }

    override fun getItemCount(): Int = comments.size
}