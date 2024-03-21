package com.xingchaozhang.androidui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.*
import android.widget.Checkable
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.children

/**
 * Common View extensions.
 */
private const val minTouchableAreaSize = 48
private var minTouchAreaDp: Int = 0

/**
 * Set view minimum hit rect.
 */
fun View.setHitRect(minTouchSize: Int = 0) {

    val hitRectSize: Int = if (minTouchSize == 0) {
        if (minTouchAreaDp == 0) {
            minTouchAreaDp = minTouchableAreaSize.dpInt(context)
        }
        minTouchAreaDp
    } else {
        minTouchSize
    }

    post {
        apply {
            if (parent is View) {
                val parentView = parent as View
                val touchableArea = Rect()
                getHitRect(touchableArea)
                val width = touchableArea.width()
                val height = touchableArea.height()
                if (width < hitRectSize) {
                    val offset = (hitRectSize - width) / 2
                    touchableArea.left -= offset
                    touchableArea.right += offset
                }
                if (height < hitRectSize) {
                    val offset = (hitRectSize - height) / 2
                    touchableArea.top -= offset
                    touchableArea.bottom += offset
                }
                parentView.touchDelegate.also { delegate ->
                    when (delegate) {
                        is MultiViewTouchDelegate -> {
                            delegate.addTouchDelegate(TouchDelegate(touchableArea, this@setHitRect))
                        }

                        is TouchDelegate -> {
                            parentView.touchDelegate =
                                MultiViewTouchDelegate(this@setHitRect).apply {
                                    addTouchDelegate(delegate)
                                    addTouchDelegate(TouchDelegate(touchableArea, this@setHitRect))
                                }
                        }

                        else -> {
                            parentView.touchDelegate =
                                MultiViewTouchDelegate(this@setHitRect).apply {
                                    addTouchDelegate(TouchDelegate(touchableArea, this@setHitRect))
                                }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Set view group enabled with all children.
 */
fun ViewGroup.setChildrenEnabled(enabled: Boolean) {
    children.forEach {
        if (it is ViewGroup) {
            it.setChildrenEnabled(enabled)
        }
        it.isEnabled = enabled
    }
}

/**
 * Sets the corner radius of the view clipping
 * @param radius radius you want to set.
 */
fun View.setViewCornerRadius(radius: Float) {
    if (radius < 0) {
        return
    }
    outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, radius)
        }
    }
    clipToOutline = true
}

/**
 * Create a drawable by view.
 */
fun View.viewToDrawable(width: Int = 0, height: Int = 0): Drawable? {
    this.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    val realWidth = if (width > 0) width else measuredWidth
    val realHeight = if (height > 0) height else measuredHeight
    if (realWidth == 0 || realHeight == 0) {
        // View has not been laid out yet
        return null
    }
    layout(0, 0, realWidth, realHeight)
    // Create the bitmap
    val bitmap = Bitmap.createBitmap(realWidth, realHeight, Bitmap.Config.ARGB_8888)
    // Draw the view onto the bitmap
    val canvas = Canvas(bitmap)
    draw(canvas)
    // Now create the BitmapDrawable
    return BitmapDrawable(resources, bitmap)
}

/**
 * Create a bitmap by view.
 */
fun View.viewToBitmap(width: Int = 0, height: Int = 0): Bitmap? {
    this.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    val realWidth = if (width > 0) width else measuredWidth
    val realHeight = if (height > 0) height else measuredHeight
    if (realWidth == 0 || realHeight == 0) {
        return null
    }
    // Layout the view
    layout(0, 0, realWidth, realHeight)
    // Create a bitmap with the same dimensions as the view
    val bitmap =
        Bitmap.createBitmap(realWidth, realHeight, Bitmap.Config.ARGB_8888)
    // Create a canvas and associate it with the bitmap
    val canvas = Canvas(bitmap)
    // Draw the view on the canvas
    draw(canvas)
    // Return the resulting bitmap
    return bitmap
}

/**
 * prevent double click.
 */
inline fun <T : View> T.singleClick(time: Long = 1000, crossinline block: (T) -> Unit) {
    setOnClickListener {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastClickTime > time || this is Checkable) {
            lastClickTime = currentTimeMillis
            block(this)
        }
    }
}

fun <T : View> T.singleClick(onClickListener: View.OnClickListener, time: Long = 1000) {
    setOnClickListener {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastClickTime > time || this is Checkable) {
            lastClickTime = currentTimeMillis
            onClickListener.onClick(this)
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
inline fun <T : View> T.doubleClick(crossinline block: (T) -> Unit) {
    val gestureDetector = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            block(this@doubleClick)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            this@doubleClick.performClick()
            return true
        }
    }
    val gestureDetectorCompat = GestureDetectorCompat(context, gestureDetector)
    this.setOnTouchListener { _, event ->
        gestureDetectorCompat.onTouchEvent(event)
        return@setOnTouchListener true
    }
}

var <T : View> T.lastClickTime: Long
    set(value) = setTag(1766613352, value)
    get() = getTag(1766613352) as? Long ?: 0

fun View.margin(
    left: Float? = null,
    top: Float? = null,
    right: Float? = null,
    bottom: Float? = null
) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        left?.run { leftMargin = dp2px(context) }
        top?.run { topMargin = dp2px(context) }
        right?.run { rightMargin = dp2px(context) }
        bottom?.run { bottomMargin = dp2px(context) }
    }
}

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
    if (layoutParams is T) block(layoutParams as T)
}

fun Context.getLayoutDirection(): Int {
    return resources.configuration.layoutDirection
}

fun Context.isRtl(): Boolean {
    return resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
}

/**
 * Whether the view is RTL. Will return correct value only after the view has been measured.
 * @return true if the view is RTL.
 */
fun View.isRtl(): Boolean {
    return layoutDirection == View.LAYOUT_DIRECTION_RTL
}

fun ViewGroup.MarginLayoutParams.setMarginsRelative(start: Int, top: Int, end: Int, bottom: Int) {
    setMargins(start, top, end, bottom)
    marginStart = start
    marginEnd = end
}

fun Rect.reverseForRtl(isRtl: Boolean) {
    if (isRtl) {
        val temp = left
        left = right
        right = temp
    }
}

/**
 * Convert a transparent color to opaque color.
 * In the process of converting a color with transparency to a color without transparency,
 * it is necessary to specify a background color,
 * this is because the transparency actually indicates the degree to which the foreground and background colors are mixed.
 * Transparent colors are incomplete on their own; they depend on the background for their final display.
 * @param transparentColor, transparent color you want to convert.
 * @param backgroundColor, background color when convert.
 */
fun convertTransparentToOpaqueColor(transparentColor: Int, backgroundColor: Int): Int {
    val alpha: Int = Color.alpha(transparentColor)
    val red: Int = Color.red(transparentColor)
    val green: Int = Color.green(transparentColor)
    val blue: Int = Color.blue(transparentColor)
    val backgroundRed: Int = Color.red(backgroundColor)
    val backgroundGreen: Int = Color.green(backgroundColor)
    val backgroundBlue: Int = Color.blue(backgroundColor)
    val newRed = ((1 - alpha / 255f) * backgroundRed + alpha / 255f * red).toInt()
    val newGreen = ((1 - alpha / 255f) * backgroundGreen + alpha / 255f * green).toInt()
    val newBlue = ((1 - alpha / 255f) * backgroundBlue + alpha / 255f * blue).toInt()
    return Color.rgb(newRed, newGreen, newBlue)
}