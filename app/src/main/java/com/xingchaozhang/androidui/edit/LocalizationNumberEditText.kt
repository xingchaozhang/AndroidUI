package com.okinc.uilab.edit

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.text.*
import android.text.method.DigitsKeyListener
import android.text.method.NumberKeyListener
import android.util.AttributeSet
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.xingchaozhang.androidui.edit.BaseEditText
import com.xingchaozhang.androidui.utils.formatLocalized
import com.xingchaozhang.androidui.utils.formatLocalizedWithMinPrecision
import com.xingchaozhang.androidui.utils.getDotChar
import com.xingchaozhang.androidui.utils.getGroupingSeparator
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * @description : This is a edit text which can format the number by localization.
 */
open class LocalizationNumberEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : BaseEditText(context, attrs, defStyleAttr) {
    var locale: Locale = Locale.getDefault()
        set(value) {
            field = value
            filters = arrayOfNumberInputFilters()
        }

    /**
     * Whether to display the thousandth.true，display it， false no.
     */
    var showGroupingSeparator: Boolean = false
        set(value) {
            field = value
            filters = arrayOfNumberInputFilters()
            if (keyListener is DigitsKeyListener) {
                keyListener = acceptKeyListener
            }
        }

    /**
     * To limit how many decimal is available
     */
    var maxDecimalValue: Int = Int.MAX_VALUE / 10
        set(value) {
            field = value
            filters = arrayOfNumberInputFilters()
        }

    /**
     * Excluding grouping char and decimal char.
     */
    var maxInputLength: Int = Int.MAX_VALUE
        set(value) {
            field = value
            filters = arrayOfNumberInputFilters()
        }

    /**
     * Characters other than the default legal characters.
     */
    var legalCharacters: Set<String> = setOf()
        set(value) {
            field = value
            numberInputFilter.legalCharacterSet = field
        }

    override var autoScale = false
        set(value) {
            field = value
            super.autoScale = field
            if (field) {
                addTextChangedListener(scaleTextWatcher)
            } else {
                removeTextChangedListener(scaleTextWatcher)
            }
        }

    /**
     * This value accept a english standard number value ,such as 9999999.02
     */
    var plainNumericText: String = ""
        @SuppressLint("SetTextI18n")
        set(value) {
            field = value
            if (TextUtils.isEmpty(value)) {
                setText("")
                return
            }
            needFormatNumber = false
            var result = ""
            result = getNumberBeforeFormat(value, Locale.US)
            val decimalPart = getDecimalPart(result)
            result = unFormat(result, true, Locale.US)
                .formatLocalizedWithMinPrecision(decimalPart.length, locale)
            result = unFormat(result, false, locale)
            val dotPosition = result.indexOf(locale.getDotChar())
            result = postProcess(result, dotPosition)
            try {
                setText(result)
                setSelection(result.length)
            } catch (e: Exception) {
                Log.d("NumberInputEditText", "plainNumericText: " + e.toString())
            }
            needFormatNumber = true
        }
        get() {
            return if (text != null) {
                unFormat(text.toString(), true, locale).removeSuffix(locale.getDotChar())
            } else {
                ""
            }
        }

    /**
     * This value accept a localized number value ,such as 999,999,999.00
     */
    var localizedNumericText: String = ""
        set(value) {
            needFormatNumber = false
            field = formatNumber(value)
            try {
                setText(field)
                setSelection(field.length)
            } catch (e: Exception) {
                Log.d("NumberInputEditText", "localizedNumericText: " + e.toString())
            }
            needFormatNumber = true
        }
        get() {
            return if (text != null) {
                formatNumber(text.toString())
            } else ""
        }

