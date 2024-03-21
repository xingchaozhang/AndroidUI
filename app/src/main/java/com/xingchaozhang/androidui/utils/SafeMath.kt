package com.xingchaozhang.androidui.utils

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

/**
 * 默认小数位为16位18位会溢出
 */
const val DEFAULT_MAX = 16
const val DEFAULT_IS_NO_ZERO = true
val DEFAULT_ROUNDING_MODE = RoundingMode.DOWN

fun Any?.addS(value: Any?, max: Int? = DEFAULT_MAX, isNoZero: Boolean? = DEFAULT_IS_NO_ZERO, roundingMode: RoundingMode? = DEFAULT_ROUNDING_MODE): String {
    if(this == null || value == null) return BigSafeDecimal("0.0").format(max, isNoZero, roundingMode)
    return BigSafeDecimal(this).add(BigSafeDecimal(value)).format(max, isNoZero, roundingMode)
}

fun Any?.subS(value: Any?, max: Int? = DEFAULT_MAX, isNoZero: Boolean? = DEFAULT_IS_NO_ZERO, roundingMode: RoundingMode? = DEFAULT_ROUNDING_MODE): String {
    if(this == null || value == null) return BigSafeDecimal("0.0").format(max, isNoZero, roundingMode)
    return BigSafeDecimal(this).subtract(BigSafeDecimal(value)).format(max, isNoZero, roundingMode)
}

fun Any?.mulS(value: Any?, max: Int? = DEFAULT_MAX, isNoZero: Boolean? = DEFAULT_IS_NO_ZERO, roundingMode: RoundingMode? = DEFAULT_ROUNDING_MODE): String {
    if(this == null || value == null) return BigSafeDecimal("0.0").format(max, isNoZero, roundingMode)
    return BigSafeDecimal(this).multiply(BigSafeDecimal(value)).format(max, isNoZero, roundingMode)
}

fun Any?.divS(value: Any?, max: Int? = DEFAULT_MAX, isNoZero: Boolean? = DEFAULT_IS_NO_ZERO, roundingMode: RoundingMode? = DEFAULT_ROUNDING_MODE): String {
    if(this == null || value == null || value.toSafeDouble() == 0.0) return BigSafeDecimal("0.0").format(max, isNoZero, roundingMode)
    return BigSafeDecimal(this).divide(BigSafeDecimal(value), DEFAULT_MAX, BigDecimal.ROUND_DOWN).format(max, isNoZero, roundingMode)
}

fun Any?.remS(value: Any?, max: Int? = DEFAULT_MAX, isNoZero: Boolean? = DEFAULT_IS_NO_ZERO, roundingMode: RoundingMode? = DEFAULT_ROUNDING_MODE): String {
    if(this == null || value == null || value.toSafeDouble() == 0.0) return BigSafeDecimal("0.0").format(max, isNoZero, roundingMode)
    return BigSafeDecimal(this).remainder(BigSafeDecimal(value)).format(max, isNoZero, roundingMode)
}

fun Any?.addD(value: Any?, max: Int? = DEFAULT_MAX, isNoZero: Boolean? = DEFAULT_IS_NO_ZERO, roundingMode: RoundingMode? = DEFAULT_ROUNDING_MODE) = this.addS(value, max, isNoZero, roundingMode).toSafeDouble()
fun Any?.subD(value: Any?, max: Int? = DEFAULT_MAX, isNoZero: Boolean? = DEFAULT_IS_NO_ZERO, roundingMode: RoundingMode? = DEFAULT_ROUNDING_MODE) = this.subS(value, max, isNoZero, roundingMode).toSafeDouble()
fun Any?.mulD(value: Any?, max: Int? = DEFAULT_MAX, isNoZero: Boolean? = DEFAULT_IS_NO_ZERO, roundingMode: RoundingMode? = DEFAULT_ROUNDING_MODE) = this.mulS(value, max, isNoZero, roundingMode).toSafeDouble()
fun Any?.divD(value: Any?, max: Int? = DEFAULT_MAX, isNoZero: Boolean? = DEFAULT_IS_NO_ZERO, roundingMode: RoundingMode? = DEFAULT_ROUNDING_MODE) = this.divS(value, max, isNoZero, roundingMode).toSafeDouble()
fun Any?.remD(value: Any?, max: Int? = DEFAULT_MAX, isNoZero: Boolean? = DEFAULT_IS_NO_ZERO, roundingMode: RoundingMode? = DEFAULT_ROUNDING_MODE) = this.remS(value, max, isNoZero, roundingMode).toSafeDouble()

fun Any?.formatS(max: Int? = DEFAULT_MAX, isNoZero: Boolean? = DEFAULT_IS_NO_ZERO, roundingMode: RoundingMode? = DEFAULT_ROUNDING_MODE) = BigSafeDecimal(this).format(max, isNoZero, roundingMode)
fun Any?.formatD(max: Int? = DEFAULT_MAX, isNoZero: Boolean? = DEFAULT_IS_NO_ZERO, roundingMode: RoundingMode? = DEFAULT_ROUNDING_MODE) = BigSafeDecimal(this).format(max, isNoZero, roundingMode).toSafeDouble()

