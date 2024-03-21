package com.xingchaozhang.androidui.edit.input

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.xingchaozhang.androidui.OKAppearance
import com.xingchaozhang.androidui.R
import com.xingchaozhang.androidui.utils.dp2pxFloat
import com.xingchaozhang.androidui.utils.dpInt
import com.xingchaozhang.androidui.utils.sp2pxFloat


/**
 * @description : Appearance for ok input.
 */
internal class InputAppearance(val input: Input) : OKAppearance {
    var fieldRadius = 0F
    var strokeWidth = 0f

    /**
     * text size.
     */
    var labelSMTextSize: Float = 0f
    var contentSMTextSize: Float = 0f
    var errorSMTextSize: Float = 0f
    var contentSMMarginHorizontal = 0

    var labelMDTextSize: Float = 0f
    var contentMDTextSize: Float = 0f
    var errorMDTextSize: Float = 0f
    var contentMDMarginHorizontal = 0

    var labelLGTextSize: Float = 0f
    var contentLGTextSize: Float = 0f
    var errorLGTextSize: Float = 0f
    var contentLGMarginHorizontal = 0

    var labelXLTextSize: Float = 0f
    var contentXLTextSize: Float = 0f
    var errorXLTextSize: Float = 0f
    var contentXLMarginHorizontal = 0

    var labelHighLightLGTextSize: Float = 0f
    var contentHighLightLGTextSize: Float = 0f
    var errorHighLightLGTextSize: Float = 0f
    var onlyBottomLayerMarginHorizontal = 0

    /**
     * Background Color.
     */
    var contentStaticColor: Int = 0
    var contentActiveColor: Int = 0
    var contentCriticalColor: Int = 0
    var contentDisableColor: Int = 0

    /**
     * Layer color
     */
    var contentLayerStaticColor: Int = 0
    var contentLayerActiveColor: Int = 0
    var contentLayerCriticalColor: Int = 0
    var contentLayerDisableColor: Int = 0

    var contentStaticTextColor: Int = 0
    var contentActiveTextColor: Int = 0
    var contentCriticalTextColor: Int = 0
    var contentDisableTextColor: Int = 0

    var labelOutTextColor = 0
    var labelInTextColor = 0
    var hintTextColor = 0
    var cursorColor = 0
    var tintColor: Int = 0
    var errorTextColor = 0
    /**
     * Auto Scaling.
     */
    var autoScale: Boolean = false
    var clearDrawable :Int = 0
    var clearDrawableSize :Int = 0
    var clearMiniSize :Int = 0
    var passwordChar : Char = '·'
    /**
     * Set the font style of label and errorText
     */
    var labelTextAppearance: Int = 0
    var contentTextAppearance: Int = 0
    var helperTextAppearance: Int = 0
    var errorTextAppearance: Int = 0