    /**
     * Number key listener,only accept number,dot and separator.
     */
    open var acceptKeyListener = object : NumberKeyListener() {
        override fun getInputType(): Int {
            return InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        override fun getAcceptedChars(): CharArray {
            return charArrayOf(
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                Locale.getDefault().getDotChar()[0],
                Locale.getDefault().getGroupingSeparator()[0]
            )
        }
    }

    internal var inputConnection: InputConnection? = null

    /**
     * True, indicate is a number before selection ,false ,no.
     */
    internal var isDotSeparatorBeforeSelection = false

    private var numberInputFilter = LocalizationInputFilter()

    /**
     * true ,format input, false ,no need.
     */
    private var needFormatNumber: Boolean = false

    /**
     * true ,allow out filters to add ,false otherwise.
     */
    private var shouldAddOutFilter: Boolean = true

    /**
     * number before format.
     */
    private var preNumber = ""

    /**
     * restore out text watcher listeners.
     */
    private var outWatcherList: MutableList<TextWatcher>? = null

    /**
     * Record the amount of numbers for offset.
     */
    private var numberCount = 0

    /**
     * Used to determine whether the data comes from the clipboard
     */
    private var isFromCopy = false

    private var startSelection = 0

    /**
     * this watcher is for inner use.
     */
    private val innerTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            outWatcherList?.forEach {
                it.beforeTextChanged(s, start, count, after)
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            outWatcherList?.forEach {
                it.onTextChanged(s, start, before, count)
            }
            val newStr = s.toString()
            numberCount = 0
            startSelection = selectionStart
            for (i in 0 until startSelection) {
                if (isNumber(newStr, i)) {
                    numberCount++
                }
            }
            // This logic is to deal with delete dot char.
            if (!TextUtils.isEmpty(s) && s!!.length > 1 && (selectionStart > 0)) {
                isDotSeparatorBeforeSelection = !(isNumber(s.toString(), selectionStart - 1)
                        || (Locale.getDefault().getGroupingSeparator()[0] == s[selectionStart - 1]))
            }
        }

        override fun afterTextChanged(s: Editable?) {
            val newStr = s.toString()
            if (!needFormatNumber) {
                outWatcherList?.forEach {
                    it.afterTextChanged(SpannableStringBuilder(newStr))
                }
                if (maxInputLength < 1000) {
                    var endIndex = newStr.length
                    if (endIndex > maxInputLength) {
                        endIndex = maxInputLength
                    }
                    removeInnerTextChangedListener()
                    try {
                        inputConnection?.apply {
                            deleteSurroundingText(SELECTION_LENGTH, SELECTION_LENGTH)
                            commitText(newStr.substring(0, endIndex), 0)
                        }
                        setSelection(if (startSelection > maxInputLength) maxInputLength else startSelection)
                    } catch (e: Exception) {
                        Log.d("NumberInputEditText", "afterTextChanged, $e")
                    }
                    addInnerTextChangedListener()
                }
                return
            }
            tryFormatNumber(newStr)
        }
    }

    private fun tryFormatNumber(number: String) {
        removeInnerTextChangedListener()
        val newStr = formatNumber(number)
        // This offset is used to calculate the number of thousandths and decimal points before the cursor.
        try {
            inputConnection?.apply {
                deleteSurroundingText(SELECTION_LENGTH, SELECTION_LENGTH)
                commitText(newStr, 0)
            }
            // If Selection is larger than length, we just set to the last.
            if (startSelection > newStr.length) {
                setSelection(newStr.length)
            } else {
                // Two display conditions，.| or |.
                setSelection(getSelectionIndex(newStr, numberCount) + if (isDotSeparatorBeforeSelection) 1 else 0)
            }
            isDotSeparatorBeforeSelection = false
        } catch (e: Exception) {
            Log.d("NumberInputEditText", e.toString())
        }
        addInnerTextChangedListener()
    }

    /**
     * Used to calculate the cursor position.
     * Offset indicates how many thousandths or decimal points are in front of the cursor,
     * and numberCount indicates the number of digits
     */
    open fun getSelectionIndex(newStr: String, numberCount: Int): Int {
        var groupingAndDotCount = 0
        var temp = numberCount
        for (i in newStr.indices) {
            if (isNumber(newStr, i)) {
                temp--
            } else {
                groupingAndDotCount++
            }
            if (temp <= 0) {
                break
            }
        }
        return groupingAndDotCount + numberCount
    }

