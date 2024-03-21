package com.xingchaozhang.androidui.utils

import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.xingchaozhang.androidui.R
import java.lang.reflect.Field

/**
 * Extensions function for [TextView]
 *
 * @author Nero Xie [Contact me.](mailto:yue.xie@okg.com)
 * @date 2022/9/21
 * @since 6.1.8
 */
fun TextView.setTextOrGone(content: CharSequence?) {
    if (content.isNullOrEmpty()) {
        visibility = View.GONE
    } else {
        visibility = View.VISIBLE
        text = content
    }
}

fun TextView.setCursorDrawable(
    color: Int = ContextCompat.getColor(context, R.color.ContentPrimary),
    width: Int = 6,
    radius: Float = 0f
) {
    val drawable = GradientDrawable()
    drawable.apply {
        setColor(color)
        cornerRadius = radius
        setSize(width, -1)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this.textCursorDrawable = drawable
    } else {
        try {
            val field: Field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            field.isAccessible = true
            field.set(this, drawable)
        } catch (e: Exception) {
            Log.e("TextView", "setCursorDrawable : $e")
        }
    }
}

fun EditText.showKeyboardAndFocus() {
    this.requestFocus()
    this.isEnabled = true
    this.isFocusable = true
    this.isFocusableInTouchMode = true
    showSoftInputOnFocus
}