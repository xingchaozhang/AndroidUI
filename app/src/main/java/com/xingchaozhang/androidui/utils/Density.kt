package com.xingchaozhang.androidui.utils

import android.content.Context
import android.util.TypedValue
import com.xingchaozhang.androidui.UIEnv
import kotlin.math.roundToInt

fun Int.px2dp(context: Context = UIEnv.getApp()): Int = (this / context.resources.displayMetrics.density).roundToInt()

fun Int.px2sp(context: Context = UIEnv.getApp()): Int = (this / context.resources.displayMetrics.scaledDensity).roundToInt()

fun Float.px2sp(context:Context = UIEnv.getApp()):Int = (this/context.resources.displayMetrics.scaledDensity).roundToInt()

fun Float.px2dp(context:Context = UIEnv.getApp()):Int = (this / context.resources.displayMetrics.density).roundToInt()

fun Float.dp2px(context: Context = UIEnv.getApp()): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics).roundToInt()

fun Float.sp2px(context:Context = UIEnv.getApp()):Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, context.resources.displayMetrics).roundToInt()

fun Float.sp2pxFloat(context:Context = UIEnv.getApp()):Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, context.resources.displayMetrics)

fun Float.dp2pxFloat(context: Context = UIEnv.getApp()): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)


fun Int.dp(context: Context = UIEnv.getApp()): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics)

fun Int.dpInt(context: Context = UIEnv.getApp()): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics).roundToInt()

fun Float.dp(context: Context = UIEnv.getApp()): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)

fun Float.dpInt(context: Context = UIEnv.getApp()): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics).roundToInt()