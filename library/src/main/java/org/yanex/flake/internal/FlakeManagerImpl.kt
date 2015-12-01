package org.yanex.flake.internal

import android.app.Activity
import android.view.View
import android.view.animation.Animation
import org.yanex.flake.*
import java.lang.ref.SoftReference
import java.util.*

internal class FlakeManagerImpl(
        override val flakeLayout: FlakeLayout,
        flakeContext: FlakeContext
) : FlakeManager() {
    private val flakeContextImpl: FlakeContextImpl = flakeContext as? FlakeContextImpl
            ?: throw IllegalArgumentException("'flakeContext' must be instance of FlakeContextImpl")

    override val flakeContext: FlakeContext
        get() = flakeContextImpl

    override val activity: Activity = flakeLayout.context as? Activity
            ?: throw RuntimeException("FlakeLayout context must be instance of be an Activity")

    protected override val stack: MutableList<FlakeState> = arrayListOf()
    protected override var current: RetainedState? = null
    protected override var previous: RetainedState? = null

    private val taskQueue = linkedListOf<StackedTask>()
    private var flakeStackLock = false
    private var currentAnimation: Animation? = null

    init {
        flakeLayout.manager = this
        flakeContextImpl.register(this)
    }

    override fun restoreState(): Boolean {
        checkLayoutIdPresent()
        val state = flakeContextImpl.getSavedManagerState(flakeLayout.id) ?: return false
        restoreState(state)
        return true
    }

    override fun <T : FlakeHolder> show(flake: Flake<T>) = underStackLock({ ShowFlakeTask(flake) }) {
        endCurrentAnimationIfRunning()

        val oldCurrent = current
        val oldUnderCurrent = previous

        val stacked = if (retainPreviousFlake) {
            val stacked = oldUnderCurrent
            previous = oldCurrent
            stacked
        } else {
            oldCurrent
        }

        if (stacked != null) {
            stacked.flake.onDetach()
            stack.add(stacked.cachedFlakeState())
        }

        flake.onAttach(this)
        val newCurrent = initFlake(flake)

        current = newCurrent

        updateLayoutAfterShow(oldUnderCurrent, oldCurrent, newCurrent)
    }

    override fun goBack(result: Any?) = underStackLock({ GoBackTask(result) }) {
        endCurrentAnimationIfRunning()

        val oldCurrent = current ?: return

        val stack = this.stack
        val retainFlakeUnderCurrent = this.retainPreviousFlake

        var newUnderCurrent: RetainedState? = null
        val newCurrent = if (retainFlakeUnderCurrent) {
            val newCurrent = previous
            if (newCurrent == null) {
                previous = null
                current = null
                flakeLayout.removeAllViews()
                return
            }

            newUnderCurrent = stack.removeLast()?.ensureRetained()
            newUnderCurrent?.flake?.onAttach(this)
            previous = newUnderCurrent
            newCurrent
        } else {
            stack.removeLast()?.ensureRetained()
        }

        newCurrent?.flake?.onAttach(this)
        current = newCurrent

        if (newCurrent != null) {
            @Suppress("UNCHECKED_CAST")
            val flake = newCurrent.flake as Flake<FlakeHolder>
            flake.update(newCurrent.holder, this, result)
        }

        updateLayoutAfterGoBack(newUnderCurrent, oldCurrent, newCurrent)
    }

    override fun <T : FlakeHolder> replace(flake: Flake<T>) = underStackLock({ ReplaceFlakeTask(flake) }) {
        endCurrentAnimationIfRunning()

        current = initFlake(flake)
        updateLayout()
    }

    override fun removeAllFlakes() {
        endCurrentAnimationIfRunning()

        stack.clear()

        previous?.let { it.flake.onDetach() }
        previous = null

        current?.let { it.flake.onDetach() }
        current = null

        flakeLayout.removeAllViews()
    }

    override fun onBackPressed(): Boolean {
        return current?.flake?.onBackPressed(this) ?: false
    }

    internal fun saveState(): List<Flake<*>> {
        endCurrentAnimationIfRunning()

        checkLayoutIdPresent()

        val s = stack
        val current = this.current
        val previous = this.previous

        val allFlakes = ArrayList<Flake<*>>(s.size + previous.oneIfNotNull() + current.oneIfNotNull())
        s.forEachByIndex { allFlakes.add(it.flake) }
        previous?.let { allFlakes.add(it.flake) }
        current?.let { allFlakes.add(it.flake) }
        return allFlakes
    }

    internal val internalGetActiveFlakeState: RetainedState?
        get() = current

    internal fun onLayoutBecameDetachedFromWindow() {
        endCurrentAnimationIfRunning()

        if (!saveStateAfterDetach) {
            removeAllFlakes()
        } else {
            val s = stack
            this.current?.cachedFlakeState()?.let {
                it.flake.onDetach()
                s.add(it)
            }
            this.previous?.cachedFlakeState()?.let {
                it.flake.onDetach()
                s.add(it)
            }
            current = null
            previous = null
        }
    }

    internal fun onLayoutBecameReattachedToWindow() = restoreState(stack)

    private fun restoreState(state: List<FlakeState>) {
        val newStack = state.toArrayList()
        val newCurrent = newStack.removeLast()?.ensureRetained()
        val newUnderCurrent = if (retainPreviousFlake) newStack.removeLast()?.ensureRetained() else null

        if (newUnderCurrent != null) {
            newUnderCurrent.flake.onAttach(this)
            previous = newUnderCurrent
        }

        newCurrent?.flake?.onAttach(this)
        current = newCurrent

        stack.clear()
        stack.addAll(newStack)
        updateLayout()
    }

    private fun updateLayoutAfterGoBack(
            newUnderCurrent: RetainedState?,
            oldCurrent: RetainedState,
            newCurrent: RetainedState?
    ) {
        val oldCurrentFlake = oldCurrent.flake
        if (oldCurrentFlake !is AnimatedFlake || newCurrent == null
                || !retainPreviousFlake || taskQueue.isNotEmpty()) {
            updateLayout()
            return
        }

        val oldCurrentView = oldCurrent.holder.root
        val newCurrentView = newCurrent.holder.root

        val oldCurrentAnimation = oldCurrentFlake.getAnimationOnHide(this)
        if (oldCurrentAnimation == null) {
            updateLayout()
            return
        }
        val newCurrentAnimation = oldCurrentFlake.getAnimationOnHideForPrevious(this)

        if (newUnderCurrent != null) {
            val newUnderCurrentView = newUnderCurrent.holder.root
            newUnderCurrentView.visibility = View.INVISIBLE
            flakeLayout.insertFlakeView(newUnderCurrentView, 0)
        }

        newCurrentView.visibility = View.VISIBLE

        oldCurrentAnimation.setAnimationListener(GoBackAnimationListener(oldCurrentView))
        currentAnimation = oldCurrentAnimation
        flakeLayout.animationRunning = true
        oldCurrentView.startAnimation(oldCurrentAnimation)

        if (newCurrentAnimation != null) {
            newCurrentAnimation.fillAfter = false
            newCurrentView.startAnimation(newCurrentAnimation)
        }
    }

    private fun updateLayoutAfterShow(
            oldUnderCurrent: RetainedState?,
            oldCurrent: RetainedState?,
            newCurrent: RetainedState
    ) {
        val currentFlake = newCurrent.flake
        if (currentFlake !is AnimatedFlake || oldCurrent == null
                || !retainPreviousFlake || taskQueue.isNotEmpty()) {
            updateLayout()
            return
        }

        if (oldUnderCurrent != null) {
            flakeLayout.removeView(oldUnderCurrent.holder.root)
        }

        flakeLayout.addFlakeView(newCurrent.holder.root)

        val oldCurrentView = oldCurrent.holder.root
        val newCurrentView = newCurrent.holder.root

        val newCurrentAnimation = currentFlake.getAnimationOnShow(this)
        if (newCurrentAnimation == null) {
            updateLayout()
            return
        }
        val oldCurrentAnimation = currentFlake.getAnimationOnShowForPrevious(this)

        newCurrentAnimation.fillAfter = false
        newCurrentAnimation.setAnimationListener(HideViewAnimationListener(oldCurrentView))

        newCurrentView.visibility = View.VISIBLE
        currentAnimation = newCurrentAnimation
        flakeLayout.animationRunning = true
        newCurrentView.startAnimation(newCurrentAnimation)

        if (oldCurrentAnimation != null) {
            oldCurrentView.startAnimation(oldCurrentAnimation)
        }
    }

    private fun updateLayout() {
        val layout = flakeLayout
        layout.removeAllViews()

        fun show(state: RetainedState?, visibility: Int) {
            if (state == null) return
            val v = state.holder.root
            v.visibility = visibility
            layout.addFlakeView(v)
        }

        show(previous, View.INVISIBLE)
        show(current, View.VISIBLE)
    }

    private fun <T : FlakeHolder> initFlake(flake: Flake<T>): RetainedState {
        return RetainedState(flake, flake.init(this))
    }

    private fun RetainedState.cachedFlakeState(): FlakeState {
        return when (retentionPolicy) {
            FlakeRetentionPolicy.ALWAYS_RETAIN -> this
            FlakeRetentionPolicy.SOFT_REFERENCE ->
                SoftReferenceState(flake, SoftReference(holder))
            FlakeRetentionPolicy.DO_NOT_RETAIN -> FlakeState(flake)
        }
    }

    private fun FlakeState.ensureRetained(): RetainedState {
        return when (this) {
            is RetainedState -> this
            is SoftReferenceState -> {
                val holder = holder.get()
                if (holder != null) RetainedState(flake, holder) else initFlake(flake)
            }
            else -> initFlake(flake)
        }
    }

    private fun endCurrentAnimationIfRunning() {
        currentAnimation?.cancel()
    }

    private fun checkLayoutIdPresent() {
        if (flakeLayout.id == View.NO_ID) {
            throw IllegalArgumentException("FlakeLayout must have a unique identifier")
        }
    }

    private inline fun underStackLock(taskFactory: () -> StackedTask, job: () -> Unit) {
        if (flakeStackLock) {
            taskQueue.add(taskFactory())
            return
        }

        flakeStackLock = true
        job()
        flakeStackLock = false

        if (taskQueue.isNotEmpty()) {
            taskQueue.removeFirst().run(this)
        }
    }

    private interface StackedTask {
        fun run(manager: FlakeManager)
    }

    private class ShowFlakeTask(val flake: Flake<*>) : StackedTask {
        override fun run(manager: FlakeManager) = manager.show(flake)
    }

    private class ReplaceFlakeTask(val flake: Flake<*>) : StackedTask {
        override fun run(manager: FlakeManager) = manager.replace(flake)
    }

    private class GoBackTask(val result: Any?) : StackedTask {
        override fun run(manager: FlakeManager) = manager.goBack(result)
    }

    private inner class HideViewAnimationListener(private val oldView: View) : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation?) {
            oldView.visibility = View.INVISIBLE
            flakeLayout.animationRunning = false
            currentAnimation = null
        }

        override fun onAnimationRepeat(animation: Animation?) {}
        override fun onAnimationStart(animation: Animation?) {}
    }

    private inner class GoBackAnimationListener(private val oldView: View) : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation?) {
            flakeLayout.removeView(oldView)
            flakeLayout.animationRunning = false
            currentAnimation = null
        }

        override fun onAnimationRepeat(animation: Animation?) {}
        override fun onAnimationStart(animation: Animation?) {}
    }

}