    init {
        outWatcherList = mutableListOf()
        outWatcherList?.add(scaleTextWatcher)
        addInnerTextChangedListener()
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        inputConnection = super.onCreateInputConnection(outAttrs)
        return inputConnection
    }

    private fun addInnerTextChangedListener() {
        super.addTextChangedListener(innerTextWatcher)
    }

    private fun removeInnerTextChangedListener() {
        super.removeTextChangedListener(innerTextWatcher)
    }

    override fun addTextChangedListener(watcher: TextWatcher) {
        outWatcherList?.add(watcher)
    }

    override fun removeTextChangedListener(watcher: TextWatcher?) {
        if (outWatcherList?.contains(watcher) == true) {
            outWatcherList?.remove(watcher)
        }
    }

    private fun getNumberBeforeFormat(number: String, locale: Locale): String {
        var result = ""
        for (i in number.indices) {
            if (LocalizationInputUtils.acceptNumber.contains(number[i].toString())
                || TextUtils.equals(locale.getDotChar(), number[i].toString())
            ) {
                result += number[i].toString()
            }
        }
        return result
    }

    private fun arrayOfNumberInputFilters(): Array<InputFilter> {
        needFormatNumber = true
        shouldAddOutFilter = false
        return arrayOf(numberInputFilter.apply {
            maxDecimal = maxDecimalValue
            useThousandsSeparator = showGroupingSeparator
            localeFilter = locale
            disableFilter = false
        })
    }

    /**
     * Support the business party to directly pass in multiple filters.
     */
    override fun setFilters(filters: Array<out InputFilter>?) {
        // When an activity or fragment is created, this value may be empty.
        if (numberInputFilter == null) {
            return
        }
        val outFilterList = filters?.toMutableList() ?: return
        if (outFilterList.contains(numberInputFilter)) {
            outFilterList.remove(numberInputFilter)
        }
        val allFilters: MutableList<InputFilter> = mutableListOf()
        // add inner filter.
        if (needFormatNumber) {
            allFilters.add(numberInputFilter)
        }
        // add filter form outside.
        allFilters.addAll(outFilterList)
        super.setFilters(allFilters.toTypedArray())
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        when (id) {
            R.id.paste -> {
                isFromCopy = true
            }
        }
        return super.onTextContextMenuItem(id)
    }

    /**
     * Remove all filters belong to this edit text.
     */
    fun removeFilters() {
        needFormatNumber = false
        filters = arrayOf()
        numberInputFilter.disableFilter = true
    }

