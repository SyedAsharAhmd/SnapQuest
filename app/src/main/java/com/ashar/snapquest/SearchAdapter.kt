package com.ashar.snapquest

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SearchAdapter(private val users: List<Pair<String, String>>) :
    RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsername: TextView = itemView.findViewById(R.id.tvSearchUsername)
        val ivProfilePic: ImageView = itemView.findViewById(R.id.ivProfilePic)

        fun bind(userId: String, username: String, profilePicUrl: String?) {
            tvUsername.text = "@$username"
            if (!profilePicUrl.isNullOrEmpty()) {
                Glide.with(itemView.context).load(profilePicUrl).into(ivProfilePic)
            }
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ProfileActivity::class.java)
                intent.putExtra("userId", userId)
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val (userId, username) = users[position]
        holder.bind(userId, username, "")
    }

    override fun getItemCount(): Int = users.size
}