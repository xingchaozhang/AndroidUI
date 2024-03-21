package com.xingchaozhang.androidui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat
import com.xingchaozhang.androidui.utils.ViewHelper
import com.xingchaozhang.androidui.utils.dp2pxFloat
import com.xingchaozhang.androidui.utils.dpInt
import com.xingchaozhang.androidui.utils.getNavigationBarHeight
import com.xingchaozhang.androidui.utils.getRealScreenHeight
import com.xingchaozhang.androidui.utils.getScreenWidth
import com.xingchaozhang.androidui.utils.getStatusBarHeight
import com.xingchaozhang.androidui.utils.isRtl
import com.xingchaozhang.androidui.view.ShadowLayout

/**
 * @author ： zhangxingchao
 * @date : 2023/5/6 19:32
 * @description : 控件提示弹出窗，可自定义弹出的位置，持续时间以及样式
 */
class TipTool private constructor(private val anchor: View) {
    companion object {
        /**
         * Used to control when the pop-up window disappears automatically.
         */
        private const val DEFAULT_TIP_SHOW_DURATION = Int.MAX_VALUE / 10

        /**
         * These two variables are used to control the height and width of the arrows
         */
        private val ARROW_HEIGHT = 8f.dpInt()

        /**
         * The width of arrow.
         */
        private val ARROW_WIDTH = 8f.dpInt()

        /**
         * Create and set the View to which the prompt control depends.
         */
        fun setAnchor(view: View): TipTool {
            return TipTool(view)
        }
    }

    internal val tipView: TipView

    internal var popupWindow: PopupWindow? = null

    private var shadowLayout: ShadowLayout? = null

    /**
     * The position to display.
     */
    enum class Position {
        /**
         * Bubble will be displayed on the left of anchor.
         */
        START,

        /**
         * Bubble will be displayed on the top of anchor.
         */
        TOP,

        /**
         * Bubble will be displayed on the right of anchor.
         */
        END,

        /**
         * Bubble will be displayed on the bottom of anchor.
         */
        BOTTOM
    }

    /**
     * Position to align bubble.
     */
    enum class Alignment {
        /**
         * Bubble and view a start-aligned.
         */
        START,

        /**
         * Bubble and view a center-aligned.
         */
        CENTER,

        /**
         * Bubble and view a end-aligned.
         */
        END
    }

    init {
        tipView = TipView(getActivityContext(anchor.context)).apply {
            setAnchorView(anchor)
        }
        shadowLayout = ShadowLayout(anchor.context)
        popupWindow = PopupWindow(shadowLayout, WRAP_CONTENT, WRAP_CONTENT).apply {
            isOutsideTouchable = true
        }
    }

    /**
     * Set the position of tip view.
     */
    fun setPosition(position: Position): TipTool {
        tipView.setPosition(position)
        return this
    }

    /**
     * Set custom view
     */
    fun setCustomView(customView: View): TipTool {
        tipView.setCustomView(customView)
        return this
    }

    fun setCustomView(viewId: Int): TipTool {
        tipView.setCustomView(
            LayoutInflater.from(anchor.context).inflate(viewId, null, false)
        )
        return this
    }

    /*fun setAlignment(alignment: Alignment): TipTool {
        tipView.setAlignment(alignment)
        return this
    }*/

    /**
     * Set how long the tip can display.
     */
    fun setDuration(duration: Long): TipTool {
        tipView.setDuration(duration)
        return this
    }

    /**
     * Set tip background color
     */
    fun setTipBackgroundColor(color: Int): TipTool {
        tipView.setBubbleBackgroundColor(color)
        return this
    }

    /**
     * Set display listener.
     */
    fun setDisplayListener(listener: OnDisplayListener?): TipTool {
        tipView.setDisplayListener(listener)
        return this
    }

    /**
     * Set hide listener.
     */
    fun setHideListener(listener: OnHideListener?): TipTool {
        tipView.setHideListener(listener)
        return this
    }

