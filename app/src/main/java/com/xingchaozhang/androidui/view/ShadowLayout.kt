package com.xingchaozhang.androidui.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.xingchaozhang.androidui.R
import java.lang.Float.max
import kotlin.math.abs

/**
 * @description : Use to add a shadow to any View.
 * refer to this, Please visit :https://github.com/lihangleo2/ShadowLayout to see more details.
 */
open class ShadowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    /**
     * shadow color
     */
    var shadowColor = 0
        set(value) {
            field = value
            invalidateShadow()
        }

    /**
     * The spread of the shadows (which can also be interpreted as the degree of spread)
     */
    var shadowSpread = 0f
        set(value) {
            field = value
            invalidateShadow()
        }

    /**
     * The spread of the shadow on the upper edge of the X-axis
     */
    var shadowSpreadX = 0f
        set(value) {
            field = value
            invalidateShadow()
        }

    /**
     * The spread of the shadow on the upper edge of the Y-axis
     */
    var shadowSpreadY = 0f
        set(value) {
            field = value
            invalidateShadow()
        }

    /**
     * Rounding size of shadows
     */
    var cornerRadius = 0f
        set(value) {
            field = value
            invalidateShadow()
        }

    /**
     * Offset of x-axis
     */
    var xOffset = 0f
        set(value) {
            field = value
            invalidateShadow()
        }

    /**
     * Offset of y-axis
     */
    var yOffset = 0f
        set(value) {
            field = value
            invalidateShadow()
        }

    /**
     * Whether to show shadows on the left.
     */
    var showLeftShadow = false
        set(value) {
            field = value
            invalidateShadow()
        }

    /**
     * Whether to show shadows on the right.
     */
    var showRightShadow = false
        set(value) {
            field = value
            invalidateShadow()
        }

    /**
     * Whether to show shadows on the top.
     */
    var showTopShadow = false
        set(value) {
            field = value
            invalidateShadow()
        }

    /**
     * Whether to show shadows on the bottom.
     */
    var showBottomShadow = false
        set(value) {
            field = value
            invalidateShadow()
        }
    private var invalidateShadowOnSizeChanged = true
    private var forceInvalidateShadow = false

    init {
        initView(context, attrs)
    }

    fun setInvalidateShadowOnSizeChanged(invalidateShadowOnSizeChanged: Boolean) {
        this.invalidateShadowOnSizeChanged = invalidateShadowOnSizeChanged
    }

    override fun getSuggestedMinimumWidth(): Int {
        return 0
    }

    override fun getSuggestedMinimumHeight(): Int {
        return 0
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && (background == null || invalidateShadowOnSizeChanged
                    || forceInvalidateShadow)
        ) {
            forceInvalidateShadow = false
            setBackgroundCompat(w, h)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (forceInvalidateShadow) {
            forceInvalidateShadow = false
            setBackgroundCompat(right - left, bottom - top)
        }
    }

    fun invalidateShadow() {
        forceInvalidateShadow = true
        setShadowPadding()
        requestLayout()
        invalidate()
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        initAttributes(context, attrs)
        setShadowPadding()
    }

    private fun initAttributes(context: Context, attrs: AttributeSet?) {
        val attr = getTypedArray(context, attrs, R.styleable.ShadowLayout)
        // 默认是显示
        showLeftShadow = attr.getBoolean(R.styleable.ShadowLayout_ok_showLeftShadow, true)
        showRightShadow = attr.getBoolean(R.styleable.ShadowLayout_ok_showRightShadow, true)
        showBottomShadow = attr.getBoolean(R.styleable.ShadowLayout_ok_showBottomShadow, true)
        showTopShadow = attr.getBoolean(R.styleable.ShadowLayout_ok_showTopShadow, true)
        cornerRadius = attr.getDimension(R.styleable.ShadowLayout_ok_shadowCornerRadius, 0f)
        shadowSpread = attr.getDimension(R.styleable.ShadowLayout_ok_shadowSpread, 0f)
        xOffset = attr.getDimension(R.styleable.ShadowLayout_ok_shadowXOffset, 0f)
        yOffset = attr.getDimension(R.styleable.ShadowLayout_ok_shadowYOffset, 0f)
        shadowColor = attr.getColor(
            R.styleable.ShadowLayout_ok_shadowColor,
            ContextCompat.getColor(context, R.color.ContentDisabled)
        )
        attr.recycle()
    }

    private fun setShadowPadding() {
        val xPadding = if (shadowSpreadX > 0f) {
            (shadowSpreadX + abs(xOffset)).toInt()
        } else {
            (shadowSpread + abs(xOffset)).toInt()
        }
        val yPadding = if (shadowSpreadY > 0f) {
            (shadowSpreadY + abs(yOffset)).toInt()
        } else {
            (shadowSpread + abs(yOffset)).toInt()
        }
        val left: Int = if (showLeftShadow) {
            xPadding
        } else {
            0
        }
        val top: Int = if (showTopShadow) {
            yPadding
        } else {
            0
        }
        val right: Int = if (showRightShadow) {
            xPadding
        } else {
            0
        }
        val bottom: Int = if (showBottomShadow) {
            yPadding
        } else {
            0
        }
        setPaddingRelative(left, top, right, bottom)
    }

    private fun setBackgroundCompat(w: Int, h: Int) {
        val bitmap = createShadowBitmap(w, h, Color.TRANSPARENT) ?: return
        val drawable = BitmapDrawable(resources, bitmap)
        background = drawable
    }

    private fun getTypedArray(
        context: Context,
        attributeSet: AttributeSet?,
        attr: IntArray
    ): TypedArray {
        return context.obtainStyledAttributes(attributeSet, attr, 0, 0)
    }

    private fun createShadowBitmap(shadowWidth: Int, shadowHeight: Int, fillColor: Int): Bitmap? {
        if (shadowWidth <= 0 || shadowHeight <= 0) {
            return null
        }
        // 根据宽高创建bitmap背景
        val output = Bitmap.createBitmap(shadowWidth, shadowHeight, Bitmap.Config.ARGB_8888)
        // 用画板canvas进行绘制
        val canvas = Canvas(output)
        val shadowRect = if (shadowSpreadX > 0f && shadowSpreadY > 0f) {
            RectF(
                shadowSpreadX, shadowSpreadY,
                shadowWidth - shadowSpreadX, shadowHeight - shadowSpreadY
            )
        } else if (shadowSpreadX > 0f) {
            RectF(
                shadowSpreadX, shadowSpread,
                shadowWidth - shadowSpreadX, shadowHeight - shadowSpread
            )
        } else if (shadowSpreadY > 0f) {
            RectF(
                shadowSpread, shadowSpreadY,
                shadowWidth - shadowSpread, shadowHeight - shadowSpreadY
            )
        } else {
            RectF(
                shadowSpread, shadowSpread,
                shadowWidth - shadowSpread, shadowHeight - shadowSpread
            )
        }
        if (yOffset > 0) {
            shadowRect.top += yOffset
            shadowRect.bottom -= yOffset
        } else if (yOffset < 0) {
            shadowRect.top += abs(yOffset)
            shadowRect.bottom -= abs(yOffset)
        }
        if (xOffset > 0) {
            shadowRect.left += xOffset
            shadowRect.right -= xOffset
        } else if (xOffset < 0) {
            shadowRect.left += abs(xOffset)
            shadowRect.right -= abs(xOffset)
        }
        val shadowPaint = Paint().apply {
            isAntiAlias = true
            color = fillColor
            style = Paint.Style.FILL
        }
        if (!isInEditMode) {
            val maxRadius = max(shadowSpreadX, shadowSpreadY)
            val shadowRadius = if (maxRadius > 0) maxRadius else shadowSpread
            shadowPaint.setShadowLayer(shadowRadius, xOffset, yOffset, shadowColor)
        }
        canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, shadowPaint)
        return output
    }
}