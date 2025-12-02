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

        val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
        if (imagePath != null) {
            binding.fullScreenImageView.setImageURI(Uri.fromFile(File(imagePath)))
        }
    }

    companion object {
        const val EXTRA_IMAGE_PATH = "extra_image_path"
    }
}
