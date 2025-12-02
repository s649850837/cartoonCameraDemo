package com.example.aimyvosk.data

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.ByteBuffer

class MLImageProcessor(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val modelName = "cartoon_gan.tflite"

    init {
        try {
            val modelFile = FileUtil.loadMappedFile(context, modelName)
            val options = Interpreter.Options()
            interpreter = Interpreter(modelFile, options)
        } catch (e: Exception) {
            // Model not found or error loading
            e.printStackTrace()
        }
    }

    fun isModelAvailable(): Boolean {
        return interpreter != null
    }

    fun process(inputBitmap: Bitmap): Bitmap {
        val tflite = interpreter ?: return inputBitmap

        // 1. Pre-process
        // CartoonGAN model (whitebox_cartoon_gan_dr) expects 512x512 input
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(512, 512, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(127.5f, 127.5f)) // [0, 255] -> [-1, 1]
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(inputBitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // 2. Run Inference
        
        // Output is 1x512x512x3
        val outputTensorBuffer = ByteBuffer.allocateDirect(1 * 512 * 512 * 3 * 4) // Float is 4 bytes
        outputTensorBuffer.order(java.nio.ByteOrder.nativeOrder())
        
        tflite.run(tensorImage.buffer, outputTensorBuffer)

        // 3. Post-process
        outputTensorBuffer.rewind()
        val outputBitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        
        val pixels = IntArray(512 * 512)
        for (i in 0 until 512 * 512) {
            val r = ((outputTensorBuffer.float + 1) * 127.5f).toInt().coerceIn(0, 255)
            val g = ((outputTensorBuffer.float + 1) * 127.5f).toInt().coerceIn(0, 255)
            val b = ((outputTensorBuffer.float + 1) * 127.5f).toInt().coerceIn(0, 255)
            pixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
        
        outputBitmap.setPixels(pixels, 0, 512, 0, 0, 512, 512)
        
        // Resize back to original size if needed, or return 512x512
        return Bitmap.createScaledBitmap(outputBitmap, inputBitmap.width, inputBitmap.height, true)
    }
}
