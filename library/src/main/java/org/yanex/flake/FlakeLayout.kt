package org.yanex.flake

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import org.yanex.flake.internal.FlakeManagerImpl

class FlakeLayout : FrameLayout {
    internal var manager: FlakeManagerImpl? = null
    internal var animationRunning = false
    private var previouslyDetached = false

    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs, 0)
    constructor(ctx: Context, attrs: AttributeSet, attr: Int) : super(ctx, attrs, attr)

    fun addFlakeView(view: View) = insertFlakeView(view, -1)

    fun insertFlakeView(view: View, index: Int) {
        val layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        addView(view, index, layoutParams)
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (manager == null) {
            throw IllegalStateException("FlakeManager is not set for this FlakeLayout")
        }

        super.addView(child, index, params)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val manager = this.manager
        if (manager != null && previouslyDetached) {
            manager.onLayoutBecameReattachedToWindow()
        }
    }

    override fun onDetachedFromWindow() {
        previouslyDetached = true
        super.onDetachedFromWindow()
        manager?.onLayoutBecameDetachedFromWindow()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (animationRunning) return true
        return super.onInterceptTouchEvent(ev)
    }

    override fun onInterceptHoverEvent(event: MotionEvent?): Boolean {
        if (animationRunning) return true
        return super.onInterceptHoverEvent(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (animationRunning) return true
        return super.dispatchKeyEvent(event)
    }
}
