package com.xingchaozhang.androidui.utils

import com.ibm.icu.number.Notation
import com.ibm.icu.number.NumberFormatter
import com.ibm.icu.number.Scale
import com.ibm.icu.util.NoUnit
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

/**
 * Format localized number.
 */
class NumberFormat

private const val TAG = "NumberFormat"

/**
 * Format localized number.
 *
 * @param roundMode Rounding mode.
 * @param precision Precision.
 * @param signType Display sign type.
 * @param locale Locale.
 */
internal fun Number.formatICUNumberInternal(
    roundMode: RoundingMode,
    precision: Precision,
    signType: DisplaySign,
    locale: Locale,
): String {
    return try {
        val formatter = NumberFormatter.withLocale(locale)
            .precision(precision.toICUPrecision())
            .roundingMode(roundMode)
            .sign(signType.toSignDisplay())
        val formatStr = formatter.format(this).toString()
        return formatStr
    } catch (e: Exception) {
        this.toString()
    } catch (e: LinkageError) {
        this.toString()
    }
}


/**
 * Format number to localized string.
 *
 * @param roundMode Rounding mode.see [RoundingMode]
 *
 * @param precision Precision. support 5 types:
 * see [Precision.fixedPrecision]
 * see [Precision.minPrecision]
 * see [Precision.maxPrecision]
 * see [Precision.minMaxPrecision]
 * see [Precision.increment]
 *
 * @param signType Display sign type. support three types.
 * [DisplaySign.AUTO] show sign when number < 0
 * [DisplaySign.EXCEPT_ZERO] show sign when number != 0
 * [DisplaySign.ALWAYS] always show sign
 *
 * @param locale Locale. which locale to format a number
 */
@JvmOverloads
fun Number.formatICUNumber(
    roundMode: RoundingMode = RoundingMode.DOWN,
    precision: Precision = Precision.fixedPrecision(2),
    signType: DisplaySign = DisplaySign.AUTO,
    locale: Locale = Locale.getDefault(),
): String {
    return formatICUNumberInternal(roundMode, precision, signType, locale)
}

/**
 * Format number to localized percent string.
 *
 * WARN: this method regards percentage as a unit instead of a style.
 * It means [0.12 will be format to 0.12%] instead of 12% !!!
 * This is the latest specification from the ICU. And you can use [scale] to multiply the number before format.
 * if [scale] is 100.0 , 0.12 will be format to 12%.
 *
 * @param roundMode Rounding mode.see [RoundingMode]
 *
 * @param precision Precision. support 5 types:
 * see [Precision.fixedPrecision]
 * see [Precision.minPrecision]
 * see [Precision.maxPrecision]
 * see [Precision.minMaxPrecision]
 * see [Precision.increment]
 *
 * @param scale Sets a scale (multiplier) to be used to scale the number by an arbitrary amount before formatting. Most common values:
 * - Multiply by 100: useful for percentages.
 * - Multiply by an arbitrary value: useful for unit conversions.
 *
 * @param signType Display sign type. support three types.
 * [DisplaySign.AUTO] show sign when number < 0
 * [DisplaySign.EXCEPT_ZERO] show sign when number != 0
 * [DisplaySign.ALWAYS] always show sign
 *
 * @param locale Locale. which locale to format a number
 */
@JvmOverloads
fun Number.formatICUPercent(
    roundMode: RoundingMode = RoundingMode.UP,
    precision: Precision = Precision.fixedPrecision(2),
    signType: DisplaySign = DisplaySign.AUTO,
    scale: Double? = null,
    locale: Locale = Locale.getDefault(),
): String {
    return formatPercentOrPermille(true, roundMode, precision, signType, scale, locale)
}


/**
 * Format number to localized permille string.
 *
 * WARN: this method regards percentage as a unit instead of a style.
 * It means [0.012 will be format to 0.012‰] instead of 12‰ !!!
 * This is the latest specification from the ICU. And you can use [scale] to multiply the number before format.
 * if [scale] is 1000.0 , 0.012 will be format to 12‰.
 *
 * @param roundMode Rounding mode.see [RoundingMode]
 *
 * @param precision Precision. support 5 types:
 * see [Precision.fixedPrecision]
 * see [Precision.minPrecision]
 * see [Precision.maxPrecision]
 * see [Precision.minMaxPrecision]
 * see [Precision.increment]
 *
 * @param scale Sets a scale (multiplier) to be used to scale the number by an arbitrary amount before formatting. Most common values:
 * - Multiply by 100: useful for percentages.
 * - Multiply by an arbitrary value: useful for unit conversions.
 *
 * @param signType Display sign type. support three types.
 * [DisplaySign.AUTO] show sign when number < 0
 * [DisplaySign.EXCEPT_ZERO] show sign when number != 0
 * [DisplaySign.ALWAYS] always show sign
 *
 * @param locale Locale. which locale to format a number
 */
