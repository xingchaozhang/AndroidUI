package com.xingchaozhang.androidui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Rect
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.annotation.StyleableRes

/**
 * Extensions function for [resources][android.content.res.Resources].
 *
 * @author Nero Xie [Contact me.](mailto:yue.xie@okg.com)
 * @date 2022/7/18
 * @since 1.0.0
 */

fun <T> Int.checkResourceId(block: (Int) -> T): T? {
    if (this != 0) {
        return block(this)
    }
    return null
}

/**
 * CustomView parse xml @string
 */
fun TypedArray.getString(
    context: Context,
    @StyleableRes index: Int,
    defId: Int = 0
): CharSequence? {
    return getResourceId(index, defId).checkResourceId { id ->
        context.getText(id)
    } ?: run {
        getText(index)
    }
}

/**
 * Get screen width.This is a member of the DisplayMetrics class and is used to represent the width of the screen in pixels.
 * Specifically, widthPixels provides the absolute width of the screen, in pixels.
 * This is the actual resolution of the device and does not change as the screen orientation changes.
 * For example, if your device's resolution is 1080x1920, then widthPixels will return 1080,
 * regardless of whether your device is positioned vertically or horizontally.
 * Note that in some cases, the device's resolution may be affected by the system bar or virtual buttons.
 * To get the real physical size, you may need to use the getRealMetrics() method instead of the getMetrics() method.
 */
fun Resources.getScreenWidth(): Int {
    return displayMetrics.widthPixels
}

/**
 * Get screen height.
 */
fun Resources.getScreenHeight(): Int {
    return displayMetrics.heightPixels
}

fun Context.getRealScreenWidth(): Int {
    return getRealScreenSize()[0]
}

fun Context.getRealScreenHeight(): Int {
    return getRealScreenSize()[1]
}

/**
 * This method gets the absolute size of the screen, including the system's navigation bar.
 * If you don't want to include this part, you can use the getMetrics() method instead of the getRealMetrics() method.
 */
fun Context.getRealScreenSize(): IntArray {
    val size = IntArray(2)
    val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    if (VERSION.SDK_INT >= VERSION_CODES.R) {
        val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
        val bounds: Rect = windowMetrics.bounds
        size[0] = bounds.width()
        size[1] = bounds.height()
    } else {
        val display: Display? = windowManager.defaultDisplay
        val displayMetrics = DisplayMetrics()
        display?.getRealMetrics(displayMetrics)
        size[0] = displayMetrics.widthPixels
        size[1] = displayMetrics.heightPixels
    }
    return size
}

/**
 * Get navigation bar height.
 */
@SuppressLint("DiscouragedApi", "InternalInsetResource")
fun Context.getNavigationBarHeight(): Int {
    var navigationBarHeight = 0
    val resources = this.resources
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceId > 0) {
        navigationBarHeight = resources.getDimensionPixelSize(resourceId)
    }
    return navigationBarHeight
}

@SuppressLint("InternalInsetResource", "DiscouragedApi")
fun Context.getStatusBarHeight(): Int {
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}