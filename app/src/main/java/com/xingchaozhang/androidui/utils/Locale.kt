package com.xingchaozhang.androidui.utils

import android.icu.text.DecimalFormatSymbols
import java.util.Locale

class Locale
fun Locale.isChinese(): Boolean {
    return "zh".equals(language, true)
}

fun Locale.isEnglish(): Boolean = "en".equals(language, true)

fun Locale.isSimplifiedChinese(): Boolean {
    return "zh-CN".equals("$language-$country", true)
}

fun Locale.isThai(): Boolean = "th".equals(language, true) || "TH".equals(country, true)

fun Locale.getDotChar(): String {
    return DecimalFormatSymbols(this).decimalSeparator.toString()
}

fun Locale.getGroupingSeparator(): String {
    return DecimalFormatSymbols(this).groupingSeparator.toString()
}

fun Locale.toLangCountryString(): String {
    return "$language-$country"
}