    private fun formatNumber(number: String): String {
        if (TextUtils.equals(preNumber, number)) {
            return number
        }
        // firstly,remove thousands separator.
        // Why do we need to remove the thousandths first? For example, in Italian,
        // if the thousandth is turned on, the last input format is 1.234.
        // If you format it directly without removing the thousandth,
        // you will get a value of 1,234, which is definitely wrong.
        var newStr = unFormat(number, false, locale)
        // No need to continue if the input have invalid char.
        if (!LocalizationInputUtils.isLegalString(
                newStr,
                legalCharacters,
                locale,
                showGroupingSeparator
            )
        ) {
            return ""
        }
        newStr = getNumberBeforeFormat(newStr, locale)
        // (1) intercept the data before the second decimal point
        var firstDotPosition = -1
        var secondDotPosition = -1
        if (isFromCopy) {
            for (i in newStr.indices) {
                if (TextUtils.equals(newStr[i].toString(), locale.getDotChar())) {
                    if (firstDotPosition == -1) {
                        firstDotPosition = i
                    } else {
                        secondDotPosition = i
                        break
                    }
                }
            }
        } else {
            firstDotPosition = newStr.indexOf(locale.getDotChar())
        }
        isFromCopy = false
        // decimal points appear next to each other，like： .. or ,,
        if (firstDotPosition + 1 == secondDotPosition) {
            newStr = newStr.substring(0, firstDotPosition)
        } else if (secondDotPosition != -1) {
            newStr = newStr.substring(0, secondDotPosition)
        }
        if (TextUtils.isEmpty(newStr)) {
            return ""
        }
        // (2) delete invalid zeros.
        // To find the last index of zero from begin to end
        var startIndex = 0
        // To find the last index of zero from end to begin.
        var endIndex = newStr.length
        for (i in newStr.indices) {
            if (newStr[i] == '0') {
                startIndex = i
            } else {
                break
            }
        }
        // We have a dot,this means it is a decimal, then we need to find the end index.
        if (firstDotPosition != -1) {
            for (i in (newStr.length - 1) downTo startIndex) {
                if (newStr[i] == '0') {
                    endIndex = i
                } else {
                    break
                }
            }
        }
        // There are three situations :
        // (1) remove 0,for example:001234.5,etc.;
        // (2) add 0,for example:.123456,etc.;
        // (3) stay the same:12345.01,etc.
        if ((startIndex + 1 == newStr.length && (newStr[startIndex] == '0'))
            || ((startIndex + 2 == endIndex) && (firstDotPosition != -1))
        ) {
            newStr = if (isPositiveNumber(newStr, endIndex - 1)) {// 0.4 or 0.400
                "0" + newStr.substring(0)
            } else if ((newStr.length > 1) && (newStr[0] == '0') && (TextUtils.equals(
                    newStr[1].toString(),
                    locale.getDotChar()
                ))
            ) { // 0.
                newStr
            } else { // 0000.00
                if (firstDotPosition != -1) {
                    newStr.substring(startIndex)
                } else { // 0000
                    "0"
                }
            }
        } else if ((firstDotPosition == 0) && (secondDotPosition > 0)) {// .000.00.00.00 or .123456..2200
            newStr = "0" + newStr.substring(0, secondDotPosition)
        } else if (firstDotPosition == 0) { // .4000200
            newStr = "0" + newStr.substring(0)
        } else if ((firstDotPosition != -1)
            && (TextUtils.equals(newStr[startIndex + 1].toString(), locale.getDotChar()))
        ) {// 0000.1256546000
            newStr = newStr.substring(startIndex)
        } else if (firstDotPosition != -1 && isPositiveNumber(newStr, startIndex)
        ) {// 1234.002200
            newStr = newStr.substring(startIndex)
        } else if (firstDotPosition != -1) { // 00001234.123400
            newStr = newStr.substring(startIndex + 1)
        } else if (isPositiveNumber(newStr, startIndex)) { //1234002200,6, etc.
            newStr = newStr.substring(startIndex)
        } else if ((firstDotPosition == -1) && (startIndex + 1 == newStr.length)) { // 0000
            newStr = "0"
        } else { // 0001234
            newStr = newStr.substring(startIndex + 1)
        }
        // If the decimal number of the pasted data exceeds the set number, it will be intercepted.
        newStr = postProcess(newStr, firstDotPosition)
        preNumber = newStr
        return newStr
    }

    /**
     * (1) max length limit
     * (2) remove extra decimal.
     * (3) add thousand separator.
     */
    private fun postProcess(number: String, dotPosition: Int): String {
        var result = number
        result = maxLengthLimit(result, number.indexOf(locale.getDotChar()))
        result = removeExtraDecimal(result, dotPosition)
        if (showGroupingSeparator) {
            result = addThousandSeparator(result)
        }
        return result
    }

    /**
     * add max length judgement.
     * Integer Max length = (max input length) - (Max decimal length)
     */
    private fun maxLengthLimit(number: String, dotPosition: Int): String {
        // condition 1 : it means we don't limit max length.
        if ((maxInputLength > 1000)) {
            return number
        }
        // condition 2 : it means we did't set maxDecimalValue or etc.
        if (maxInputLength < maxDecimalValue) {
            maxDecimalValue = 0
        }
        var integerPart = getIntegerPart(number)
        var decimalPart = getDecimalPart(number)
        // Integer digits = (total length) - (decimal length)
        val maxIntLength = maxInputLength - maxDecimalValue
        if (integerPart.length > maxIntLength) {
            integerPart = integerPart.substring(0, maxIntLength)
        }
        var result = integerPart
        if (dotPosition != -1 && maxDecimalValue > 0) {
            result = integerPart + locale.getDotChar()
            if (!TextUtils.isEmpty(decimalPart)) {
                if (decimalPart.length > maxDecimalValue) {
                    decimalPart = decimalPart.substring(0, maxDecimalValue)
                }
                result += decimalPart
            }
        }
        // Note that the decimal point is not included in the total length.
        if (result.length > maxInputLength) {
            result = if (dotPosition != -1) {
                result.substring(0, maxInputLength + 1)
            } else {
                result.substring(0, maxInputLength)
            }
        }
        return result
    }

