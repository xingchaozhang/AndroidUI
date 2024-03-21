package com.xingchaozhang.androidui.edit.input

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import com.okinc.uilab.edit.LocalizationNumberEditText
import com.okinc.uilab.edit.OKInputFieldContentView
import com.xingchaozhang.androidui.R
import com.xingchaozhang.androidui.databinding.LayoutInputBinding
import com.xingchaozhang.androidui.shape.AttributeSetData
import com.xingchaozhang.androidui.shape.ShapeBuilder
import com.xingchaozhang.androidui.utils.dp2pxFloat
import com.xingchaozhang.androidui.utils.dpInt
import com.xingchaozhang.androidui.utils.getString
import com.xingchaozhang.androidui.utils.px2sp
import com.xingchaozhang.androidui.utils.setCursorDrawable
import com.xingchaozhang.androidui.utils.setViewCornerRadius
import com.xingchaozhang.androidui.utils.sp2pxFloat

/**
 * @description : Highly customisable input field.
 */

@SuppressLint("ResourceType")
class Input @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.InputAppearance
) : LinearLayout(context, attrs, defStyleAttr) {

    var contentView: OKInputFieldContentView? = null
    var isErrorShowing: Boolean = false

    /**
     * Parse search custom attributes.
     */
    internal val appearance = InputAppearance(this)

    @ContentHeight
    internal var contentHeight: Int = ContentHeight.MD

    internal var fieldBinding: LayoutInputBinding =
        LayoutInputBinding.inflate(LayoutInflater.from(context), this)

    /**
     * This will decide the position of field.
     */
    internal var fieldType: Int = FIELD_LABEL_OUTSIDE
    internal var label: TextView? = null

    /**
     * Error instance.
     */
    internal var errorTextView: TextView? = null

    /**
     * The line where the helper text is located.
     */
    private var supportingText: Int = 0

    /**
     * Content view int helper layout.
     */
    private var supportingView: View? = null

    private var fieldState: Int = TYPE_STATIC
    private var labelText: CharSequence? = ""
    private var contentText: CharSequence? = ""
    private var contentHintText: CharSequence? = ""
    private var errorText: CharSequence? = ""
    private var inputType: Int = 0
    private var focusListener: ViewTreeObserver.OnGlobalFocusChangeListener? = null

    /**
     *True, only show bottom border, false, otherwise.
     */
    private var onlyBottomLayer = false

    init {
        orientation = VERTICAL
        appearance.loadFromAttributes(attrs, R.attr.InputAppearance)
        val a = context.obtainStyledAttributes(attrs, R.styleable.Input)
        fieldType = a.getInteger(R.styleable.Input_type, FIELD_LABEL_OUTSIDE)
        fieldState = a.getInteger(R.styleable.Input_state, TYPE_STATIC)
        labelText = a.getString(context, R.styleable.Input_labelText)
        contentText = a.getString(context, R.styleable.Input_android_text)
        contentHintText = a.getString(context, R.styleable.Input_android_hint)
        errorText = a.getString(context, R.styleable.Input_errorText)
        contentHeight = a.getInteger(R.styleable.Input_contentHeight, ContentHeight.MD)
        supportingText = a.getInteger(R.styleable.Input_supportingText, 0)
        onlyBottomLayer = a.getBoolean(R.styleable.Input_onlyBottomLayer, false)
        val supportingViewId = a.getResourceId(R.styleable.Input_supportingLayoutId, 0)
        if (supportingViewId != 0) {
            supportingView = LayoutInflater.from(context).inflate(supportingViewId, this, false)
        }
        inputType = a.getInt(R.styleable.Input_android_inputType, EditorInfo.TYPE_CLASS_TEXT)

        a.recycle()

        addLayout()
        setAllTextAppearance()
        setAllTextSize()
        setState(fieldState)
        setAllText()
    }

    /**
     * this method will clear error state.
     */
    fun setState(@State fieldState: Int) {
        this.fieldState = fieldState
        setInputFiledViewEnable(true)
        when (fieldState) {
            TYPE_STATIC -> {
                contentView?.contentBinding?.edtContent?.setTextColor(appearance.contentStaticTextColor)
                setContentBackgroundType(TYPE_STATIC)
            }

            TYPE_ACTIVE -> {
                contentView?.contentBinding?.edtContent?.setTextColor(appearance.contentCriticalTextColor)
                setContentBackgroundType(TYPE_ACTIVE)
            }

            TYPE_CRITICAL -> {
                contentView?.contentBinding?.edtContent?.setTextColor(appearance.contentActiveTextColor)
                setContentBackgroundType(TYPE_CRITICAL)
            }

            TYPE_DISABLE -> {
                contentView?.contentBinding?.edtContent?.setTextColor(appearance.contentDisableTextColor)
                setInputFiledViewEnable(false)
                setContentBackgroundType(TYPE_DISABLE)
            }
        }
    }

    fun getState(): Int {
        return fieldState
    }

    /**
     * set the position of label,The method is named to keep in line with IOS
     */
    fun setTitleType(labelType: Int = FIELD_LABEL_OUTSIDE) {
        if (labelType == fieldType) {
            return
        }
        fieldType = labelType
        updateTitleStyle()
    }

    fun setLabelText(input: CharSequence) {
        labelText = input
        updateTitleStyle()
    }

    /**
     * Get current label view by field type.
     */
    fun getLabelView(): View? {
        return when (fieldType) {
            FIELD_LABEL_INSIDE -> {
                contentView?.contentBinding?.contentLabel
            }

            FIELD_LABEL_OUTSIDE -> {
                label
            }

            else -> {
                contentView?.contentBinding?.title
            }
        }
    }

    fun showClearButtonWhenEditing(isVisible: Boolean = true) {
        contentView?.apply {
            isClearIconVisible = isVisible
            ivFunction?.isVisible = isVisible
        }
    }

    fun isClearIconVisible(): Boolean {
        return contentView?.isClearIconVisible == true
    }

    fun setLabelView(anyView: View) {
        if (fieldType == FIELD_LABEL_INSIDE) {
            fieldBinding.llLabelLayout.visibility = GONE
            contentView?.contentBinding?.rlLabel?.apply {
                removeAllViews()
                addView(anyView)
            }
        } else {
            fieldBinding.llLabelLayout.apply {
                visibility = VISIBLE
                removeAllViews()
                addView(anyView)
            }
        }
    }

    fun getTitleViewGroup(): LinearLayout {
        return fieldBinding.llLabelLayout
    }

    /**
     * Api for contentView.
     */
    fun getContentViewGroup(): LinearLayout {
        return fieldBinding.llContentLayout
    }

    fun getContentTitleTextView(): TextView? {
        return if (fieldType == FIELD_LABEL_INSIDE2) {
            contentView?.contentBinding?.title
        } else {
            contentView?.contentBinding?.contentLabel
        }
    }

    /**
     * Api for contentView.
     */
    fun getContentTitleViewGroup(): RelativeLayout? {
        return contentView?.contentBinding?.rlLabel
    }

    fun getContentEditText(): LocalizationNumberEditText? {
        return contentView?.contentBinding?.edtContent
    }

    fun getContentFunctionViewGroup(): LinearLayout? {
        return contentView?.contentBinding?.llFunction
    }

    fun getErrorContainer(): LinearLayout {
        return fieldBinding.llErrorLayout
    }

    fun getSupportingContainer(): RelativeLayout {
        return fieldBinding.rlHelperLayout
    }

    fun setContentView(anyView: View) {
        fieldBinding.llContentLayout.removeAllViews()
        anyView.layoutParams = generateContentViewLayoutParams()
        if ((contentHeight != ContentHeight.XL || fieldType != FIELD_LABEL_INSIDE2) && !onlyBottomLayer) {
            anyView.setViewCornerRadius(appearance.fieldRadius)
        }
        fieldBinding.llContentLayout.addView(anyView)
    }

    fun setHintText(hintText: CharSequence?) {
        getContentEditText()?.hint = hintText
        contentView?.updateContentAppearance()
    }

    fun setText(text: CharSequence? = null) {
        getContentEditText()?.setText(text)
    }

    fun setSupportingView(anyView: View) {
        supportingView = anyView
        fieldBinding.rlHelperLayout.apply {
            removeAllViews()
            visibility = VISIBLE
            addView(anyView)
        }
        updateHelperErrorViewGroupTopMargin()
    }

    fun getSupportingView(): View? {
        return supportingView
    }

    /**
     * 自定义时，只处理显示与隐藏。需要向外部反馈当前的显示和状态。
     */
    fun setErrorView(anyView: View) {
        fieldBinding.llErrorLayout.apply {
            removeAllViews()
            addView(anyView)
        }
        updateHelperErrorViewGroupTopMargin()
    }

    fun setErrorText(errorMsg: CharSequence) {
        fieldState = TYPE_CRITICAL
        isErrorShowing = true
        fieldBinding.llErrorLayout.visibility = VISIBLE
        errorTextView?.apply {
            movementMethod = LinkMovementMethod.getInstance()
            text = errorMsg
            setTextColor(appearance.errorTextColor)
        }
        setContentBackgroundType(TYPE_CRITICAL)
        updateHelperErrorViewGroupTopMargin()
    }

    fun clearErrorText() {
        if (!isErrorShowing) {
            return
        }
        isErrorShowing = false
        fieldBinding.llErrorLayout.visibility = GONE
        errorTextView?.text = ""
        if (hasFocus()) {
            fieldState = TYPE_ACTIVE
            setContentBackgroundType(TYPE_ACTIVE)
        } else {
            fieldState = TYPE_STATIC
            setContentBackgroundType(TYPE_STATIC)
        }
        updateHelperErrorViewGroupTopMargin()
    }

    fun setSupportVisibility(supportingText: Int = SUPPORTING_TEXT_OFF) {
        addSupportingView()
    }

    /**
     * Change filed state to only bottom border type.
     * @param onlyBottomLayer, True, only show bottom border, false, otherwise.
     */
    fun setOnlyBottomBorder(onlyBottomLayer: Boolean) {
        this.onlyBottomLayer = onlyBottomLayer
        setContentBackgroundType(fieldState)
        setHorizontalPadding()
        setAllTextSize()
    }

    /**
     * Return if Input field only has bottom border, true, only bottom border, false, otherwise.
     */
    fun onlyBottomLayer(): Boolean {
        return onlyBottomLayer
    }

    /**
     * Reset this OKInputField.
     */
    fun reset() {
        clearAllViews()
        addLayout()
        setAllTextAppearance()
        setAllTextSize()
        setState(TYPE_STATIC)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        fieldBinding.llContentLayout.viewTreeObserver?.removeOnGlobalFocusChangeListener(
            focusListener
        )
    }

    private fun setInputFiledViewEnable(enable: Boolean) {
        fieldBinding.llContentLayout.isEnabled = enable
        setViewEnable(fieldBinding.llContentLayout, enable)
        if (enable) {
            if (hasFocus()) {
                setContentBackgroundType(TYPE_ACTIVE)
            } else {
                setContentBackgroundType(TYPE_STATIC)
            }
            contentView?.setBackgroundColor(appearance.contentStaticColor)
        } else {
            setContentBackgroundType(TYPE_DISABLE)
            contentView?.setBackgroundColor(appearance.contentDisableColor)
        }
        if (!isErrorShowing) {
            clearErrorText()
        }
    }

    private fun setContentBackground(color: Int) {
        fieldBinding.llContentLayout.setBackgroundResource(ContextCompat.getColor(context, color))
    }

    internal fun setContentBackgroundType(state: Int) {
        if (onlyBottomLayer) {
            contentView?.setViewCornerRadius(0f)
            when (state) {
                TYPE_STATIC -> {
                    appearance.setLayerBackground(fieldBinding.llContentLayout)
                    contentView?.setBackgroundColor(appearance.contentActiveColor)
                }

                TYPE_ACTIVE -> {
                    appearance.setLayerBackground(
                        fieldBinding.llContentLayout,
                        appearance.contentLayerActiveColor
                    )
                    contentView?.setBackgroundColor(appearance.contentStaticColor)
                }

                TYPE_CRITICAL -> {
                    appearance.setLayerBackground(
                        fieldBinding.llContentLayout,
                        appearance.contentLayerCriticalColor
                    )
                    contentView?.setBackgroundColor(appearance.contentCriticalColor)
                }

                TYPE_DISABLE -> {
                    appearance.setLayerBackground(fieldBinding.llContentLayout)
                    contentView?.setBackgroundColor(appearance.contentDisableColor)
                }
            }
        } else {
            when (state) {
                TYPE_STATIC -> {
                    setContentLayerBackground(
                        appearance.contentLayerStaticColor,
                        appearance.contentStaticColor
                    )
                    contentView?.setBackgroundColor(appearance.contentStaticColor)
                }

                TYPE_ACTIVE -> {
                    setContentLayerBackground(
                        appearance.contentLayerActiveColor,
                        appearance.contentActiveColor
                    )
                    contentView?.setBackgroundColor(appearance.contentActiveColor)
                }

                TYPE_CRITICAL -> {
                    setContentLayerBackground(
                        appearance.contentLayerCriticalColor,
                        appearance.contentCriticalColor
                    )
                    contentView?.setBackgroundColor(appearance.contentCriticalColor)
                }

                TYPE_DISABLE -> {
                    setContentLayerBackground(
                        appearance.contentLayerDisableColor,
                        ContextCompat.getColor(context, R.color.transparent)
                    )
                    contentView?.setBackgroundColor(appearance.contentDisableColor)
                }
            }
        }
    }

    private fun addLayout() {
        addLabelView()

        addContentView()

        addSupportingView()

        addErrorView()

        updateHelperErrorViewGroupTopMargin()
    }

    private fun addLabelView() {
        label = TextView(context).apply {
            setTextColor(appearance.contentStaticTextColor)
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
        }
        fieldBinding.llLabelLayout.addView(
            label,
            LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.START
            })
    }

    private fun addContentView() {
        if (fieldBinding.llContentLayout.hasFocus()) {
            setContentBackgroundType(TYPE_ACTIVE)
        } else {
            setContentBackgroundType(TYPE_STATIC)
        }
        focusListener = ViewTreeObserver.OnGlobalFocusChangeListener { _, _ ->
            if (isErrorShowing) {
                setContentBackgroundType(TYPE_CRITICAL)
            } else if (!fieldBinding.llContentLayout.isEnabled) {
                setContentBackgroundType(TYPE_DISABLE)
            } else if (fieldBinding.llContentLayout.hasFocus()) {
                setContentBackgroundType(TYPE_ACTIVE)
            } else {
                setContentBackgroundType(TYPE_STATIC)
            }
        }
        fieldBinding.llContentLayout.viewTreeObserver?.addOnGlobalFocusChangeListener(
            focusListener
        )
        // Add a default contentView to the InputField.
        contentView = OKInputFieldContentView(context).apply {
            setInput(this@Input)
            layoutParams = generateContentViewLayoutParams()
            setViewCornerRadius(appearance.fieldRadius)
            setContentBackgroundColorByType(fieldType)
            contentBinding.edtContent.apply {
                setCursorDrawable(appearance.cursorColor, 2f.dpInt(context))
                inputType = this@Input.inputType
            }
            setPasswordChar(appearance.passwordChar)
            setImageTint(appearance.tintColor)
        }
        setHorizontalPadding()
        fieldBinding.llContentLayout.addView(contentView)
        // We adjust set label text code at here, because in some situations, Label text is in content view.
        if (!TextUtils.isEmpty(labelText)) {
            labelText?.let { setLabelText(it) }
        }
    }

    private fun addSupportingView() {
        if (supportingText == SUPPORTING_TEXT_OFF || supportingView == null) {
            fieldBinding.rlHelperLayout.visibility = GONE
        } else {
            fieldBinding.rlHelperLayout.apply {
                removeAllViews()
                addView(supportingView)
                visibility = VISIBLE
            }
        }
    }

    private fun addErrorView() {
        errorTextView = TextView(context)
        errorTextView?.apply {
            gravity = Gravity.START
            setTextColor(appearance.errorTextColor)
        }
        fieldBinding.llErrorLayout.addView(errorTextView)
    }

    private fun updateTitleStyle() {
        when (fieldType) {
            FIELD_LABEL_INSIDE -> {
                fieldBinding.llLabelLayout.isVisible = false
                contentView?.contentBinding?.apply {
                    title.isVisible = false
                    contentLabel.apply {
                        isVisible = true
                        text = labelText
                    }
                }
                getContentEditText()?.gravity = Gravity.CENTER_VERTICAL or Gravity.END
            }

            FIELD_LABEL_OUTSIDE -> {
                fieldBinding.llLabelLayout.isVisible = true
                contentView?.contentBinding?.apply {
                    title.isVisible = false
                    contentLabel.isVisible = false
                }
                label?.text = labelText
                getContentEditText()?.gravity = Gravity.CENTER_VERTICAL or Gravity.START
            }

            else -> {
                fieldBinding.llLabelLayout.isVisible = false
                contentView?.contentBinding?.apply {
                    title.apply {
                        isVisible = true
                        text = labelText
                    }
                    contentLabel.isVisible = false
                }
                contentView?.updateContentAppearance(!TextUtils.isEmpty(getContentEditText()?.text))
            }
        }
    }

    private fun setContentBackgroundColorByType(type: Int) {
        when (type) {
            TYPE_STATIC -> {
                contentView?.setBackgroundColor(appearance.contentStaticColor)
            }

            TYPE_ACTIVE -> {
                contentView?.setBackgroundColor(appearance.contentActiveColor)
            }

            TYPE_CRITICAL -> {
                contentView?.setBackgroundColor(appearance.contentCriticalColor)
            }

            TYPE_DISABLE -> {
                contentView?.setBackgroundColor(appearance.contentDisableColor)
            }
        }
    }

    private fun setHorizontalPadding() {
        when (contentHeight) {
            ContentHeight.SM -> {
                contentView?.setPaddingRelative(appearance.contentSMMarginHorizontal, 0, 0, 0)
            }

            ContentHeight.MD -> {
                contentView?.setPaddingRelative(appearance.contentMDMarginHorizontal, 0, 0, 0)
            }

            ContentHeight.LG -> {
                contentView?.setPaddingRelative(appearance.contentLGMarginHorizontal, 0, 0, 0)
            }

            ContentHeight.XL -> {
                if (onlyBottomLayer) {
                    contentView?.setPaddingRelative(
                        appearance.onlyBottomLayerMarginHorizontal,
                        0,
                        0,
                        0
                    )
                    updateLabelOutsideBottomMargin()
                } else {
                    contentView?.setPaddingRelative(appearance.contentLGMarginHorizontal, 0, 0, 0)
                    updateLabelOutsideBottomMargin(8.dpInt(context))
                }
            }

            else -> {
                contentView?.setPaddingRelative(appearance.onlyBottomLayerMarginHorizontal, 0, 0, 0)
                updateLabelOutsideBottomMargin()
            }
        }
    }

    private fun updateLabelOutsideBottomMargin(margin: Int = 4.dpInt(context)) {
        fieldBinding.llLabelLayout.layoutParams.also {
            if (it is MarginLayoutParams) {
                it.bottomMargin = margin
            }
        }
    }

    internal fun generateContentViewLayoutParams(): ViewGroup.LayoutParams {
        val contentLp = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, contentHeight.dpInt(context)
        )
        if (onlyBottomLayer) {
            contentLp.setMargins(0, 0, 0, appearance.strokeWidth.toInt())
        } else {
            contentLp.setMargins(
                appearance.strokeWidth.toInt(),
                appearance.strokeWidth.toInt(),
                appearance.strokeWidth.toInt(),
                appearance.strokeWidth.toInt()
            )
        }
        updateHelperErrorViewGroupTopMargin()
        return contentLp
    }

    internal fun setAllTextAppearance() {
        label?.let {
            TextViewCompat.setTextAppearance(it, appearance.labelTextAppearance)
            if (fieldType == FIELD_LABEL_OUTSIDE) {
                it.setTextColor(appearance.labelOutTextColor)
            }
        }
        contentView?.contentBinding?.apply {
            contentLabel.setTextColor(appearance.labelInTextColor)
            edtContent.let {
                if (contentHeight == ContentHeight.XXL) {
                    TextViewCompat.setTextAppearance(it, R.style.OKTextFont_Bold)
                } else if (onlyBottomLayer) {
                    TextViewCompat.setTextAppearance(it, R.style.OKTextFont_Medium)
                } else {
                    TextViewCompat.setTextAppearance(it, appearance.contentTextAppearance)
                }
                it.apply {
                    setTextColor(appearance.contentStaticTextColor)
                    setHintTextColor(appearance.hintTextColor)
                }
            }
        }
        errorTextView?.let {
            TextViewCompat.setTextAppearance(it, appearance.errorTextAppearance)
            it.apply {
                setTextColor(appearance.errorTextColor)
            }
        }
    }

    private fun setAllTextSize() {
        val labelTextSize: Float
        val contentTextSize: Float
        val errorTextSize: Float
        when (contentHeight) {
            ContentHeight.SM -> {
                labelTextSize = appearance.labelSMTextSize
                contentTextSize = appearance.contentSMTextSize
                errorTextSize = appearance.errorSMTextSize
            }

            ContentHeight.MD -> {
                labelTextSize = appearance.labelMDTextSize
                contentTextSize = appearance.contentMDTextSize
                errorTextSize = appearance.errorMDTextSize
            }

            ContentHeight.LG -> {
                labelTextSize = appearance.labelLGTextSize
                contentTextSize = appearance.contentLGTextSize
                errorTextSize = appearance.errorLGTextSize
            }

            ContentHeight.XL -> {
                labelTextSize = appearance.labelXLTextSize
                contentTextSize = if (onlyBottomLayer) {
                    24f.sp2pxFloat(context)
                } else if (fieldType == FIELD_LABEL_INSIDE2) {
                    16f.sp2pxFloat(context)
                } else {
                    appearance.contentXLTextSize
                }
                errorTextSize = appearance.errorXLTextSize
            }

            ContentHeight.XXL -> {
                labelTextSize = appearance.labelHighLightLGTextSize
                contentTextSize = appearance.contentHighLightLGTextSize
                errorTextSize = appearance.errorHighLightLGTextSize
            }

            else -> {
                labelTextSize = appearance.labelSMTextSize
                contentTextSize = 10f.dp2pxFloat(context)
                errorTextSize = appearance.errorSMTextSize
            }
        }
        if (fieldType == FIELD_LABEL_INSIDE) {
            label?.setTextSize(TypedValue.COMPLEX_UNIT_PX, contentTextSize)
        } else {
            label?.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelTextSize)
        }
        contentView?.contentBinding?.edtContent?.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, contentTextSize)
            maxTextSize = contentTextSize.px2sp(context)
        }
        errorTextView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, errorTextSize)
    }

    /**
     * When the visibility of helper or error view changed. we should update its top margin dynamically.
     */
    private fun updateHelperErrorViewGroupTopMargin() {
        fieldBinding.helperErrorViewGroup.layoutParams.also {
            if (it is MarginLayoutParams) {
                it.topMargin =
                    if (!fieldBinding.rlHelperLayout.isVisible && !fieldBinding.llErrorLayout.isVisible) {
                        0
                    } else if (!onlyBottomLayer && contentHeight == ContentHeight.XL) {
                        8.dpInt(context)
                    } else {
                        4.dpInt(context)
                    }
            }
        }
    }

    private fun setAllText() {
        getContentEditText()?.setText(contentText)
        if (!TextUtils.isEmpty(contentHintText)) {
            setHintText(contentHintText)
        }
        // Because Appearance has text color, so we need to set error text color here.
        if (!TextUtils.isEmpty(errorText)) {
            errorText?.let { setErrorText(it) }
            isErrorShowing = true
        }
    }

    private fun clearAllViews() {
        fieldBinding.apply {
            llLabelLayout.removeAllViews()
            llContentLayout.removeAllViews()
            rlHelperLayout.removeAllViews()
            llErrorLayout.removeAllViews()
        }
    }

    private fun setViewEnable(view: View?, enable: Boolean) {
        if (view == null) {
            return
        }
        view.apply {
            isEnabled = enable
            isClickable = enable
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                if (child is ViewGroup) {
                    setViewEnable(view.getChildAt(i), enable)
                } else {
                    child.apply {
                        isEnabled = enable
                        isClickable = enable
                    }
                }
            }
        }
    }

    /**
     * okx has some special color for background.
     */
    private fun setContentLayerBackground(layerColor: Int, contentColor: Int) {
        val shapeBuilder = ShapeBuilder()
        val attributeSetData = AttributeSetData()
        attributeSetData.apply {
            shapeType = GradientDrawable.RECTANGLE
            // For okxl, in dark mode, the color of layer and content is the same.
            // so we add this code to deal with this situation.
            strokeColor = layerColor
            solidColor = contentColor
            strokeWidth = appearance.strokeWidth.toInt()
            cornersRadius = appearance.fieldRadius
        }
        shapeBuilder.init(fieldBinding.llContentLayout, attributeSetData)
    }

    companion object {
        const val TYPE_STATIC = 0
        const val TYPE_ACTIVE = 1
        const val TYPE_CRITICAL = 2
        const val TYPE_DISABLE = 3

        /**
         * for label position.
         */
        const val FIELD_LABEL_INSIDE = 0
        const val FIELD_LABEL_OUTSIDE = 1

        /**
         * Label position for OKDS2.0
         */
        const val FIELD_LABEL_INSIDE2 = 2

        /**
         * show or hide helper View.
         */
        const val SUPPORTING_TEXT_OFF = 0
        const val SUPPORTING_TEXT_ON = 1

    }

    annotation class ContentHeight {
        companion object {
            const val SM = 36
            const val MD = 40
            const val LG = 44
            const val XL = 48

            /**
             * Add for high light.
             */
            const val XXL = 72
        }
    }

    @IntDef(TYPE_STATIC, TYPE_ACTIVE, TYPE_CRITICAL, TYPE_DISABLE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class State
}