    /**
     * Set the distance between the anchor View and the surrounding area.
     */
    fun setAnchorMargins(start: Int, top: Int, end: Int, bottom: Int): TipTool {
        tipView.apply {
            marginStart = start
            marginTop = top
            marginEnd = end
            marginBottom = bottom
        }
        return this
    }

    /**
     * Set distance between bubble and the rectF
     */
    fun setBubblePadding(start: Int, top: Int, end: Int, bottom: Int): TipTool {
        tipView.apply {
            contentPaddingTop = top
            contentPaddingBottom = bottom
            contentPaddingStart = start
            contentPaddingEnd = end
        }
        return this
    }

    /**
     * Set the radius of corner.
     */
    fun setBubbleRadius(corner: Float): TipTool {
        tipView.setBubbleRadius(corner)
        return this
    }

    /**
     * Set click to hide.
     */
    fun setClickToHide(clickToHide: Boolean): TipTool {
        tipView.setClickToHide(clickToHide)
        return this
    }

    /**
     * Set auto hide, after duration time, the tip will dismiss.
     */
    fun setAutoHide(autoHide: Boolean, duration: Long): TipTool {
        tipView.apply {
            setAutoHide(autoHide)
            setDuration(duration)
        }
        return this
    }

    /**
     * Set click outside to disappear bubble.
     * @param isClickOutsideToDisappear true, click outside to disappear, false, otherwise.
     */
    fun setClickOutsideToDisappear(isClickOutsideToDisappear: Boolean = true): TipTool {
        popupWindow?.isOutsideTouchable = isClickOutsideToDisappear
        return this
    }

    /**
     * Set bubble shadow spread, this value should not be too large.
     */
    fun setShadowSpread(spread: Float = 0f): TipTool {
        tipView.shadowSpreadScope = spread
        return this
    }

    /**
     * Set the distance between bubble and screen border.
     */
    fun setDistanceToScreenEdge(distance: Int): TipTool {
        tipView.marginBorder = distance
        return this
    }

    /**
     * Set text for default bubble textview.
     */
    fun setText(text: CharSequence?): TipTool {
        if (tipView.defaultTipView is TextView) {
            (tipView.defaultTipView as TextView).text = text
        }
        return this
    }

    /**
     * Set text size  for default bubble textview.
     */
    fun setTextSize(size: Float): TipTool {
        if (tipView.defaultTipView is TextView) {
            (tipView.defaultTipView as TextView).textSize = size
        }
        return this
    }

    /**
     * Set text appearance  for default bubble textview.
     */
    fun setTextAppearance(@StyleRes textAppearance: Int): TipTool {
        if (tipView.defaultTipView is TextView) {
            TextViewCompat.setTextAppearance((tipView.defaultTipView as TextView), textAppearance)
        }
        return this
    }

    /**
     * Set stroke color for default bubble textview.
     */
    fun setStrokeColor(color: Int = 0): TipTool {
        tipView.setStrokeColor(color)
        return this
    }

