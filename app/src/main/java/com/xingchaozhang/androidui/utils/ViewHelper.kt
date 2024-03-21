package com.xingchaozhang.androidui.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * 针对View的一些帮助方法
 */
object ViewHelper {
    /**
     * 获取View所在的Activity
     *
     * @param context Context
     */
    fun getActivity(context: Context?): Activity? {
        when (context) {
            null -> {
                return null
            }
            is Activity -> {
                return context
            }
            is ContextWrapper -> {
                return getActivity(context.baseContext)
            }
            else -> return null
        }
    }
}
