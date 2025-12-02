package com.example.aimyvosk.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aimyvosk.data.ImageRepository
import com.example.aimyvosk.data.StyleType
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ImageRepository(application.applicationContext)

    private val _processedImage = MutableLiveData<Bitmap?>()
    val processedImage: LiveData<Bitmap?> = _processedImage

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun processImage(bitmap: Bitmap, style: StyleType) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.applyCartoonFilter(bitmap, style)
            _processedImage.value = result
            _isLoading.value = false
        }
    }
    
    fun clearImage() {
        _processedImage.value = null
    }

    fun saveImage(bitmap: Bitmap) {
        viewModelScope.launch {
            saveBitmapToGallery(bitmap)
        }
    }

    private suspend fun saveBitmapToGallery(bitmap: Bitmap) {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        var fos: java.io.OutputStream? = null
        var imageUri: android.net.Uri? = null
        val context = getApplication<Application>().applicationContext
        val contentResolver = context.contentResolver

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/AiMyVosk")
                }
                imageUri = contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { contentResolver.openOutputStream(it) }
            } else {
                val imagesDir = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES), "AiMyVosk")
                if (!imagesDir.exists()) imagesDir.mkdirs()
                val image = java.io.File(imagesDir, filename)
                fos = java.io.FileOutputStream(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                android.widget.Toast.makeText(context, "Saved to Gallery", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Failed to save", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