    /**
     * Start to show bubble.
     */
    fun show(): TipTool {
        val activityContext = tipView.context
        if (activityContext is Activity) {
            val decorView = activityContext.window.decorView as ViewGroup
            anchor.post {
                val anchorRect = Rect()
                anchor.getGlobalVisibleRect(anchorRect)
                val location = IntArray(2)
                anchor.getLocationOnScreen(location)
//                println("zxc0 anchorRect.left = ${anchorRect.left} , anchorRect.top = ${anchorRect.top} , anchorRect.right = ${anchorRect.right} , anchorRect.bottom = ${anchorRect.bottom}")
                anchorRect.left = location[0]
//                println("zxc1 anchorRect.left = ${anchorRect.left} , anchorRect.top = ${anchorRect.top} , anchorRect.right = ${anchorRect.right} , anchorRect.bottom = ${anchorRect.bottom}")
                if (anchor.rootView is ViewGroup) {
                    (anchor.rootView as ViewGroup).addView(tipView, WRAP_CONTENT, WRAP_CONTENT)
                } else {
                    decorView.addView(tipView, WRAP_CONTENT, WRAP_CONTENT)
                }
                tipView.viewTreeObserver.addOnPreDrawListener(object :
                    ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        tipView.show(anchorRect)
                        tipView.viewTreeObserver.removeOnPreDrawListener(this)
//                        println("zxc3 anchorRect.left = ${anchorRect.left} , anchorRect.top = ${anchorRect.top} , anchorRect.right = ${anchorRect.right} , anchorRect.bottom = ${anchorRect.bottom}")
                        return false
                    }
                })
            }
        }
        return this
    }

    /**
     * Dismiss the tip view.
     */
    fun dismiss(): TipTool {
        tipView.dismiss()
        popupWindow?.apply {
            dismiss()
            setOnDismissListener(null)
        }
        return this
    }

    @Deprecated("Use setClickOutsideToDisappear instead.")
    fun setShadowInterceptEvent(intercept: Boolean = false): TipTool {
        popupWindow?.isOutsideTouchable = !intercept
        return this
    }

    /**
     * Set hide or show animation.
     */
    @Deprecated("This feature is not been support since 20240104")
    fun setAnimation(tooltipAnimation: TipAnimation): TipTool {
        tipView.setAnimation(tooltipAnimation)
        return this
    }

    private fun getActivityContext(context: Context): Activity {
        var ctx: Context? = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return ViewHelper.getActivity(context)!!
    }

    /**
     * This is the implementation of tip view.Its outside is a rectangle, and the bubble with arrows
     * are drawn in this interval.
     */
    internal inner class TipView(context: Context) : FrameLayout(context) {
        /**
         * Margins for container. this will decide the distance between anchor view and bubble.
         */
        internal var marginStart = 0
        internal var marginTop = 0
        internal var marginEnd = 0
        internal var marginBottom = 0

        /**
         * Padding for anchor view.This decide the distance between bubble and rectangle.
         */
        internal var contentPaddingStart = 12f.dpInt(context)
        internal var contentPaddingTop = 8f.dpInt(context)
        internal var contentPaddingEnd = 12f.dpInt(context)
        internal var contentPaddingBottom = 8f.dpInt(context)

        /**
         * If this value less than or equal to -1f， the arrow is in center.
         * This value present how long the distance between start drawing point to arrow.
         */
        internal var arrowOffset = -1f

        /**
         * Spread scope of shadow.
         */
        internal var shadowSpreadScope = 0f

        /**
         * Control the minimum distance from the border to the edge of the screen.
         */
        internal var marginBorder = 16.dpInt()

        /**
         * Attribute to draw bubble.
         */
        private var bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)

        private var bubbleColor = ContextCompat.getColor(context, R.color.BasePrimary)
        private var bubblePath: Path? = null

        /**
         * This is the bubble we see on the screen.
         */
        internal var defaultTipView: View
        private var position = Position.BOTTOM
        private var alignment = Alignment.CENTER

        /**
         * True, click bubble to hide bubble,false otherwise.
         */
        private var clickToHide = false
        private var autoHide = false
        private var duration = DEFAULT_TIP_SHOW_DURATION.toLong()
        private var displayListener: OnDisplayListener? = null
        private var onHideListener: OnHideListener? = null
        private var tooltipAnimation: TipAnimation = FadeTipAnimation()
        private var radius = 8f.dp2pxFloat(context)
        private var anchorViewRect: Rect? = null

        /**
         * Anchor view.
         */
        private var anchorView: View? = null

        private var extraTranslationX = 0
        private var isPositive = false

        private val distanceBetweenBubbleAndAnchor = 4f.dpInt(context)

        init {
            setWillNotDraw(false)
            // Set tip view.
            defaultTipView = LayoutInflater.from(context)
                .inflate(R.layout.layout_tips_popup_window, this, false)
            (defaultTipView as TextView).apply {
                setPadding(0, 0, 0, 0)
                maxWidth = context.resources.getScreenWidth() - 56.dpInt(context)
                addView(this, WRAP_CONTENT, WRAP_CONTENT)
            }
            bubblePaint.apply {
                color = bubbleColor
                style = Paint.Style.FILL
            }
            // 设置描边画笔颜色为透明
            strokePaint.apply {
                style = Paint.Style.STROKE
                strokeWidth = 0.5f.dp2pxFloat(context)
                color = ContextCompat.getColor(context, R.color.transparent)
            }
            // dismiss this tip view.
            ViewCompat.addOnUnhandledKeyEventListener(this) { _, event ->
                if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss()
                }
                false
            }
        }

        fun setCustomView(customView: View) {
            removeView(defaultTipView)
            defaultTipView = customView
            addView(defaultTipView, WRAP_CONTENT, WRAP_CONTENT)
        }

        fun setBubbleBackgroundColor(color: Int) {
            bubbleColor = color
            bubblePaint.color = color
            defaultTipView.setBackgroundColor(color)
            postInvalidate()
        }

        fun setAlignment(alignment: Alignment) {
            this.alignment = alignment
            postInvalidate()
        }

        fun setPosition(position: Position) {
            this.position = position
            when (position) {
                Position.START -> setPaddingRelative(
                    contentPaddingStart,
                    contentPaddingTop,
                    contentPaddingEnd + ARROW_HEIGHT,
                    contentPaddingBottom
                )

                Position.TOP -> setPaddingRelative(
                    contentPaddingStart,
                    contentPaddingTop,
                    contentPaddingEnd,
                    contentPaddingBottom + ARROW_HEIGHT
                )

                Position.END -> setPaddingRelative(
                    contentPaddingStart + ARROW_HEIGHT,
                    contentPaddingTop,
                    contentPaddingEnd,
                    contentPaddingBottom
                )

                Position.BOTTOM -> setPaddingRelative(
                    contentPaddingStart,
                    contentPaddingTop + ARROW_HEIGHT,
                    contentPaddingEnd,
                    contentPaddingBottom
                )
            }
            postInvalidate()
        }

        fun setAnchorView(anchorView: View?) {
            this.anchorView = anchorView
        }

        fun setClickToHide(clickToHide: Boolean) {
            this.clickToHide = clickToHide
        }

        fun setBubbleRadius(corner: Float) {
            radius = corner
        }

        fun setDisplayListener(listener: OnDisplayListener?) {
            displayListener = listener
        }

        fun setHideListener(listener: OnHideListener?) {
            onHideListener = listener
        }

        fun setAnimation(animation: TipAnimation) {
            tooltipAnimation = animation
        }

        fun remove() {
            (parent as? ViewGroup)?.removeView(this@TipView)
//            startExitAnimation(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    super.onAnimationEnd(animation)
//                    /*(parent as? ViewGroup)?.removeView(this@TipView)*/
//                }
//            })
        }

        fun setDuration(duration: Long) {
            this.duration = duration
        }

        fun setAutoHide(autoHide: Boolean) {
            this.autoHide = autoHide
        }

        fun show(adjustableRect: Rect) {
            anchorViewRect = Rect(adjustableRect)
            adjustBubbleOutRectSizeAndTranslationX(adjustableRect)
            translationBubble(adjustableRect)
            realShow()
        }

        fun dismiss() {
            remove()
            anchorView = null
        }

        fun setStrokeColor(color: Int) {
            strokePaint.color = color
            invalidate()
        }

        override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(width, height, oldw, oldh)
            bubblePath = drawBubble(
                RectF(0f, 0f, width.toFloat(), height.toFloat()), radius, radius, radius, radius
            )
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (bubblePath != null) {
                canvas.drawPath(bubblePath!!, bubblePaint)
                // 使用描边画笔在画布上绘制气泡形状的边框
                canvas.drawPath(bubblePath!!, strokePaint)
            }
        }

        private fun startEnterAnimation() {
            tooltipAnimation.onAnimateEnter(this, object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    displayListener?.onDisplay(this@TipView)
                }
            })
        }

        private fun startExitAnimation(animatorListener: Animator.AnimatorListener) {
            tooltipAnimation.onAnimateExit(this, object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    animatorListener.onAnimationEnd(animation)
                    onHideListener?.onHide(this@TipView)
                }
            })
        }

        private fun handleAutoRemove() {
            if (clickToHide) {
                setOnClickListener {
                    remove()
                }
            }
            if (autoHide) {
                postDelayed({ remove() }, duration)
            }
        }

        /**
         * We must adjust its size to prevent its width ow height beyond screen width.
         * 到目前为止，我们调整的都是外边框的大小，因为带箭头的是绘制在这个边框内部的。
         */
        private fun adjustBubbleOutRectSizeAndTranslationX(adjustableRect: Rect) {
            val tipViewLp = layoutParams
            val screenWidth = context.resources.getScreenWidth()
            if (position == Position.START || position == Position.END) {
                if (context.isRtl()) {
                    if (position == Position.START && adjustableRect.right + width > screenWidth) {
                        tipViewLp.width =
                            screenWidth - adjustableRect.right - marginBorder - distanceBetweenBubbleAndAnchor
                    } else if (position == Position.END && width > adjustableRect.left) {
                        tipViewLp.width =
                            adjustableRect.left - marginBorder - distanceBetweenBubbleAndAnchor
                    }
                } else {
                    if (position == Position.START && width > adjustableRect.left) {
                        tipViewLp.width =
                            adjustableRect.left - marginBorder - distanceBetweenBubbleAndAnchor
                    } else if (position == Position.END && adjustableRect.right + width > screenWidth) {
                        tipViewLp.width =
                            screenWidth - adjustableRect.right - marginBorder - distanceBetweenBubbleAndAnchor
                    }
                }
            } else if (position == Position.TOP || position == Position.BOTTOM) {
                // The logic below is to calculate when placing the bubble on top of bottom, its left or right should not beyond screen boundary.
                // But this logic seems will never enter because the limit of before logic.
                val remainWidth = (width - adjustableRect.width()) / 2
                // Gives a default horizontal displacement for vertically placed bubbles.
                if (adjustableRect.right + remainWidth > screenWidth) {
                    isPositive = false
                    extraTranslationX =
                        adjustableRect.right + remainWidth - screenWidth + marginBorder
                } else if (adjustableRect.left - remainWidth < 0) {
                    isPositive = true
                    extraTranslationX =
                        remainWidth - adjustableRect.left + marginBorder
                }
            }
            layoutParams = tipViewLp
        }

        /**
         * Translate bubble to make sure it shows in valid areas, exclude status bar and navigation bar.
         * Before this you should know that we have add this tip view in the root view. decor view or the root view of bottom sheet.
         * So we only need translation of the bubble, that is enough.
         *
         * @param adjustableRect The position of anchor.
         */
        private fun translationBubble(adjustableRect: Rect) {
//            println("zxc2 anchorRect.left = " + adjustableRect.left + ", anchorRect.top = " + adjustableRect.top + ", anchorRect.right = " + adjustableRect.right + ", anchorRect.bottom = " + adjustableRect.bottom)
            if (position == Position.START || position == Position.END) {
                var realYOffset = 0
                val yCoordinate = getCenterCoordinates(anchorView)[1]
                // 说明显示区域超过了状态栏的高度，需要做偏移
                val anchorCenterToStatusBarDistance = yCoordinate - context.getStatusBarHeight()
                // 说明显示区域低于了导航栏位置，需要做偏移
                val anchorCenterToNaviDistance =
                    context.getRealScreenHeight() - yCoordinate - context.getNavigationBarHeight()
                if (height / 2 > anchorCenterToStatusBarDistance) {
                    arrowOffset = anchorCenterToStatusBarDistance + ARROW_HEIGHT / 2f
                    realYOffset = height / 2 - (yCoordinate - context.getStatusBarHeight())
                } else if (anchorCenterToNaviDistance < height / 2) { // 如果显示区域低于了导航栏
                    arrowOffset = height - anchorCenterToNaviDistance + ARROW_HEIGHT / 2f
                    realYOffset = -(height / 2 - anchorCenterToNaviDistance)
                }
                // This calculate will help us to align tip tool with anchor.
                val anchorHeight = adjustableRect.height()
                val maxHeight = anchorHeight.coerceAtLeast(height)
                val minHeight = anchorHeight.coerceAtMost(height)
                var spacingY = 0
                when (alignment) {
                    Alignment.END -> spacingY = maxHeight - minHeight
                    Alignment.CENTER -> spacingY = -1 * maxHeight / 2 + minHeight / 2
                    Alignment.START -> {}
                }
                translationX = if (position == Position.START) {
                    if (context.isRtl()) {
                        -(resources.getScreenWidth() - adjustableRect.right - width - distanceBetweenBubbleAndAnchor - marginStart - shadowSpreadScope)
                    } else {
                        val offset: Float =
                            (adjustableRect.left - width - distanceBetweenBubbleAndAnchor - marginStart).toFloat()
                        if (offset < marginBorder) {
                            marginBorder.toFloat()
                        } else {
                            offset
                        }
                    }
                } else {
                    if (context.isRtl()) {
                        -(resources.getScreenWidth() - adjustableRect.left + distanceBetweenBubbleAndAnchor + marginEnd + shadowSpreadScope)
                    } else {
                        (adjustableRect.right + distanceBetweenBubbleAndAnchor + marginEnd).toFloat()
                    }
                }
                translationY = (adjustableRect.top + spacingY + realYOffset).toFloat()
//                println("zxc Start or End bubbleHeight = $height, anchorHeight = $anchorHeight, maxHeight = $maxHeight, minHeight = $minHeight, translationX = $translationX, translationY = $translationY")
            } else {
                // This calculate will help us to align tip tool with anchor.
                var spacingX = 0
                if (alignment == Alignment.CENTER) {
                    spacingX = (adjustableRect.width() / 2 - width / 2)
                }
                translationY = if (position == Position.TOP) {
                    (adjustableRect.top - height - distanceBetweenBubbleAndAnchor - marginTop).toFloat()
                } else {
                    (adjustableRect.bottom + distanceBetweenBubbleAndAnchor + marginBottom).toFloat()
                }
                translationX = if (context.isRtl()) {
                    -(resources.getScreenWidth() - adjustableRect.left - adjustableRect.width() / 2 - width / 2 - if (isPositive) extraTranslationX else -extraTranslationX).toFloat()
                } else {
                    (adjustableRect.left + spacingX).toFloat() + if (isPositive) extraTranslationX else -extraTranslationX
                }
//                println("zxc Top or Bottom bubbleHeight = $height, spacingX = $spacingX, isPositive = $isPositive, extraTranslationX = $extraTranslationX, translationX = $translationX, translationY = $translationY")
            }
        }

        private fun getCenterCoordinates(view: View?): IntArray {
            val centerX = view!!.width / 2
            val centerY = view.height / 2
            val locationOnScreen = IntArray(2)
            view.getLocationOnScreen(locationOnScreen)
            val centerXOnScreen = locationOnScreen[0] + centerX
            val centerYOnScreen = locationOnScreen[1] + centerY
            return intArrayOf(centerXOnScreen, centerYOnScreen)
        }

        private fun realShow() {
            // This logic is to help us to adjust arrow position.
            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                if (position == Position.START) {
                    position = Position.END
                } else if (position == Position.END) {
                    position = Position.START
                }
            }
            // 接着，画气泡。
            bubblePath = drawBubble(
                RectF(0f, 0f, width.toFloat(), height.toFloat()), radius, radius, radius, radius
            )
            startEnterAnimation()
            handleAutoRemove()
            // 在popup window 中显示整个tip view.
            viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    viewTreeObserver.removeOnPreDrawListener(this)
                    val outLocation = IntArray(2)
                    tipView.getLocationOnScreen(outLocation)
                    val x = outLocation[0]
                    val y = outLocation[1]
                    // After we add shadow ,the bubble position will be changed. so we need use translation.
                    // method to adjust the position of tip view.
                    tipView.apply {
                        translationX = x - shadowSpreadScope
                        translationY = y - shadowSpreadScope
                    }
                    // Move tipView out of its current parent View
                    val parent = tipView.parent as? ViewGroup
                    parent?.removeView(tipView)
                    shadowLayout?.apply {
                        addView(tipView, WRAP_CONTENT, WRAP_CONTENT)
                        shadowColor = Color.parseColor("#0D000000")
                        shadowSpread = shadowSpreadScope
                    }
                    tipView.apply {
                        this.layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                            gravity = Gravity.TOP or Gravity.START
                        }
                        translationX = 0f
                        translationY = 0f
                    }

                    // Initialize the PopupWindow
                    popupWindow?.apply {
                        isFocusable = false
                        width = tipView.measuredWidth + shadowSpreadScope.toInt() * 2
                        // Show the PopupWindow
                        showAtLocation(
                            anchor,
                            Gravity.NO_GRAVITY,
                            x - shadowSpreadScope.toInt(),
                            y - shadowSpreadScope.toInt()
                        )
                        setOnDismissListener {
                            tipView.dismiss()
                            onHideListener?.onHide(this@TipView)
                        }
                    }
                    return false
                }
            })
        }

        /**
         * The drawing process starts from the top left and draws a clockwise circle,
         * the arrow is in the center position without configuration offset
         *
         * @param rectF             Bubble scope
         * @param topLeftRadius     Top left rounded corner size
         * @param topRightRadius    Top right rounded corner size
         * @param bottomLeftRadius  Bottom left rounded corner size
         * @param bottomRightRadius Bottom right rounded corner size
         * @return Bubble.
         */
        private fun drawBubble(
            rectF: RectF,
            topLeftRadius: Float,
            topRightRadius: Float,
            bottomLeftRadius: Float,
            bottomRightRadius: Float
        ): Path {
            // Since we did a divide by 2 operation on radius in the subsequent operation, we need to multiply by 2 here.
            var lTR = topLeftRadius * 2
            var tRR = topRightRadius * 2
            var bRR = bottomRightRadius * 2
            var bLR = bottomLeftRadius * 2
            val path = Path()
            if (anchorViewRect == null) {
                return path
            }
            lTR = if (lTR < 0f) 0f else lTR
            tRR = if (tRR < 0f) 0f else tRR
            bLR = if (bLR < 0f) 0f else bLR
            bRR = if (bRR < 0f) 0f else bRR
            val spacingLeft = if (position == Position.END) ARROW_HEIGHT.toFloat() else 0f
            val spacingTop = if (position == Position.BOTTOM) ARROW_HEIGHT.toFloat() else 0f
            val spacingRight = if (position == Position.START) ARROW_HEIGHT.toFloat() else 0f
            val spacingBottom = if (position == Position.TOP) ARROW_HEIGHT.toFloat() else 0f

            val left = rectF.left + spacingLeft
            val top = rectF.top + spacingTop
            val right = rectF.right - spacingRight
            val bottom = rectF.bottom - spacingBottom

            // 存储一下原始值，因为 arrowOffset 会在下边的绘制流程中被赋值。
            val originalOffset = arrowOffset
            // Start for left, draw top side
            path.moveTo(left + lTR / 2f, top)
            if (position == Position.BOTTOM) {
                if (originalOffset == -1f) {
                    arrowOffset = anchorViewRect!!.centerX() - x
                }
                // 水平放置箭头的时候，算出来的arrowStartX似乎是箭头右侧的位置。
                val arrowStartX = left + arrowOffset - ARROW_WIDTH / 2
                path.apply {
                    lineTo(arrowStartX - ARROW_WIDTH / 2, top)
                    lineTo(arrowStartX + ARROW_WIDTH / 2, rectF.top)
                    lineTo(arrowStartX + ARROW_WIDTH / 2 + ARROW_WIDTH, top)
                }
            }
            // Start from right, draw right side.
            path.apply {
                lineTo(right - tRR / 2f, top)
                quadTo(right, top, right, top + tRR / 2)
            }
            if (position == Position.START) {
                // 垂直放置箭头的时候，算出来的arrowStartX似乎是箭头居中的位置。
                if (originalOffset == -1f) {
                    path.apply {
                        lineTo(right, bottom / 2f - ARROW_WIDTH)
                        lineTo(rectF.right, bottom / 2f)
                        lineTo(right, bottom / 2f + ARROW_WIDTH)
                    }
                } else {
                    val arrowTop = top + arrowOffset - ARROW_WIDTH / 2 - shadowSpreadScope
                    path.apply {
                        lineTo(right, arrowTop - ARROW_WIDTH)
                        lineTo(rectF.right, arrowTop)
                        lineTo(right, arrowTop + ARROW_WIDTH)
                    }
                }
            }
            // Start from right, draw bottom side.
            path.apply {
                lineTo(right, bottom - bRR / 2)
                quadTo(right, bottom, right - bRR / 2, bottom)
            }
            if (position == Position.TOP) {
                if (originalOffset == -1f) {
                    arrowOffset = anchorViewRect!!.centerX() - x
                }
                val arrowStartX = left + arrowOffset + ARROW_WIDTH / 2
                // Calculate if the arrow center is in the center of the line.
                path.apply {
                    lineTo(arrowStartX + ARROW_WIDTH / 2, bottom)
                    lineTo(arrowStartX - ARROW_WIDTH / 2, rectF.bottom)
                    lineTo(arrowStartX - ARROW_WIDTH / 2 - ARROW_WIDTH, bottom)
                }
            }
            // Start from bottom, draw left side.
            path.apply {
                lineTo(left + bLR / 2, bottom)
                quadTo(left, bottom, left, bottom - bLR / 2)
            }
            if (position == Position.END) {
                if (originalOffset == -1f) {
                    path.apply {
                        lineTo(left, bottom / 2f + ARROW_WIDTH)
                        lineTo(rectF.left, bottom / 2f)
                        lineTo(left, bottom / 2f - ARROW_WIDTH)
                    }
                } else {
                    val arrowTop = top + arrowOffset - ARROW_WIDTH / 2 - shadowSpreadScope
                    path.apply {
                        lineTo(left, arrowTop - ARROW_WIDTH)
                        lineTo(rectF.left, arrowTop)
                        lineTo(left, arrowTop + ARROW_WIDTH)
                    }
                }
            }
            path.apply {
                lineTo(left, top + lTR / 2)
                quadTo(left, top, left + lTR / 2, top)
                close()
            }
            return path
        }
    }

    internal inner class FadeTipAnimation(private val fadeDuration: Long = 0) : TipAnimation {
        override fun onAnimateEnter(view: View, animatorListener: Animator.AnimatorListener?) {
            view.alpha = 0f
            view.animate().alpha(1f).setDuration(fadeDuration).setListener(animatorListener)
        }

        override fun onAnimateExit(view: View, animatorListener: Animator.AnimatorListener?) {
            view.animate().alpha(0f).setDuration(fadeDuration).setListener(animatorListener)
        }
    }

    interface TipAnimation {
        fun onAnimateEnter(view: View, animatorListener: Animator.AnimatorListener?)
        fun onAnimateExit(view: View, animatorListener: Animator.AnimatorListener?)
    }

    /**
     * 显示监听
     */
    interface OnDisplayListener {
        fun onDisplay(view: View?)
    }

    /**
     * 隐藏监听
     */
    interface OnHideListener {
        fun onHide(view: View?)
    }
}