internal fun BigDecimal?.format(max: Int? = DEFAULT_MAX, isNoZero: Boolean? = DEFAULT_IS_NO_ZERO, roundingMode: RoundingMode? = DEFAULT_ROUNDING_MODE): String {
    var d = BigSafeDecimal(this).setScale(max ?: DEFAULT_MAX, roundingMode ?: DEFAULT_ROUNDING_MODE)
    if(isNoZero ?: DEFAULT_IS_NO_ZERO){
        if(d.toSafeDouble() == 0.0) return "0"
        d = d.stripTrailingZeros()
    }
    return d.toPlainString()
}

fun BigSafeDecimal(value: Any?): BigDecimal = try {
    when (value) {
        null -> BigDecimal("0.0")
        is BigDecimal -> value
        else -> BigDecimal(value.toString())
    }
} catch (e: Exception) {
    BigDecimal("0.0")
}

fun Any?.toSafeDouble(): Double = try { BigSafeDecimal(this).toDouble() } catch (e: Exception) { 0.0 }
fun Any?.toSafeInt(): Int = try { BigSafeDecimal(this).toInt() } catch (e: Exception) { 0 }
fun Any?.toSafeFloat(): Float = try { BigSafeDecimal(this).toFloat() } catch (e: Exception) { 0.0F }
fun Any?.toSafeLong(): Long = try { BigSafeDecimal(this).toLong() } catch (e: Exception) { 0L }
fun Any?.toSafeShort(): Short = try { BigSafeDecimal(this).toShort() } catch (e: Exception) { 0 }
fun Any?.toSafeByte(): Byte = try { BigSafeDecimal(this).toByte() } catch (e: Exception) { 0 }
fun Any?.toSafeBigDecimal(): BigDecimal = try { BigSafeDecimal(this.toString()) } catch (e: Exception) { BigDecimal("0.0") }
fun Any?.toSafeString(): String = try { this?.toString() ?: "" } catch (e: Exception) { "" }
fun Any?.toSafeIntString(): String = try { BigSafeDecimal(this).toBigInteger().toString() } catch (e: Exception) { "0" }
fun Any?.toSafeBigInteger(): BigInteger = try{BigSafeDecimal(this).toBigInteger()}catch (e:java.lang.Exception){
    BigInteger.ZERO}
fun Any?.toSafeDoubleString(): String = try { this.formatS() } catch (e: Exception) { "0.0" }
fun Double.Companion.valueSafeOf(value: Any?): Double = try { java.lang.Double.valueOf(value?.toString().orEmpty()) } catch (e: Exception) { 0.0 }
fun Int.Companion.valueSafeOf(value: Any?): Int = try { java.lang.Integer.valueOf(value?.toString().orEmpty()) } catch (e: Exception) { 0 }
fun Float.Companion.valueSafeOf(value: Any?): Float = try { java.lang.Float.valueOf(value?.toString().orEmpty()) } catch (e: Exception) { 0.0F }
fun Long.Companion.valueSafeOf(value: Any?): Long = try { java.lang.Long.valueOf(value?.toString().orEmpty()) } catch (e: Exception) { 0L }
fun Short.Companion.valueSafeOf(value: Any?): Short = try { java.lang.Short.valueOf(value?.toString()) } catch (e: Exception) { 0 }
fun Byte.Companion.valueSafeOf(value: Any?): Byte = try { java.lang.Byte.valueOf(value?.toString().orEmpty()) } catch (e: Exception) { 0 }

fun <T> Collection<T>?.isNotNullOrEmpty(): Boolean = this != null && this.isNotEmpty()
fun CharSequence?.isNotNullOrEmptyOrBlank(): Boolean = this != null && this.isNotEmpty() && this.isNotBlank()
fun Any?.gt(data:Any?): Boolean{
    return BigSafeDecimal(this) > BigSafeDecimal(data)
}
fun Any?.lt(data:Any?): Boolean{
    return BigSafeDecimal(this) < BigSafeDecimal(data)
}
fun Any?.gte(data:Any?): Boolean{
    return BigSafeDecimal(this) >= BigSafeDecimal(data)
}
fun Any?.lte(data:Any?): Boolean{
    return BigSafeDecimal(this) <= BigSafeDecimal(data)
}
fun Any?.eq(data:Any?): Boolean{
    return BigSafeDecimal(this).compareTo(BigSafeDecimal(data)) == 0
}
fun Any?.neq(data:Any?): Boolean{
    return BigSafeDecimal(this).compareTo(BigSafeDecimal(data)) != 0
}

fun Any?.Max(data: Any?): String{
    return if(this.gte(data)) this.toSafeDoubleString() else data.toSafeDoubleString()
}

fun Any?.Min(data: Any?): String{
    return if(this.lte(data)) this.toSafeDoubleString() else data.toSafeDoubleString()
}