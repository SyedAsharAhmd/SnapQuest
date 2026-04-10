package com.ashar.snapquest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GalleryAdapter(private val imageUrls: List<String>) :
    RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    inner class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivGalleryImage: ImageView = itemView.findViewById(R.id.ivGalleryImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(imageUrls[position])
            .into(holder.ivGalleryImage)
    }

    override fun getItemCount(): Int = imageUrls.size
}