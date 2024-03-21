package com.xingchaozhang.androidui

import android.app.Application

class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        UIEnv.init(this)
    }
}