@JvmOverloads
fun Number.formatICUPermille(
    roundMode: RoundingMode = RoundingMode.UP,
    precision: Precision = Precision.fixedPrecision(2),
    signType: DisplaySign = DisplaySign.AUTO,
    scale: Double? = null,
    locale: Locale = Locale.getDefault(),
): String {
    return formatPercentOrPermille(false, roundMode, precision, signType, scale, locale)
}

internal fun Number.formatPercentOrPermille(
    isPercent: Boolean,
    roundMode: RoundingMode,
    precision: Precision,
    signType: DisplaySign,
    scale: Double?,
    locale: Locale,
): String {
    return try {
        val unit = if (isPercent) {
            NoUnit.PERCENT
        } else {
            NoUnit.PERMILLE
        }
        val doubleScale = scale ?: 1.0
        val signDisplayType = signType.toSignDisplay()

        val formatter = NumberFormatter.withLocale(locale)
            .precision(precision.toICUPrecision())
            .roundingMode(roundMode)
            .unit(unit)
            .scale(Scale.byDouble(doubleScale))
            .sign(signDisplayType)

        return formatter.format(this).toString()
    } catch (e: LinkageError) {
        this.fallbackPercent(isPercent, roundMode, precision, signType, scale, locale)
    }
}


private fun Number.fallbackPercent(
    isPercent: Boolean,
    roundMode: RoundingMode,
    precision: Precision,
    signType: DisplaySign,
    scale: Double?,
    locale: Locale,
): String {
    return try {
        val sign = if (isPercent) "%" else "‰"
        val scaledNumber = this.toSafeBigDecimal().let {
            return@let if (scale != null) {
                it.multiply(BigDecimal(scale))
            } else {
                it
            }
        }.setScale(2, roundMode)
        String.format(locale, "%s%s", scaledNumber, sign)
    } catch (e: Exception) {
        this.toString()
    }

}

/**
 * 最多精确到precision位，多余位入一位
 * such as : precision = 2， 1.232 -> 1.24
 */
@JvmOverloads
fun BigDecimal.formatUpToMax(precision: Int = 2, locale: Locale = Locale.getDefault()): String {
    val tempPrecision = if (precision >= 0) {
        Precision.maxPrecision(precision)
    } else {
        Precision.compatNegativePrecision(precision)
    }
    return formatICUNumberInternal(RoundingMode.UP, tempPrecision, DisplaySign.AUTO, locale)
}

/**
 * 固定精确到precision位，多余位入一位
 * such as : precision = 2， 1.111 -> 1.20
 */
@JvmOverloads
fun BigDecimal.formatUpToFixed(precision: Int = 2, locale: Locale = Locale.getDefault()): String {
    val tempPrecision = if (precision >= 0) {
        Precision.fixedPrecision(precision)
    } else {
        Precision.compatNegativePrecision(precision)
    }
    return formatICUNumberInternal(RoundingMode.UP, tempPrecision, DisplaySign.AUTO, locale)
}

/**
 * 最多精确到precision位，多余位直接省略, 推荐使用BigDecimal(String)比较准确，
 * 如果传入double，precision>10后可能展示有问题, precision >10的推荐用string构造BigDecimal
 * such as : precision = 2， 1.234 -> 1.23
 */
@JvmOverloads
fun BigDecimal.formatDownToMax(precision: Int = 2, locale: Locale = Locale.getDefault()): String {
    val tempPrecision = if (precision >= 0) {
        Precision.maxPrecision(precision)
    } else {
        Precision.compatNegativePrecision(precision)
    }
    return formatICUNumberInternal(RoundingMode.DOWN, tempPrecision, DisplaySign.AUTO, locale)
}

/**
 * 固定精确到precision位，多余位直接省略, 推荐使用BigDecimal(String)比较准确，
 * 如果传入double，precision>10后可能展示有问题, precision >10的推荐用string构造BigDecimal
 * such as : precision = 2， 1.2 -> 1.20
 */
@JvmOverloads
fun BigDecimal.formatDownToFixed(precision: Int = 2, locale: Locale = Locale.getDefault()): String {
    val tempPrecision = if (precision >= 0) {
        Precision.fixedPrecision(precision)
    } else {
        Precision.compatNegativePrecision(precision)
    }

    return formatICUNumberInternal(RoundingMode.DOWN, tempPrecision, DisplaySign.AUTO, locale)
}

/**
 * 最多精确到precision位，四舍五入
 * such as : precision = 2， 1.234 -> 1.23
 */
@JvmOverloads
fun BigDecimal.formatRoundToMax(precision: Int = 2, locale: Locale = Locale.getDefault()): String {
    val tempPrecision = if (precision >= 0) {
        Precision.maxPrecision(precision)
    } else {
        Precision.compatNegativePrecision(precision)
    }

    return formatICUNumberInternal(RoundingMode.HALF_UP, tempPrecision, DisplaySign.AUTO, locale)
}

