package com.example.aimyvosk.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.aimyvosk.R
import java.io.File

class GalleryAdapter(
    private val images: List<File>,
    private val onImageClick: (File) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageFile = images[position]
        val bitmap = android.graphics.BitmapFactory.decodeFile(imageFile.absolutePath)
        holder.imageView.setImageBitmap(bitmap)
        
        holder.itemView.setOnClickListener {
            onImageClick(imageFile)
        }
    }

    override fun getItemCount() = images.size
}
