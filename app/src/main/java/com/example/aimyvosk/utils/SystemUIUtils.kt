package com.example.aimyvosk.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object SystemUIUtils {

    fun enableImmersiveMode(activity: Activity) {
        // Make content appear behind the status bar and navigation bar
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)

        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)

        // Configure the behavior of the system bars
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        // Hide the system bars (optional, or just make them transparent)
        // For "Immersive", usually we want them hidden or transparent.
        // Let's make them transparent and let content draw behind.
        
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        
        // If we want to hide them completely:
        // controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}