/**
 * 固定精确到precision位，四舍五入
 * such as : precision = 2， 1.2 -> 1.20
 */
@JvmOverloads
fun BigDecimal.formatRoundToFixed(
    precision: Int = 2,
    locale: Locale = Locale.getDefault()
): String {
    val tempPrecision = if (precision >= 0) {
        Precision.fixedPrecision(precision)
    } else {
        Precision.compatNegativePrecision(precision)
    }

    return formatICUNumberInternal(RoundingMode.HALF_EVEN, tempPrecision, DisplaySign.AUTO, locale)
}

/**
 * 不处理进位截位、只进行格式化展示, 保留原始小数位
 * this string is normally obtained from server response
 * 比如2.0130566进行多语言格式化： US: 2.0130566  IT:2,0130566
 */
@JvmOverloads
fun String.formatLocalized(locale: Locale = Locale.getDefault()): String {
    return try {
        toBigSafeDecimal(this).formatDownToMax(100, locale)
    } catch (e: Exception) {
        e.printStackTrace()
        this
    }
}

/**
 * 不处理进位截位、只进行格式化展示, 保留原始小数位，最少展示小数点后 minPrecision 位
 * this string is normally obtained from server response
 * 比如2.0130566进行多语言格式化：
 *  US: 2.0130566  IT:2,0130566
 *
 * 0 : US: 0.00 IT: 0,00
 */
@JvmOverloads
fun String.formatLocalizedWithMinPrecision(
    minPrecision: Int = 2,
    locale: Locale = Locale.getDefault()
): String {
    return try {
        toBigSafeDecimal(this).formatICUNumberInternal(
            RoundingMode.HALF_DOWN,
            Precision.minMaxPrecision(minPrecision, 100),
            DisplaySign.AUTO,
            locale
        )
    } catch (e: Exception) {
        e.printStackTrace()
        this
    }
}

/**
 * 将一个平凡的数字字符串转换成BigDecimal
 * 注意，这里只能传入一个平凡的、没有任何格式的字符串，这个字符串一般是你从后端接口获取而来，如"123456.789"
 */
fun String.fromPlainString(): BigDecimal {
    return toBigSafeDecimal(this)
}

/**
 * Format string with percent symbol, no matter the string is number or not.
 * such as String = "balabala", output = balabala% (Locale.US), %balabala(Locale.TR)
 */
@JvmOverloads
fun String.formatStringWithPercent(locale: Locale = Locale.getDefault()):
        String {
    val percentString =
        toBigSafeDecimal("0").formatICUPercent(
            RoundingMode.UP,
            Precision.fixedPrecision(0),
            DisplaySign.AUTO,
            100.0,
            locale
        )
    return percentString.replace("0", this)
}

/**
 * 按照Precision精度格式化String Number
 */
@JvmOverloads
fun String.toLocalizationStringWithPrecision(
    minPrecision: Int,
    maxPrecision: Int,
    roundMode: RoundingMode,
    locale: Locale = Locale.getDefault()
): String {
    return toBigSafeDecimal(this).formatICUNumberInternal(
        roundMode,
        Precision.minMaxPrecision(minPrecision, maxPrecision),
        DisplaySign.AUTO,
        locale
    )
}

/**
 * 按照maxPrecision精度格式化String Number
 */
@JvmOverloads
fun String.toLocalizationStringWithMaxPrecision(
    maxPrecision: Int,
    roundMode: RoundingMode,
    locale: Locale = Locale.getDefault()
): String {
    return toLocalizationStringWithPrecision(0, maxPrecision, roundMode, locale)
}

/**
 * 将一个Object转成BigDecimal
 */
fun toBigSafeDecimal(value: Any?): BigDecimal = try {
    when (value) {
        null -> BigDecimal("0.0")
        is BigDecimal -> value
        else -> BigDecimal(value.toString())
    }
} catch (e: Exception) {
    BigDecimal("0.0")
}

fun Number.formatICUCompactInternal(
    roundMode: RoundingMode,
    precision: Precision,
    signType: DisplaySign,
    locale: Locale,
): String {
    return try {
        return NumberFormatter.withLocale(locale)
            .notation(Notation.compactShort())
            .precision(precision.toICUPrecision())
            .roundingMode(roundMode)
            .sign(signType.toSignDisplay())
            .grouping(NumberFormatter.GroupingStrategy.AUTO)
            .format(this)
            .toString()
    } catch (e: LinkageError) {
        this.toString()
    }
}


@JvmOverloads
fun Number.formatICUCompact(
    roundMode: RoundingMode = RoundingMode.HALF_UP,
    precision: Precision = Precision.fixedPrecision(2),
    signType: DisplaySign = DisplaySign.AUTO,
    locale: Locale = Locale.getDefault(),
): String {

    return formatICUCompactInternal(roundMode, precision, signType, locale)
}
