package com.okinc.uilab.edit

import android.text.TextUtils
import com.xingchaozhang.androidui.utils.getDotChar
import com.xingchaozhang.androidui.utils.getGroupingSeparator
import java.util.*

/**
 * @description : number input utils.
 */
internal class LocalizationInputUtils {
    companion object {
        val acceptNumber = setOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
        fun isLegalString(
            input: String? = null,
            legalCharacters: Set<String>? = null,
            locale: Locale = Locale.getDefault(),
            isThousandsAvailable: Boolean = false
        ): Boolean {
            if (TextUtils.isEmpty(input)) {
                return false
            }
            var isLegal = true
            for (i in 0 until input!!.length) {
                val param = input[i].toString()
                // judge whether it is a number.
                isLegal = acceptNumber.contains(param)
                // judge whether it is a dot separator.
                if (!isLegal) {
                    isLegal  = TextUtils.equals(locale.getDotChar(), param)
                }
                // judge whether it is a grouping separator.
                if (!isLegal) {
                    isLegal = isThousandsAvailable && TextUtils.equals(locale.getGroupingSeparator(), param)
                }
                // judeg whether it is a legal character of the biz.
                if (!isLegal) {
                    isLegal = (legalCharacters != null) && legalCharacters.contains(param)
                }
                if (!isLegal) {
                    return false
                }
            }
            return isLegal
        }
    }
}