    /**
     * If the decimal number of the pasted data exceeds the set number, it will be intercepted.
     */
    private fun removeExtraDecimal(number: String, dotPosition: Int): String {
        var res = number
        if (dotPosition != -1) {
            val lastIndex = dotPosition + maxDecimalValue
            res = if (lastIndex >= res.length) {
                res.substring(0)
            } else if (maxDecimalValue == 0) {
                res.substring(0, lastIndex)
            } else {
                res.substring(0, lastIndex + 1)
            }
        }
        return res
    }

    private fun getIntegerPart(number: String): String {
        if (TextUtils.isEmpty(number)) {
            return ""
        }
        var index = 0
        for (i in number.indices) {
            if (isNumber(number, i)) {
                index++
            } else {
                break
            }
        }
        return number.substring(0, index)
    }

    private fun getDecimalPart(number: String): String {
        if (TextUtils.isEmpty(number)) {
            return ""
        }
        val integerPart = getIntegerPart(number)
        return if ((integerPart.length == number.length)
            || (integerPart.length + 1) == number.length
        ) { // 1234 or 1234.
            ""
        } else {
            number.substring(integerPart.length + 1)
        }
    }

    /**
     * whether the current number is an integer greater than 0,true,bigger greater 0,false ,otherwise.
     */
    private fun isPositiveNumber(inputStr: String, index: Int): Boolean {
        return ('1' <= inputStr[index]) && (inputStr[index] <= '9')
    }

    open fun isNumber(inputStr: String, index: Int): Boolean {
        return ('0' <= inputStr[index]) && (inputStr[index] <= '9')
    }

    /**
     * add thousands separator.after format,add the dot char.
     */
    private fun addThousandSeparator(number: String): String {
        if (number.isEmpty()) {
            return ""
        }
        // A integer less than 1000,like 895, 562, etc.
        val dotPosition = number.indexOf(locale.getDotChar())
        return if (dotPosition == -1) { // integer, like 12345,
            realThousandsNumber(number)
        } else { // decimal，like 12345.12 or 12345.
            realThousandsNumber(number.substring(0, dotPosition)) + number.substring(dotPosition)
        }
    }

    /**
     * get real thousands number
     */
    private fun realThousandsNumber(intNumber: String): String {
        val usDecimalSep = DecimalFormatSymbols(Locale.US).decimalSeparator
        var newStr = intNumber.replace(locale.getDotChar(), usDecimalSep.toString())
        newStr = newStr.formatLocalized(locale)
        return newStr
    }

    /**
     * process the formatted string to plain string.
     * Note: string is the string you get from the above method,
     * not a randomly constructed or backend-acquired string
     * such as in Locale.US: 999,999,999,999.00 -> 999999999999.00
     *            Locale.TR: 999.999.999.999,00 -> 999999999999.00
     */
    private fun unFormat(
        input: String,
        replaceDecimal: Boolean = false,
        locale: Locale = Locale.getDefault()
    ): String {
        try {
            val newStr: String = input.replace(locale.getGroupingSeparator(), "")
            if (!replaceDecimal) {
                return newStr
            }
            val usDecimalSep = DecimalFormatSymbols(Locale.US).decimalSeparator
            return newStr.replace(locale.getDotChar(), usDecimalSep.toString())
        } catch (e: Exception) {
            Log.d("NumberInputEditText", "unFormat: $e")
        }
        return input
    }

    companion object {
        internal const val SELECTION_LENGTH = 10000
    }
}