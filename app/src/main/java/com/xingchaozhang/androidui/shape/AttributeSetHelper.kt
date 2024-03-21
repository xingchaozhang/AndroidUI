package com.okinc.uilab.shape.helper

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import com.xingchaozhang.androidui.R
import com.xingchaozhang.androidui.shape.AttributeSetData

/**
 * <pre>
 *      @author : Allen
 *      e-mail  : lygttpod@163.com
 *      date    : 2019/09/09
 *      desc    :
 * </pre>
 */
class AttributeSetHelper {

    private val defaultColor = 0xffffff
    private val defaultSelectorColor = 0x20000000

    fun loadFromAttributeSet(context: Context, attrs: AttributeSet?): AttributeSetData {
        val attributeSetData = AttributeSetData()

        if (attrs == null) return attributeSetData

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AttributeSet)

        attributeSetData.shapeType = typedArray.getInt(R.styleable.AttributeSet_shapeStyle, GradientDrawable.RECTANGLE)

        attributeSetData.solidColor = typedArray.getColor(R.styleable.AttributeSet_shapeSolidColor, defaultColor)

        attributeSetData.selectorPressedColor = typedArray.getColor(R.styleable.AttributeSet_shapeSelectorPressedColor, defaultSelectorColor)
        attributeSetData.selectorDisableColor = typedArray.getColor(R.styleable.AttributeSet_shapeSelectorDisableColor, defaultSelectorColor)
        attributeSetData.selectorNormalColor = typedArray.getColor(R.styleable.AttributeSet_shapeSelectorNormalColor, defaultSelectorColor)

        attributeSetData.cornersRadius = typedArray.getDimensionPixelSize(R.styleable.AttributeSet_shapeCornersRadius, 0).toFloat()
        attributeSetData.cornersTopLeftRadius = typedArray.getDimensionPixelSize(R.styleable.AttributeSet_shapeCornersTopLeftRadius, 0).toFloat()
        attributeSetData.cornersTopRightRadius = typedArray.getDimensionPixelSize(R.styleable.AttributeSet_shapeCornersTopRightRadius, 0).toFloat()
        attributeSetData.cornersBottomLeftRadius = typedArray.getDimensionPixelSize(R.styleable.AttributeSet_shapeCornersBottomLeftRadius, 0).toFloat()
        attributeSetData.cornersBottomRightRadius = typedArray.getDimensionPixelSize(R.styleable.AttributeSet_shapeCornersBottomRightRadius, 0).toFloat()

        attributeSetData.strokeWidth = typedArray.getDimensionPixelSize(R.styleable.AttributeSet_shapeStrokeWidth, 0)
        attributeSetData.strokeDashWidth = typedArray.getDimensionPixelSize(R.styleable.AttributeSet_shapeStrokeDashWidth, 0).toFloat()
        attributeSetData.strokeDashGap = typedArray.getDimensionPixelSize(R.styleable.AttributeSet_shapeStrokeDashGap, 0).toFloat()

        attributeSetData.strokeColor = typedArray.getColor(R.styleable.AttributeSet_shapeStrokeColor, defaultColor)

        attributeSetData.sizeWidth = typedArray.getDimensionPixelSize(R.styleable.AttributeSet_shapeSizeWidth, 0)
        attributeSetData.sizeHeight = typedArray.getDimensionPixelSize(R.styleable.AttributeSet_shapeSizeHeight, dip2px(context, 48f))

        attributeSetData.gradientAngle = typedArray.getFloat(R.styleable.AttributeSet_shapeGradientAngle, -1f).toInt()
        attributeSetData.gradientCenterX = typedArray.getFloat(R.styleable.AttributeSet_shapeGradientCenterX, 0f)
        attributeSetData.gradientCenterY = typedArray.getFloat(R.styleable.AttributeSet_shapeGradientCenterY, 0f)
        attributeSetData.gradientGradientRadius = typedArray.getDimensionPixelSize(R.styleable.AttributeSet_shapeGradientGradientRadius, 0)

        attributeSetData.gradientStartColor = typedArray.getColor(R.styleable.AttributeSet_shapeGradientStartColor, -1)
        attributeSetData.gradientCenterColor = typedArray.getColor(R.styleable.AttributeSet_shapeGradientCenterColor, -1)
        attributeSetData.gradientEndColor = typedArray.getColor(R.styleable.AttributeSet_shapeGradientEndColor, -1)

        attributeSetData.gradientType = typedArray.getInt(R.styleable.AttributeSet_shapeGradientType, 0)
        attributeSetData.gradientUseLevel = typedArray.getBoolean(R.styleable.AttributeSet_shapeGradientUseLevel, false)

        attributeSetData.useSelector = typedArray.getBoolean(R.styleable.AttributeSet_shapeUseSelector, false)

        typedArray.recycle()

        return attributeSetData
    }


    /**
     * 单位转换工具类
     *
     * @param context  上下文对象
     * @param dipValue 值
     * @return 返回值
     */
    private fun dip2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }
}