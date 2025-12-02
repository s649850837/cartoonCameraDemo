package com.example.aimyvosk

import android.app.Application

import com.example.aimyvosk.data.MLImageProcessor

class MyApplication : Application() {

    lateinit var mlImageProcessor: MLImageProcessor
        private set

    override fun onCreate() {
        super.onCreate()
        mlImageProcessor = MLImageProcessor(this)
    }
}
