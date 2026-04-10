package com.ashar.snapquest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Comment(
    val username: String = "",
    val text: String = "",
    val timestamp: Long = 0
)

class CommentAdapter(private val comments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCommentUsername: TextView = itemView.findViewById(R.id.tvCommentUsername)
        val tvCommentText: TextView = itemView.findViewById(R.id.tvCommentText)
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
    }

    override fun getItemCount(): Int = comments.size
}