package com.xingchaozhang.androidui.utils

import android.graphics.Rect
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo.TouchDelegateInfo


/**
 * Multiple view touch delegate in same ViewGroup.
 *
 */
class MultiViewTouchDelegate(delegateView: View) : TouchDelegate(DEFAULT_RECT, delegateView) {

    companion object {
        private val DEFAULT_RECT = Rect()
    }


    private val touchDelegates: ArrayList<TouchDelegate> by lazy {
        arrayListOf()
    }

    private var currentTouchDelegate: TouchDelegate? = null

    fun addTouchDelegate(touchDelegate: TouchDelegate) {
        touchDelegates.add(touchDelegate)
    }

    fun removeTouchDelegate(touchDelegate: TouchDelegate) {
        touchDelegates.remove(touchDelegate)
    }

    fun clearTouchDelegates() {
        touchDelegates.clear()
        currentTouchDelegate = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var delegate: TouchDelegate? = null
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                for (touchDelegate in touchDelegates) {
                    if (touchDelegate.onTouchEvent(event)) {
                        currentTouchDelegate = touchDelegate
                        return true
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                currentTouchDelegate = null
            }
            MotionEvent.ACTION_UP -> {
                delegate = currentTouchDelegate
                currentTouchDelegate = null
            }
            else -> {}
        }
        return delegate?.onTouchEvent(event) ?: false
    }

    override fun onTouchExplorationHoverEvent(event: MotionEvent): Boolean {
        return currentTouchDelegate?.run {
            onTouchExplorationHoverEvent(event)
        } ?: super.onTouchExplorationHoverEvent(event)
    }

    override fun getTouchDelegateInfo(): TouchDelegateInfo {
        return currentTouchDelegate?.run {
            touchDelegateInfo
        } ?: super.getTouchDelegateInfo()
    }
}