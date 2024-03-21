package com.xingchaozhang.androidui

import android.util.AttributeSet

/**
 * @description : Appearance interface.
 */
interface OKAppearance {

    /**
     * Load appearance from [attributes][attrs] with [default style][defStyleAttr].
     */
    fun loadFromAttributes(attrs: AttributeSet?, defStyleAttr: Int = 0, defStyle: Int = 0)
}