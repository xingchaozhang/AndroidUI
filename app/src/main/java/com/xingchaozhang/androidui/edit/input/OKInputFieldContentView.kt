package com.okinc.uilab.edit

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.xingchaozhang.androidui.R
import com.xingchaozhang.androidui.databinding.LayoutInputContentBinding
import com.xingchaozhang.androidui.edit.input.Input
import com.xingchaozhang.androidui.edit.input.Input.Companion.FIELD_LABEL_INSIDE
import com.xingchaozhang.androidui.utils.dpInt
import com.xingchaozhang.androidui.utils.showKeyboardAndFocus

/**
 * @description : Provide the user with a default EditText with a delete button
 */
class OKInputFieldContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    /**
     * Used to set error messages
     */
    var validator: ((CharSequence?) -> CharSequence?)? = null

    /**
     * Callback when EditText focus changes.
     */
    var onFocusChangeCallback: ((hasFocus: Boolean) -> Unit)? = null

    /**
     * If we need to disable the child View from intercepting events.
     */
    var interceptEvent = false
        set(value) {
            field = value
            contentBinding.edtContent.apply {
                isEnabled = !value
                isFocusable = !value
                isFocusableInTouchMode = !value
            }
        }

    /**
     * To control the visibility of keypad, true, show keyboard force, false otherwise.
     */
    var forceShowKeyboard = true

    internal var edtContent: LocalizationNumberEditText? = null

    internal var contentBinding: LayoutInputContentBinding =
        LayoutInputContentBinding.inflate(LayoutInflater.from(context), this)

    internal var isClearIconVisible = true
        set(value) {
            field = value
            if (value) {
                setContentHorizontalPadding(false)
            } else {
                setContentHorizontalPadding()
            }
        }

    internal var ivFunction: ImageView? = null

    private var popWindow: PopupWindow? = null
    private var inputField: Input? = null
    private var isFunctionViewReplaced = false
    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            validator?.invoke(s)?.let {
                if (!TextUtils.isEmpty(it)) {
                    inputField?.apply {
                        isErrorShowing = true
                        setErrorText(it.toString())
                        fieldBinding.llErrorLayout.visibility = VISIBLE
                    }
                } else {
                    inputField?.apply {
                        clearErrorText()
                        isErrorShowing = false
                        fieldBinding.llErrorLayout.visibility = GONE
                    }
                }
            }
            if (hasFocus()) {
                updateContentAppearance()
            } else {
                if (TextUtils.isEmpty(s)
                    && TextUtils.isEmpty(contentBinding.edtContent.hint)
                ) {
                    updateContentAppearance(false)
                } else {
                    updateContentAppearance()
                }
            }
            if (isFunctionViewReplaced) {
                return
            }
            if (!isClearIconVisible) {
                contentBinding.llFunction.visibility = GONE
            } else if (!TextUtils.isEmpty(s) && contentBinding.edtContent.hasFocus()) {
                contentBinding.llFunction.visibility = VISIBLE
            } else {
                popWindow?.dismiss()
                contentBinding.llFunction.visibility = INVISIBLE
            }
        }
    }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        initEditText()
    }

    fun getInputContent(): LinearLayout {
        return contentBinding.inputContent
    }

    fun replaceFunctionView(anyView: View) {
        isFunctionViewReplaced = true
        contentBinding.llFunction.apply {
            removeAllViews()
            visibility = VISIBLE
            minimumWidth = inputField?.appearance?.clearMiniSize ?: 0

            addView(View(context).apply {
                setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
            }, 4f.dpInt(context), MATCH_PARENT)

            addView(anyView)
        }
        setContentHorizontalPadding()
    }

    /**
     * @param drawableId drawableId,
     * @param width,     drawable width.
     * @param height,    drawable height.
     */
    @JvmOverloads
    fun replaceFunctionDrawable(@DrawableRes drawableId: Int, width: Int = 0, height: Int = 0) {
        setIconImage(drawableId, width, height)
    }

    /**
     * @param drawable drawable ,
     * @param width,   drawable width.
     * @param height,  drawable height.
     */
    @JvmOverloads
    fun replaceFunctionDrawable(drawable: Drawable?, width: Int = 0, height: Int = 0) {
        isFunctionViewReplaced = true
        contentBinding.apply {
            edtContent.removeTextChangedListener(textWatcher)
            llFunction.apply {
                removeAllViews()
                visibility = VISIBLE
            }
        }
        setFunctionImage(drawable, true)
    }

    fun hideInput() {
        contentBinding.edtContent.apply {
            transformationMethod = AsteriskPasswordTransformationMethod.instance
            setSelection(contentBinding.edtContent.text?.length ?: 0)
        }
    }

    fun showInput() {
        contentBinding.edtContent.apply {
            transformationMethod = HideReturnsTransformationMethod.getInstance()
            setSelection(contentBinding.edtContent.text?.length ?: 0)
        }
    }

    /**
     * Customized display styles.
     */
    fun setPasswordChar(passwordChar: Char) {
        AsteriskPasswordTransformationMethod.instance?.passwordChar = passwordChar
    }

    fun setImageTint(color: Int) {
        ivFunction?.apply {
            let { ImageViewCompat.setImageTintList(it, ColorStateList.valueOf(color)) }
            imageTintMode = PorterDuff.Mode.SRC_IN
        }
    }

    /**
     * Function view will be delete,but paddings still exist.
     */
    fun removeFunctionView() {
        contentBinding.llFunction.apply {
            removeAllViews()
            layoutParams.also {
                it?.apply {
                    width = 0
                    height = 0
                }
                this.layoutParams = it
            }
        }
        setContentHorizontalPadding()
    }

    fun addTextWatcher() {
        contentBinding.edtContent.addTextChangedListener(textWatcher)
    }

    fun removeTextWatcher() {
        contentBinding.edtContent.removeTextChangedListener(textWatcher)
    }

    @Deprecated("Use replaceFunctionDrawable() instead.")
    @JvmOverloads
    fun setIconImage(drawableId: Int, width: Int = 0, height: Int = 0) {
        isFunctionViewReplaced = true
        contentBinding.apply {
            edtContent.removeTextChangedListener(textWatcher)
            llFunction.removeAllViews()
            llFunction.visibility = VISIBLE
        }
        setFunctionImage(ContextCompat.getDrawable(context, drawableId), true, width, height)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (interceptEvent) {
            true
        } else {
            super.onInterceptTouchEvent(ev)
        }
    }

    private fun initEditText() {
        edtContent = contentBinding.edtContent
        contentBinding.edtContent.apply {
            addTextChangedListener(textWatcher)
            onFocusChangeListener = object : OnFocusChangeListener {
                override fun onFocusChange(v: View?, hasFocus: Boolean) {
                    onFocusChangeCallback?.invoke(hasFocus)
                    if (hasFocus) {
                        // On some devices, the keyboard can not show occasionally, so we add codes here to prevent this situation.
                        if (forceShowKeyboard) {
                            val inputMethodManager: InputMethodManager =
                                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            inputMethodManager.showSoftInput(
                                contentBinding.edtContent,
                                InputMethodManager.SHOW_IMPLICIT
                            )
                        }
                        updateContentAppearance()
                    } else if (!hasFocus && !TextUtils.isEmpty(contentBinding.edtContent.text)) {
                        updateContentAppearance()
                    } else if (!hasFocus
                        && TextUtils.isEmpty(contentBinding.edtContent.text)
                        && !TextUtils.isEmpty(contentBinding.edtContent.hint)
                    ) {
                        updateContentAppearance()
                    } else {
                        updateContentAppearance(false)
                    }
                    if (isFunctionViewReplaced) {
                        return
                    }
                    contentBinding.llFunction.apply {
                        visibility = if (!isClearIconVisible) {
                            GONE
                        } else if (hasFocus && !TextUtils.isEmpty(contentBinding.edtContent.text)) {
                            View.VISIBLE
                        } else {
                            View.INVISIBLE
                        }
                    }
                }
            }
            setPaddingRelative(0, 0, 0, 0)
        }
    }

    internal fun updateContentAppearance(isInputContentVisible: Boolean = true) {
        contentBinding.apply {
//            inputContent.isVisible = isInputContentVisible
//            interceptEvent = !isInputContentVisible
            if (isInputContentVisible) {
                (title.layoutParams as RelativeLayout.LayoutParams).removeRule(RelativeLayout.CENTER_VERTICAL)
            } else {
                (title.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.CENTER_VERTICAL)
            }
            title.textSize =
                if (inputField?.contentHeight == Input.ContentHeight.MD) {
                    if (isInputContentVisible) 10f else 14f
                } else {
                    if (isInputContentVisible) 12f else 16f
                }
        }
    }

    private fun setFunctionImage(
        drawableId: Drawable?,
        replace: Boolean,
        width: Int = 0,
        height: Int = 0
    ) {
        ivFunction = ImageView(context)
        val ivContentLp = RelativeLayout.LayoutParams(
            if (width > 0) width else inputField?.appearance?.clearDrawableSize ?: 0,
            if (height > 0) height else inputField?.appearance?.clearDrawableSize ?: 0
        )
        ivContentLp.apply {
            marginStart = 4.dpInt(context)
            addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
        }
        ivFunction?.apply {
            layoutParams = ivContentLp
            setImageDrawable(drawableId)
            if (!replace) {
                setOnClickListener { contentBinding.edtContent.setText("") }
            } else {
                imageTintMode = PorterDuff.Mode.DST
            }
        }
        contentBinding.llFunction.addView(ivFunction)
    }

    private fun setContentHorizontalPadding(needEndPadding: Boolean = true) {
        when (inputField?.contentHeight) {
            Input.ContentHeight.SM -> {
                if (inputField?.onlyBottomLayer() == true) {
                    inputField?.appearance?.contentSMMarginHorizontal?.let {
                        setPaddingRelative(0, 0, 0, 0)
                    }
                } else {
                    inputField?.appearance?.contentSMMarginHorizontal?.let {
                        setPaddingRelative(it, 0, if (needEndPadding) it else 0, 0)
                    }
                }
            }

            Input.ContentHeight.MD -> {
                if (inputField?.onlyBottomLayer() == true) {
                    inputField?.appearance?.contentMDMarginHorizontal?.let {
                        setPaddingRelative(0, 0, 0, 0)
                    }
                } else {
                    inputField?.appearance?.contentMDMarginHorizontal?.let {
                        setPaddingRelative(it, 0, if (needEndPadding) it else 0, 0)
                    }
                }
            }

            Input.ContentHeight.LG -> {
                if (inputField?.onlyBottomLayer() == true) {
                    inputField?.appearance?.contentLGMarginHorizontal?.let {
                        setPaddingRelative(0, 0, 0, 0)
                    }
                } else {
                    inputField?.appearance?.contentLGMarginHorizontal?.let {
                        setPaddingRelative(it, 0, if (needEndPadding) it else 0, 0)
                    }
                }
            }

            Input.ContentHeight.XL -> {
                if (inputField?.onlyBottomLayer() == true) {
                    inputField?.appearance?.contentXLMarginHorizontal?.let {
                        setPaddingRelative(0, 0, 0, 0)
                    }
                } else {
                    inputField?.appearance?.contentXLMarginHorizontal?.let {
                        setPaddingRelative(it, 0, if (needEndPadding) it else 0, 0)
                    }
                }
            }
        }
    }

    internal fun setInput(input: Input) {
        this.inputField = input
        if (this.inputField?.fieldType == FIELD_LABEL_INSIDE) {
            contentBinding.edtContent.gravity = Gravity.CENTER_VERTICAL or Gravity.END
        }
        setOnClickListener {
            updateContentAppearance()
            (contentBinding.title.layoutParams as RelativeLayout.LayoutParams).removeRule(
                RelativeLayout.CENTER_VERTICAL
            )
            contentBinding.edtContent.showKeyboardAndFocus()
            if (input.isErrorShowing) {
                input.setState(Input.TYPE_CRITICAL)
            } else {
                input.setState(Input.TYPE_ACTIVE)
            }
        }
        contentBinding.title.setOnClickListener {
            performClick()
        }
        contentBinding.llFunction.setOnClickListener {
            performClick()
        }
        // Auto scale text.
        contentBinding.edtContent.autoScale = (this.inputField?.appearance?.autoScale == true)
        setDefaultFunctionView()
    }

    private fun setDefaultFunctionView() {
        setFunctionImage(ContextCompat.getDrawable(context, inputField?.appearance?.clearDrawable ?: 0), false)
        contentBinding.llFunction.apply {
            visibility = INVISIBLE
            minimumWidth = inputField?.appearance?.clearMiniSize ?: 0
        }
    }

    class AsteriskPasswordTransformationMethod : PasswordTransformationMethod() {
        var passwordChar = '*'

        companion object {
            private var sInstance: AsteriskPasswordTransformationMethod? = null
            val instance: AsteriskPasswordTransformationMethod?
                get() {
                    if (sInstance != null) {
                        return sInstance
                    }
                    sInstance = AsteriskPasswordTransformationMethod()
                    return sInstance
                }
        }

        override fun getTransformation(source: CharSequence, view: View): CharSequence {
            return PasswordCharSequence(source)
        }

        private inner class PasswordCharSequence(private val mSource: CharSequence) : CharSequence {
            override val length: Int
                get() = mSource.length

            override fun get(index: Int): Char {
                return passwordChar
            }

            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
                return mSource.subSequence(startIndex, endIndex)
            }
        }
    }
}