    override fun loadFromAttributes(attrs: AttributeSet?, defStyleAttr: Int, defStyle: Int) {
        val typedArray = input.context.obtainStyledAttributes(attrs, R.styleable.InputAppearance, defStyleAttr, defStyle)
        fieldRadius = typedArray.getDimension(R.styleable.InputAppearance_MDRadius, 6f)
        strokeWidth = typedArray.getDimension(R.styleable.InputAppearance_strokeWidth, 1f)
        // get text size.
        labelSMTextSize= typedArray.getDimension(R.styleable.InputAppearance_labelSMTextSize, 14f.sp2pxFloat(input.context))
        contentSMTextSize = typedArray.getDimension(R.styleable.InputAppearance_contentSMTextSize, 14f.sp2pxFloat(input.context))
        errorSMTextSize = typedArray.getDimension(R.styleable.InputAppearance_errorSMTextSize, 14f.sp2pxFloat(input.context))
        contentSMMarginHorizontal = typedArray.getDimensionPixelSize(R.styleable.InputAppearance_content_marginSMHorizontal, 8.dpInt(input.context))

        labelMDTextSize = typedArray.getDimension(R.styleable.InputAppearance_labelMDTextSize, 14f.sp2pxFloat(input.context))
        contentMDTextSize = typedArray.getDimension(R.styleable.InputAppearance_contentMDTextSize, 14f.sp2pxFloat(input.context))
        errorMDTextSize = typedArray.getDimension(R.styleable.InputAppearance_errorMDTextSize, 14f.sp2pxFloat(input.context))
        contentMDMarginHorizontal = typedArray.getDimensionPixelSize(R.styleable.InputAppearance_content_marginMDHorizontal, 8.dpInt(input.context))

        labelLGTextSize = typedArray.getDimension(R.styleable.InputAppearance_labelLGTextSize, 14f.sp2pxFloat(input.context))
        contentLGTextSize = typedArray.getDimension(R.styleable.InputAppearance_contentLGTextSize, 14f.sp2pxFloat(input.context))
        errorLGTextSize= typedArray.getDimension(R.styleable.InputAppearance_errorLGTextSize, 14f.sp2pxFloat(input.context))
        contentLGMarginHorizontal = typedArray.getDimensionPixelSize(R.styleable.InputAppearance_content_marginLGHorizontal, 8.dpInt(input.context))

        labelXLTextSize = typedArray.getDimension(R.styleable.InputAppearance_labelXLTextSize, 14f.sp2pxFloat(input.context))
        contentXLTextSize = typedArray.getDimension(R.styleable.InputAppearance_contentXLTextSize, 14f.sp2pxFloat(input.context))
        errorXLTextSize = typedArray.getDimension(R.styleable.InputAppearance_errorXLTextSize, 14f.sp2pxFloat(input.context))
        contentXLMarginHorizontal = typedArray.getDimensionPixelSize(R.styleable.InputAppearance_content_marginXLHorizontal, 8.dpInt(input.context))

        labelHighLightLGTextSize = typedArray.getDimension(R.styleable.InputAppearance_labelHighLightLGTextSize, 14f.sp2pxFloat(input.context))
        contentHighLightLGTextSize = typedArray.getDimension(R.styleable.InputAppearance_contentHighLightLGTextSize, 40f.sp2pxFloat(input.context))
        errorHighLightLGTextSize = typedArray.getDimension(R.styleable.InputAppearance_errorHighLightLGTextSize, 14f.sp2pxFloat(input.context))
        onlyBottomLayerMarginHorizontal = typedArray.getDimensionPixelSize(R.styleable.InputAppearance_onlyBottomLayerMarginHorizontal, 0)

        /**
         * background color.
         */
        contentStaticColor = typedArray.getColor(R.styleable.InputAppearance_content_staticColor, ContextCompat.getColor(input.context, R.color.ContainerPrimary))
        contentActiveColor = typedArray.getColor(R.styleable.InputAppearance_content_activeColor, ContextCompat.getColor(input.context, R.color.ContainerPrimary))
        contentCriticalColor = typedArray.getColor(R.styleable.InputAppearance_content_criticalColor, ContextCompat.getColor(input.context, R.color.ContainerPrimary))
        contentDisableColor = typedArray.getColor(R.styleable.InputAppearance_content_disableColor, ContextCompat.getColor(input.context, R.color.ContainerPrimary))

        contentLayerActiveColor = typedArray.getColor(R.styleable.InputAppearance_activeLayerColor, ContextCompat.getColor(input.context, R.color.ContainerPrimary))
        contentLayerStaticColor = typedArray.getColor(R.styleable.InputAppearance_staticLayerColor, ContextCompat.getColor(input.context, R.color.ContainerPrimary))
        contentLayerCriticalColor = typedArray.getColor(R.styleable.InputAppearance_criticalLayerColor, ContextCompat.getColor(input.context, R.color.ContainerPrimary))
        contentLayerDisableColor = typedArray.getColor(R.styleable.InputAppearance_disableLayerColor, ContextCompat.getColor(input.context, R.color.ContainerPrimary))

        // get text color
        contentStaticTextColor = typedArray.getColor(R.styleable.InputAppearance_content_staticTextColor, ContextCompat.getColor(input.context, R.color.ContainerPrimary))
        contentActiveTextColor = typedArray.getColor(R.styleable.InputAppearance_content_activeTextColor, ContextCompat.getColor(input.context, R.color.ContainerPrimary))
        contentCriticalTextColor = typedArray.getColor(R.styleable.InputAppearance_content_criticalTextColor, ContextCompat.getColor(input.context, R.color.ContainerPrimary))
        contentDisableTextColor = typedArray.getColor(R.styleable.InputAppearance_content_disableTextColor, ContextCompat.getColor(input.context, R.color.ContainerPrimary))

        labelOutTextColor = typedArray.getColor(R.styleable.InputAppearance_content_labelOutTextColor, ContextCompat.getColor(input.context, R.color.ContainerPrimary))
        labelInTextColor = typedArray.getColor(R.styleable.InputAppearance_content_labelInTextColor, ContextCompat.getColor(input.context, R.color.ContainerPrimary))
        hintTextColor = typedArray.getColor(R.styleable.InputAppearance_content_hintTextColor, ContextCompat.getColor(input.context, R.color.ContainerPrimary))
        cursorColor = typedArray.getColor(R.styleable.InputAppearance_cursorColor, ContextCompat.getColor(input.context, R.color.ContainerPrimary))
        tintColor = typedArray.getColor(R.styleable.InputAppearance_tint, ContextCompat.getColor(input.context, R.color.ContainerPrimary))
        errorTextColor = typedArray.getColor(R.styleable.InputAppearance_error_textColor, ContextCompat.getColor(input.context, R.color.ContainerPrimary))

        autoScale = typedArray.getBoolean(R.styleable.InputAppearance_autoScale, false)
        clearDrawable = typedArray.getResourceId(R.styleable.InputAppearance_clearDrawable, -1)
        clearDrawableSize = typedArray.getDimensionPixelSize(R.styleable.InputAppearance_clearDrawableSize, 16.dpInt(input.context))
        clearMiniSize = typedArray.getDimensionPixelSize(R.styleable.InputAppearance_clearMiniSize, 28.dpInt(input.context))
        passwordChar = typedArray.getString(R.styleable.InputAppearance_passwordChar)?.get(0) ?: '·'
        // TextAppearance
        labelTextAppearance = typedArray.getResourceId(R.styleable.InputAppearance_labelOutTextAppearance, R.style.TextAppearance)
        contentTextAppearance = typedArray.getResourceId(R.styleable.InputAppearance_contentTextAppearance, R.style.TextAppearance)
        helperTextAppearance = typedArray.getResourceId(R.styleable.InputAppearance_helperTextAppearance, R.style.TextAppearance)
        errorTextAppearance = typedArray.getResourceId(R.styleable.InputAppearance_errorTextAppearance, R.style.TextAppearance)
        typedArray.recycle()
    }

