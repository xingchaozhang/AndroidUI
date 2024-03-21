package com.okinc.uilab.edit

import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import com.xingchaozhang.androidui.utils.getDotChar
import java.util.*


/**
 * @description : Use to control edit text input type. there are five thing need to gurantee:
 *                (1) only one dot is allowed;
 *                (2) input decimal should less than we define;
 *                (3) use the right symbol, . or ,;
 *                (4) if we start the number with 0, then only one zero can be allowed.
 */
open class LocalizationInputFilter(
    var maxDecimal: Int = Int.MAX_VALUE / 10,
    var useThousandsSeparator: Boolean = false,
    var localeFilter: Locale = Locale.getDefault(),
    var legalCharacterSet: Set<String>? = null,
    var disableFilter: Boolean = false
) : InputFilter {
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        if (disableFilter) {
            return null
        }
        if (source.isEmpty()) {
            return ""
        }
        var newSource = source
        if (TextUtils.equals(Locale.US.getDotChar(), source.toString())) {
            newSource = localeFilter.getDotChar()
        }
        // No need to continue if the input have invalid char.
        if (!LocalizationInputUtils.isLegalString(
                newSource.toString(),
                legalCharacterSet,
                localeFilter,
                useThousandsSeparator
            )
        ) {
            return ""
        }
        val dotPosition = dest.toString().indexOf(localeFilter.getDotChar())
        // (1)only one dot is allowed;
        if (dotPosition >= 0) {
            // protects against many dots
            for (i in 0 until newSource.length) { // if dstart == 0, this means a total cover
                if (TextUtils.equals(
                        newSource[i].toString(),
                        localeFilter.getDotChar()
                    ) && (dstart != 0)
                ) {
                    return ""
                }
            }
            // if the text is entered before the dot
            if (dend <= dotPosition) {
                return null
            }
            // (2) input decimal should less than we define;
            // this is decimal amount，it shouldn't bigger than we set.
            if ((dest.length - dotPosition > maxDecimal) && (dstart != 0)) {
                return ""
            }
        }
        // (3)use the right symbol . or ,;
        // Click directly, or ., you need to add 0 in front,
        // and the return value is determined according to the current locale.
        if ((TextUtils.equals(newSource.toString(), localeFilter.getDotChar())) && (dstart == 0)) {
            return if (maxDecimal > 0) ("0" + localeFilter.getDotChar()) else ""
        }
        // if the dot is after the inserted part, nothing can break
        if (TextUtils.equals(newSource.toString(), localeFilter.getDotChar().toString())) {
            // make sure we return the right dot by locale
            return if (maxDecimal > 0) localeFilter.getDotChar() else ""
        }
        if (TextUtils.equals("0", dest.toString()) && TextUtils.equals("0", newSource.toString())) {
            // Add for special case: replace '0' with '0'.
            if (start == 0 && dstart == 0) {
                return "0"
            }
            return ""
        }
        return null
    }
}
