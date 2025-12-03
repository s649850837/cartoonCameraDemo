package com.example.aimyvosk.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.aimyvosk.databinding.ActivityGalleryBinding
import java.io.File

class GalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.aimyvosk.utils.SystemUIUtils.enableImmersiveMode(this)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (checkPermissions()) {
            setupRecyclerView()
        } else {
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        requestPermissionLauncher.launch(permission)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setupRecyclerView()
        } else {
            // Handle permission denied
            binding.tvEmpty.text = "Permission denied"
            binding.tvEmpty.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        val imagesDir = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // For Android 10+, we need to query MediaStore or use the specific folder we saved to.
            // However, for simplicity in this demo, we'll look in the app-specific external storage 
            // or the public directory if we can access it.
            // Since we saved to MediaStore.Images.Media.EXTERNAL_CONTENT_URI with RELATIVE_PATH "Pictures/AiMyVosk",
            // we should query MediaStore.
            null
        } else {
            val picturesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES)
            File(picturesDir, "AiMyVosk")
        }
        
        // Note: Reading from MediaStore is the proper way for Android 10+, 
        // but for this quick implementation let's try to read from the file system 
        // if we have permission (READ_EXTERNAL_STORAGE).
        // We didn't ask for READ_EXTERNAL_STORAGE in Manifest yet.
        
        // Let's implement a simple file-based approach for now, assuming the user granted permissions 
        // or we are accessing our own files.
        // Actually, to make it robust, let's just query the MediaStore.
        
        val images = getImagesFromMediaStore()

        if (images.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            
            binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
            binding.recyclerView.adapter = GalleryAdapter(images) { file ->
                val intent = android.content.Intent(this, FullScreenImageActivity::class.java)
                val imagePaths = ArrayList(images.map { it.absolutePath })
                intent.putStringArrayListExtra(FullScreenImageActivity.EXTRA_IMAGE_PATHS, imagePaths)
                intent.putExtra(FullScreenImageActivity.EXTRA_START_POSITION, images.indexOf(file))
                startActivity(intent)
            }
        }
    }

    private fun getImagesFromMediaStore(): List<File> {
        val imageList = mutableListOf<File>()
        
        val projection = arrayOf(
            android.provider.MediaStore.Images.Media.DATA,
            android.provider.MediaStore.Images.Media.DATE_ADDED
        )
        
        // Filter for our app's images if possible, or just show all recent images
        // Ideally we filter by path "AiMyVosk"
        val selection = "${android.provider.MediaStore.Images.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%AiMyVosk%")
        
        val sortOrder = "${android.provider.MediaStore.Images.Media.DATE_ADDED} DESC"

        applicationContext.contentResolver.query(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val dataColumn = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA)
            while (cursor.moveToNext()) {
                val filePath = cursor.getString(dataColumn)
                imageList.add(File(filePath))
            }
        }
        
        return imageList
    }
}
