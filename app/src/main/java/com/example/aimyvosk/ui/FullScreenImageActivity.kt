package com.example.aimyvosk.ui

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.aimyvosk.databinding.ActivityFullScreenImageBinding
import java.io.File

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullScreenImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.aimyvosk.utils.SystemUIUtils.enableImmersiveMode(this)
        binding = ActivityFullScreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imagePaths = intent.getStringArrayListExtra(EXTRA_IMAGE_PATHS)
        val startPosition = intent.getIntExtra(EXTRA_START_POSITION, 0)

        if (imagePaths != null) {
            val adapter = FullScreenImageAdapter(imagePaths)
            binding.viewPager.adapter = adapter
            binding.viewPager.setCurrentItem(startPosition, false)
        }
    }

    companion object {
        const val EXTRA_IMAGE_PATHS = "extra_image_paths"
        const val EXTRA_START_POSITION = "extra_start_position"
    }
}

class FullScreenImageAdapter(private val imagePaths: List<String>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<FullScreenImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: com.example.aimyvosk.databinding.ItemFullScreenImageBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ImageViewHolder {
        val binding = com.example.aimyvosk.databinding.ItemFullScreenImageBinding.inflate(
            android.view.LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imagePath = imagePaths[position]
        holder.binding.imageView.setImageURI(Uri.fromFile(File(imagePath)))
    }

    override fun getItemCount(): Int = imagePaths.size
}
