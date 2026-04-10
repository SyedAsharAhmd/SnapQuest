package com.ashar.snapquest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchAdapter(private val usernames: List<String>) :
    RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsername: TextView = itemView.findViewById(R.id.tvSearchUsername)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.tvUsername.text = "@${usernames[position]}"
    }

    override fun getItemCount(): Int = usernames.size
}