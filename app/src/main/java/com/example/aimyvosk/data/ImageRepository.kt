package com.example.aimyvosk.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.example.aimyvosk.MyApplication

class ImageRepository(context: Context) {

    private val mlProcessor = (context.applicationContext as MyApplication).mlImageProcessor

    suspend fun applyCartoonFilter(original: Bitmap, style: StyleType): Bitmap = withContext(Dispatchers.Default) {
        when (style) {
            StyleType.ANIME -> {
                if (mlProcessor.isModelAvailable()) {
                    return@withContext mlProcessor.process(original)
                }
                // Fallback if model missing
                return@withContext applyBasicFilter(original)
            }
            StyleType.PIXEL_ART -> {
                return@withContext applyPixelArtFilter(original)
            }
            StyleType.SKETCH -> {
                return@withContext applySketchFilter(original)
            }
            StyleType.CYBERPUNK -> {
                return@withContext applyCyberpunkFilter(original)
            }
        }
    }

    private fun applySketchFilter(original: Bitmap): Bitmap {
        val width = original.width
        val height = original.height
        val result = Bitmap.createBitmap(width, height, original.config)
        
        val canvas = Canvas(result)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix()
        // 1. Grayscale
        colorMatrix.setSaturation(0f)
        
        // 2. High Contrast / Threshold-like
        // [ 2  0  0  0 -160 ]
        // [ 0  2  0  0 -160 ]
        // [ 0  0  2  0 -160 ]
        // [ 0  0  0  1    0 ]
        val contrast = 2.0f
        val translate = -160f
        val contrastMatrix = floatArrayOf(
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )
        colorMatrix.postConcat(ColorMatrix(contrastMatrix))
        
        // 3. Invert to make it look like dark lines on white paper (optional, but "Sketch" usually means dark on light)
        // Actually, high contrast grayscale is often enough for a "noir" look. 
        // Let's try to make it look like a pencil sketch: Invert colors? No, that's negative.
        // Let's stick to High Contrast Grayscale (Noir style).
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(original, 0f, 0f, paint)
        
        return result
    }

    private fun applyCyberpunkFilter(original: Bitmap): Bitmap {
        val width = original.width
        val height = original.height
        val result = Bitmap.createBitmap(width, height, original.config)
        
        val canvas = Canvas(result)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix()
        
        // Boost Blue and Red/Pink, suppress Green
        // [ 1.2  0    0    0   0 ]
        // [ 0    0.8  0    0   0 ]
        // [ 0    0    1.5  0   0 ]
        // [ 0    0    0    1   0 ]
        val cyberpunkMatrix = floatArrayOf(
            1.2f, 0f, 0f, 0f, 0f,
            0f, 0.8f, 0f, 0f, 0f,
            0f, 0f, 1.5f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        colorMatrix.set(cyberpunkMatrix)
        
        // Increase saturation
        val satMatrix = ColorMatrix()
        satMatrix.setSaturation(1.5f)
        colorMatrix.postConcat(satMatrix)
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(original, 0f, 0f, paint)
        
        return result
    }

    private fun applyPixelArtFilter(original: Bitmap): Bitmap {
        val width = original.width
        val height = original.height
        
        // Downscale factor (higher = more pixelated)
        val pixelSize = 16f 
        val smallWidth = (width / pixelSize).toInt()
        val smallHeight = (height / pixelSize).toInt()
        
        if (smallWidth <= 0 || smallHeight <= 0) return original

        // 1. Downscale
        val smallBitmap = Bitmap.createScaledBitmap(original, smallWidth, smallHeight, false)
        
        // 2. Upscale with Nearest Neighbor (to keep blocky look)
        // Android's createScaledBitmap with filter=false uses Nearest Neighbor
        return Bitmap.createScaledBitmap(smallBitmap, width, height, false)
    }

    private fun applyBasicFilter(original: Bitmap): Bitmap {
        val width = original.width
        val height = original.height
        val result = Bitmap.createBitmap(width, height, original.config)
        
        val canvas = Canvas(result)
        val paint = Paint()
        
        // Step 1: Increase Saturation
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(1.5f) // Boost saturation
        
        // Step 2: Increase Contrast
        val contrast = 1.2f
        val scale = contrast
        val translate = (-.5f * scale + .5f) * 255.0f
        val contrastMatrix = floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )
        colorMatrix.postConcat(ColorMatrix(contrastMatrix))
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(original, 0f, 0f, paint)

        return result
    }
}
