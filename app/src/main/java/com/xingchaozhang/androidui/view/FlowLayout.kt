package com.xingchaozhang.androidui.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IntDef
import com.xingchaozhang.androidui.R
import com.xingchaozhang.androidui.utils.dpInt

/**
 * @date : 2023/4/14 11:58
 * @author ： zhangxingchao
 * @description : Flowlayout.
 */
open class FlowLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    /**
     * To record the last index of second line.We may need to leave an empty space for the arrow
     */
    var secondLineLastIndex = 0

    /**
     * True expand, false otherwise.
     */
    var expand = true

    /**
     * When we set up the list directly, we need to update the entire View,
     * using this variable to limit the number of calls to measure all the time
     * true update layout, false otherwise.
     */
    var needUpdate = true

    /**
     * Use this to update list and restart measure.
     * Int : size of the list.
     */
    var onFirstTimeMeasureCallback: ((Int) -> Unit)? = null

    /**
     * Use this to update list and restart measure.
     * Int : size of the list.
     */
    var onMeasureCallback: ((Int) -> Unit)? = null

    /**
     * How may line should this layout showing.
     */
    var maxLines = 2

    /**
     * Add function class for each line.
     */
    private var line: Line? = null

    /**
     * 所有的子控件
     */
    private var viewArray: SparseArray<View?>? = null

    /**
     * 横向间隔
     */
    private var horizontalSpacing = 12f.dpInt(context)

    /**
     * 纵向间隔
     */
    private var verticalSpacing = 12f.dpInt(context)

    /**
     * 当前行已用的宽度，由子View宽度加上横向间隔
     */
    private var usedWidth = 0

    /**
     * width of pre used width.
     */
    private var preUsedWidth = 0

    /**
     * 代表每一行的集合
     */
    private val lineList: MutableList<Line?> = ArrayList()

    /**
     * 子View的对齐方式
     */
    private var isAlignByCenter = 1

    /**
     * 最大的行数
     */
    private val maxLinesCount = Int.MAX_VALUE

    /**
     * True ,is first time to layout, false ,no.
     */
    private var isFirstTime = true

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout)
        maxLines = typedArray.getInteger(R.styleable.FlowLayout_android_maxLines, 2)
        expand = typedArray.getBoolean(R.styleable.FlowLayout_flow_isExpand, true)
        horizontalSpacing = typedArray.getDimensionPixelSize(R.styleable.FlowLayout_flowHorizontalSpacing, 12f.dpInt(context))
        verticalSpacing = typedArray.getDimensionPixelSize(R.styleable.FlowLayout_flowVerticalSpacing, 12f.dpInt(context))
        typedArray.recycle()
    }

    fun setAlignByCenter(@AlienState.Val isAlignByCenter: Int) {
        this.isAlignByCenter = isAlignByCenter
        requestLayoutInner()
    }

    fun setAdapter(list: List<*>?, res: Int, mItemView: ItemView<*>) {
        if (list == null) {
            return
        }
        removeAllViews()
        setHorizontalSpacing(horizontalSpacing)
        setVerticalSpacing(verticalSpacing)
        val size = list.size
        for (i in 0 until size) {
            val item = list[i]!!
            val inflate = LayoutInflater.from(context).inflate(res, null)
            mItemView.getCover(item, ViewHolder(inflate), inflate, i)
            addView(inflate)
        }
    }

    fun setHorizontalSpacing(spacing: Int) {
        if (horizontalSpacing != spacing) {
            horizontalSpacing = spacing
            requestLayoutInner()
        }
    }

    fun setVerticalSpacing(spacing: Int) {
        if (verticalSpacing != spacing) {
            verticalSpacing = spacing
            requestLayoutInner()
        }
    }

    fun getTotalLine(): Int {
        return lineList.size
    }

    fun resetValue() {
        expand = false
        needUpdate = true
        isFirstTime = true
        secondLineLastIndex = 0
        onFirstTimeMeasureCallback = null
        onMeasureCallback = null
        line = null
        viewArray = SparseArray()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingRight - paddingLeft
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        // 还原数据，以便重新记录
        restoreLine()
        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            if (child.visibility == GONE) {
                continue
            }
            val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                sizeWidth, if (modeWidth == MeasureSpec.EXACTLY) MeasureSpec.AT_MOST else modeWidth
            )
            val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                sizeHeight,
                if (modeHeight == MeasureSpec.EXACTLY) MeasureSpec.AT_MOST else modeHeight
            )
            // 测量child
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            if (line == null) {
                line = Line()
            }
            val childWidth = child.measuredWidth
            // 增加使用的宽度
            preUsedWidth = usedWidth
            usedWidth += childWidth
            // 使用宽度小于总宽度，该child属于这一行，无需换行。
            if (usedWidth <= sizeWidth) {
                // 添加child
                line!!.addView(child)
                // 加上间隔
                usedWidth += horizontalSpacing
                // 加上间隔后如果大于等于总宽度，需要换行
                if (usedWidth >= sizeWidth) {
                    if (!newLine()) {
                        break
                    }
                }
                // This is because when we changed line, we start add line to lineList, so lineList size is 0 when we start.
                if (lineList.size == 1) {
                    secondLineLastIndex = i
                }
            } else {
                // 28f.dpInt(context), the width of the function image.
                if ((lineList.size == 1) && (preUsedWidth + 28f.dpInt(context)) < sizeWidth) {
                    secondLineLastIndex++
                }
                // 如果这行一个child都没有，即使占用长度超过了总长度，也要加上去，保证每行都有至少有一个child
                if (line!!.viewCount == 0) {
                    // 添加child
                    line!!.addView(child)
                    // 换行
                    if (!newLine()) {
                        break
                    }
                } else { // 如果该行有数据了，就直接换行
                    // 换行
                    if (!newLine()) {
                        break
                    }
                    // 在新的一行，不管是否超过长度，先加上去，因为这一行一个child都没有，所以必须满足每行至少有一个child
                    line!!.addView(child)
                    usedWidth += childWidth + horizontalSpacing
                }
            }
        }
        if (line != null && (line!!.viewCount > 0) && !lineList.contains(line)) {
            // 由于前面采用判断长度是否超过最大宽度来决定是否换行，则最后一行可能因为还没达到最大宽度，所以需要验证后加入集合中
            lineList.add(line)
        }
        /**
         * To update layout when we enter in the first time.
         */
        if (isFirstTime) {
            onFirstTimeMeasureCallback?.invoke(lineList.size)
            isFirstTime = false
        } else {
            // Only in the contracted state do we need to do the list adjustment.
            // In the rest of cases, the whole list is refreshed directly without hiding.
            if (needUpdate) {
                onMeasureCallback?.invoke(lineList.size)
                needUpdate = false
            }
        }
        val totalWidth = MeasureSpec.getSize(widthMeasureSpec)
        var totalHeight = 0
        val linesCount = lineList.size
        for (i in 0 until linesCount) { // 加上所有行的高度
            totalHeight += lineList[i]!!.height
        }
        totalHeight += verticalSpacing * (linesCount - 1) // 加上所有间隔的高度
        totalHeight += paddingTop + paddingBottom // 加上padding
        // 设置布局的宽高，宽度直接采用父view传递过来的最大宽度，而不用考虑子view是否填满宽度，因为该布局的特性就是填满一行后，再换行
        // 高度根据设置的模式来决定采用所有子View的高度之和还是采用父view传递过来的高度
        setMeasuredDimension(totalWidth, resolveSize(totalHeight, heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val isRtl = layoutDirection == LAYOUT_DIRECTION_RTL
        val initialLeft = if (isRtl) width - paddingRight else paddingLeft
        var top = paddingTop
        val count = if (lineList.size > maxLines && !expand) maxLines else lineList.size
        for (i in 0 until count) {
            val line = lineList[i]
            line!!.layoutView(initialLeft, top, isRtl)
            top += line.height + verticalSpacing
        }
    }

    private fun requestLayoutInner() {
        Handler(Looper.getMainLooper()).post { requestLayout() }
    }

    private fun restoreLine() {
        lineList.clear()
        line = Line()
        usedWidth = 0
    }

    /**
     * 新增加一行
     */
    private fun newLine(): Boolean {
        lineList.add(line)
        if (lineList.size < maxLinesCount) {
            line = Line()
            usedWidth = 0
            return true
        }
        return false
    }

    interface AlienState {
        @IntDef(value = [RIGHT, LEFT, CENTER])
        annotation class Val
        companion object {
            const val RIGHT = 0
            const val LEFT = 1
            const val CENTER = 2
        }
    }

    abstract class ItemView<T> {
        abstract fun getCover(item: Any, holder: ViewHolder?, inflate: View?, position: Int)
    }

    inner class ViewHolder(var convertView: View) {
        init {
            viewArray = SparseArray()
        }

        fun <T : View?> getView(viewId: Int): T? {
            var view = viewArray!![viewId]
            if (view == null) {
                view = convertView.findViewById(viewId)
                viewArray!!.put(viewId, view)
            }
            try {
                return view as T?
            } catch (e: ClassCastException) {
                e.printStackTrace()
            }
            return null
        }

        fun setText(viewId: Int, text: String?) {
            val view = getView<TextView>(viewId)!!
            view.text = text
        }
    }

    inner class Line {
        /**
         * 该行中所有的子View累加的宽度
         */
        var width = 0

        /**
         * 该行中所有的子View中高度的那个子View的高度
         */
        var height = 0

        /**
         * 每一行所有的子View都存储在这个list中
         */
        var viewList: MutableList<View> = ArrayList()

        /**
         * 获取一行中所有需要参与布局的子View的数量
         */
        val viewCount: Int
            get() = viewList.size

        /**
         * 往该行中添加一个
         */
        fun addView(view: View) {
            viewList.add(view)
            width += view.measuredWidth
            val childHeight = view.measuredHeight
            // 高度等于一行中最高的View
            height = if (height < childHeight) childHeight else height
        }

        /**
         * 摆放行中子View的位置
         */
        fun layoutView(initialLeft: Int, t: Int, isRtl: Boolean) {
            var left = initialLeft
            val count = viewCount
            val layoutWidth = measuredWidth - paddingLeft - paddingRight
            val surplusWidth = layoutWidth - width - horizontalSpacing * (count - 1)

            if (surplusWidth >= 0) {
                for (i in 0 until count) {
                    val view = viewList[i]
                    val childWidth = view.measuredWidth
                    val childHeight = view.measuredHeight
                    var topOffset = ((height - childHeight) / 2.0 + 0.5).toInt()
                    if (topOffset < 0) {
                        topOffset = 0
                    }

                    if (i == 0) {
                        when (isAlignByCenter) {
                            AlienState.CENTER -> left += surplusWidth / 2
                            AlienState.RIGHT -> if (isRtl) left -= surplusWidth else left += surplusWidth
                            else -> {}  // do nothing for LEFT
                        }
                    }

                    if (isRtl) {
                        view.layout(
                            left - childWidth,
                            t + topOffset,
                            left,
                            t + topOffset + childHeight
                        )
                        left -= (childWidth + verticalSpacing)
                    } else {
                        view.layout(
                            left,
                            t + topOffset,
                            left + childWidth,
                            t + topOffset + childHeight
                        )
                        left += (childWidth + verticalSpacing)
                    }
                }
            }
        }
    }
}