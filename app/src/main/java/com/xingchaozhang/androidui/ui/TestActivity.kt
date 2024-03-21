package com.xingchaozhang.androidui.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.core.content.res.TypedArrayUtils
import androidx.core.view.WindowCompat
import com.xingchaozhang.androidui.R
import com.xingchaozhang.androidui.databinding.ActivityTestBinding

class TestActivity : BaseThemeActivity() {

    private lateinit var binding: ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.initView()
    }

    @SuppressLint("RestrictedApi")
    private fun ActivityTestBinding.initView() {
        window.statusBarColor = Color.TRANSPARENT
        toolbar.apply {
            setSupportActionBar(this)
        }
        tab.addTab(tab.newTab().setText("Tab"), true)
        tab.addTab(tab.newTab().setText("Tab"))
        tab.addTab(tab.newTab().setText("Tab"))
    }
}