    fun setLayerBackground(
        view: View,
        color: Int = ContextCompat.getColor(view.context, R.color.BorderPrimary),
        lineHeight: Float = 1.5f.dp2pxFloat(view.context)
    ) {
        val bottomLineDrawable = BottomLineDrawable(color, lineHeight)
        val transparentBackground = ColorDrawable(Color.TRANSPARENT)

        val layers = arrayOf(bottomLineDrawable, transparentBackground)
        val layerDrawable = LayerDrawable(layers)

        view.background = layerDrawable
    }

    class BottomLineDrawable(color: Int, lineHeight: Float) : Drawable() {
        private val paint = Paint()

        init {
            paint.color = color
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = lineHeight
        }

        override fun draw(canvas: Canvas) {
            // The actual drawn line width (strokeWidth) is smaller than expected,
            // probably because part of the line is cropped by the view's boundaries.
            // When drawing, the strokeWidth is distributed along the center line of the line.
            // This means that if part of the line is outside the Drawable's boundaries, that part will not be drawn.
            // To solve this problem, you can adjust the drawing position of the line so that it lies
            // completely within the Drawable's boundaries. Considering that the line is half the width of the line,
            // you can move the line up by strokeWidth / 2.
            val y = bounds.bottom.toFloat() - paint.strokeWidth / 2
            canvas.drawLine(bounds.left.toFloat(), y, bounds.right.toFloat(), y, paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int {
            return PixelFormat.OPAQUE
        }
    }
}