package com.xingchaozhang.androidui.shape

import android.content.res.ColorStateList
import android.graphics.Color

/**
 * <pre>
 *      @author : Allen
 *      e-mail  : lygttpod@163.com
 *      date    : 2019/09/22
 *      desc    :
 * </pre>
 */
class AttributeSetData {
    var shapeType = -1
    var solidColor = -1

    var strokeWidth = -1
    var strokeColor = -1
    var strokeDashWidth = 0.0f
    var strokeDashGap = 0.0f
    var strokeColorStateList: ColorStateList? = null

    var cornersRadius = 0.0f
    var cornersTopLeftRadius = 0.0f
    var cornersTopRightRadius = 0.0f
    var cornersBottomLeftRadius = 0.0f
    var cornersBottomRightRadius = 0.0f

    var gradientAngle = -1
    var gradientCenterX = 0f
    var gradientCenterY = 0f
    var gradientGradientRadius: Int = 0
    var gradientStartColor = -1
    var gradientCenterColor = -1
    var gradientEndColor = -1
    var gradientType: Int = 0
    var gradientUseLevel: Boolean = false

    var sizeWidth = -1
    var sizeHeight = -1

    var selectorPressedColor: Int = 0
    var selectorDisableColor: Int = 0
    var selectorNormalColor: Int = 0

    var useSelector: Boolean = false

    //////////阴影相关////////
    var showShadow: Boolean = false
    var shadowColor: Int = Color.GRAY
    var shadowColorAlpha = 0.2f
    var shadowLeftWidth = 0f
    var shadowTopWidth = 0f
    var shadowRightWidth = 0f
    var shadowBottomWidth = 0f

    var shadowCornersRadius = 0f
    var shadowCornersTopLeftRadius = 0f
    var shadowCornersTopRightRadius = 0f
    var shadowCornersBottomLeftRadius = 0f
    var shadowCornersBottomRightRadius = 0f
    
    fun reset() {
        shapeType = -1
        solidColor = -1

        strokeWidth = -1
        strokeColor = -1
        strokeDashWidth = 0.0f
        strokeDashGap = 0.0f
        strokeColorStateList = null

        cornersRadius = 0.0f
        cornersTopLeftRadius = 0.0f
        cornersTopRightRadius = 0.0f
        cornersBottomLeftRadius = 0.0f
        cornersBottomRightRadius = 0.0f

        gradientAngle = -1
        gradientCenterX = 0f
        gradientCenterY = 0f
        gradientGradientRadius = 0
        gradientStartColor = -1
        gradientCenterColor = -1
        gradientEndColor = -1
        gradientType = 0
        gradientUseLevel = false

        sizeWidth = -1
        sizeHeight = -1

        selectorPressedColor = 0
        selectorDisableColor = 0
        selectorNormalColor = 0

        useSelector = false

        //////////阴影相关////////
        showShadow = false
        shadowColor = Color.GRAY
        shadowColorAlpha = 0.2f
        shadowLeftWidth = 0f
        shadowTopWidth = 0f
        shadowRightWidth = 0f
        shadowBottomWidth = 0f

        shadowCornersRadius = 0f
        shadowCornersTopLeftRadius = 0f
        shadowCornersTopRightRadius = 0f
        shadowCornersBottomLeftRadius = 0f
        shadowCornersBottomRightRadius = 0f
    }
}