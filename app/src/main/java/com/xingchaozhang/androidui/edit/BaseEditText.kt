package com.xingchaozhang.androidui.edit

import android.content.Context
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.SpannedString
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.util.AttributeSet
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatEditText
import com.xingchaozhang.androidui.R
import com.xingchaozhang.androidui.utils.px2sp

/**
 * @description : Allow to set hint text size and zoom text size.
 */
open class BaseEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {
    /**
     * hint text size.
     */
    var hintTextSize = 14
        set(value) {
            field = value
            setHintTextSize(hint, field)
        }

    /**
     * It must be set when auto scaling is turned on. The value at the beginning of scaling.
     */
    var maxTextSize: Int = 14

    /**
     * Minimum value when autoscale is turned on.
     */
    var minTextSize: Int = 12

    /**
     * When the size is automatically zoomed, the range will be reduced each time.
     */
    var narrowTextSize: Int = 2

    /**
     * if we allow zoom size. true ,allow ,false,disallow.
     */
    open var autoScale = false
        set(value) {
            field = value
            if (value) {
                addTextChangedListener(scaleTextWatcher)
            } else {
                removeTextChangedListener(scaleTextWatcher)
            }
        }

    private var currentTextSize: Int = 0

    /**
     * length before text changed.
     */
    private var preTextLength: Int = 0

    internal var scaleTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            if (!autoScale) {
                preTextLength = s?.length ?: 0
                return
            }
            if (!TextUtils.isEmpty(s)) {
                if (s!!.length < preTextLength) {
                    viewTreeObserver.addOnPreDrawListener(enlargeListener)
                } else {
                    viewTreeObserver.addOnPreDrawListener(narrowListener)
                }
            } else {
                if (preTextLength > 0) {
                    viewTreeObserver.addOnPreDrawListener(enlargeListener)
                }
            }
            preTextLength = s?.length ?: 0
        }
    }

    /**
     * Listener for narrow
     */
    private val narrowListener = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            val length = paint.measureText(text?.toString())
            if (length > width && currentTextSize > minTextSize) {
                currentTextSize -= narrowTextSize
                textSize = currentTextSize.toFloat()
            } else {
                viewTreeObserver.removeOnPreDrawListener(this)
            }
            return false
        }
    }

    /**
     * Listener for enlarge
     */
    private val enlargeListener = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            val length = paint.measureText(text?.toString())
            if (length > width) {
                viewTreeObserver.removeOnPreDrawListener(this)
                viewTreeObserver.addOnPreDrawListener(narrowListener)
            }
            if (length < width && currentTextSize < maxTextSize) {
                currentTextSize += narrowTextSize
                textSize = if (currentTextSize < maxTextSize) {
                    currentTextSize.toFloat()
                } else {
                    maxTextSize.toFloat()
                }
            } else {
                viewTreeObserver.removeOnPreDrawListener(this)
            }
            return false
        }
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaseEditText)
        autoScale = typedArray.getBoolean(R.styleable.BaseEditText_autoZoomSize, false)
        maxTextSize = typedArray.getInt(R.styleable.BaseEditText_maxTextSize, -1)
        minTextSize = typedArray.getInt(R.styleable.BaseEditText_minTextSize, 12)
        narrowTextSize = typedArray.getInt(R.styleable.BaseEditText_narrowTextSize, 2)
        hintTextSize = typedArray.getInteger(R.styleable.BaseEditText_hintTextSize, -1)
        typedArray.recycle()
        if (hintTextSize != -1) {
            setHintTextSize(hint, hintTextSize)
        }
        if (autoScale) {
            addTextChangedListener(scaleTextWatcher)
        }
    }

    /**
     * setHint is final in textView, so we add this method to call make it possible to adjust hint text size dynamically.
     */
    fun setHintText(hintText: CharSequence?) {
        hint = hintText
        setHintTextSize(hintText, hintTextSize)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        maxTextSize = if (maxTextSize == -1) textSize.px2sp(context) else maxTextSize.px2sp(context)
        currentTextSize = maxTextSize
    }

    private fun setHintTextSize(hintText: CharSequence?, textSize: Int) {
        if (TextUtils.isEmpty(hintText)) {
            return
        }
        val size = intArrayOf(textSize)
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                val hintTotalLength =
                    paint.measureText(hintText.toString()) + paddingStart + paddingEnd
                val length = paint.measureText(hintText.toString())
                if (length > width && size[0] > minTextSize) {
                    size[0] = size[0] - narrowTextSize
                    // Create a new text object that can add attributes
                    val ss = SpannableString(hintText)
                    // Create a new attribute object and set the size of the text
                    val ass = AbsoluteSizeSpan(size[0], true)
                    // Attach attribute to text
                    ss.setSpan(ass, 0, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    // Set hint,Be sure to convert, or the attribute will disappear
                    hint = SpannedString(ss)
                } else {
                    viewTreeObserver.removeOnPreDrawListener(this)
                }
                // If one line is incomplete, use ellipsis instead.
                ellipsize = if (hintTotalLength > width) {
                    TextUtils.TruncateAt.END
                } else {
                    null
                }
                return false
            }
        })
    }
}