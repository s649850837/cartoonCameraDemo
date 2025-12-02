package com.example.aimyvosk.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.aimyvosk.databinding.ActivityMainBinding
import com.example.aimyvosk.utils.BitmapUtils
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.aimyvosk.utils.SystemUIUtils.enableImmersiveMode(this)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }
        viewBinding.retakeButton.setOnClickListener { 
            viewModel.clearImage()
        }
        viewBinding.saveButton.setOnClickListener {
            viewModel.processedImage.value?.let { bitmap ->
                viewModel.saveImage(bitmap)
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.processedImage.observe(this) { bitmap ->
            if (bitmap != null) {
                showResult(bitmap)
            } else {
                showPreview()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            viewBinding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            viewBinding.imageCaptureButton.isEnabled = !isLoading
        }
    }

    private fun showResult(bitmap: Bitmap) {
        viewBinding.viewFinder.visibility = View.GONE
        viewBinding.styleScrollView.visibility = View.GONE
        viewBinding.imageCaptureButton.visibility = View.GONE
        viewBinding.imageViewResult.visibility = View.VISIBLE
        viewBinding.retakeButton.visibility = View.VISIBLE
        viewBinding.saveButton.visibility = View.VISIBLE
        
        viewBinding.imageViewResult.setImageBitmap(bitmap)
    }

    private fun showPreview() {
        viewBinding.viewFinder.visibility = View.VISIBLE
        viewBinding.styleScrollView.visibility = View.VISIBLE
        viewBinding.imageCaptureButton.visibility = View.VISIBLE
        viewBinding.imageViewResult.visibility = View.GONE
        viewBinding.retakeButton.visibility = View.GONE
        viewBinding.saveButton.visibility = View.GONE
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    processImageProxy(image)
                }
            }
        )
    }

    private fun processImageProxy(image: ImageProxy) {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        
        // Handle rotation if needed
        val rotation = image.imageInfo.rotationDegrees
        val rotatedBitmap = if (rotation != 0) {
            BitmapUtils.rotateBitmap(bitmap, rotation.toFloat())
        } else {
            bitmap
        }

        val style = when {
            viewBinding.rbPixel.isChecked -> com.example.aimyvosk.data.StyleType.PIXEL_ART
            viewBinding.rbSketch.isChecked -> com.example.aimyvosk.data.StyleType.SKETCH
            viewBinding.rbCyberpunk.isChecked -> com.example.aimyvosk.data.StyleType.CYBERPUNK
            else -> com.example.aimyvosk.data.StyleType.ANIME
        }

        viewModel.processImage(rotatedBitmap, style)
        image.close()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        baseContext, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
    }
}
