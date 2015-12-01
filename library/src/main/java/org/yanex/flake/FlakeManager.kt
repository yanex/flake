package org.yanex.flake

import android.app.Activity
import android.view.View
import org.yanex.flake.internal.FlakeManagerImpl
import org.yanex.flake.internal.FlakeState
import org.yanex.flake.internal.RetainedState

enum class FlakeRetentionPolicy {
    ALWAYS_RETAIN, SOFT_REFERENCE, DO_NOT_RETAIN
}

abstract class FlakeManager internal constructor() {
    abstract val flakeLayout: FlakeLayout
    abstract val flakeContext: FlakeContext
    abstract val activity: Activity

    var saveStateAfterDetach: Boolean = true
    var retentionPolicy: FlakeRetentionPolicy = FlakeRetentionPolicy.ALWAYS_RETAIN
    var retainPreviousFlake: Boolean = false
        set(v) {
            if (stack.isNotEmpty() || previous != null) {
                throw IllegalStateException("FlakeManager has not-empty stack")
            }
            field = v
        }

    val hasFlakes: Boolean
        get() = current != null

    val stackSize: Int
        get() = stack.size + (if (retainPreviousFlake && previous != null) 1 else 0)

    val canGoBack: Boolean
        get() = previous != null || stack.isNotEmpty()

    val activeFlake: Flake<*>?
        get() = current?.flake

    val activeFlakeRootView: View?
        get() = current?.holder?.root

    abstract fun restoreState(): Boolean

    fun restoreStateOrShow(factory: () -> Flake<*>) {
        if (!restoreState()) show(factory())
    }

    fun goBack(): Unit = goBack(null)

    abstract fun <T : FlakeHolder> show(flake: Flake<T>)
    abstract fun <T : FlakeHolder> replace(flake: Flake<T>)
    abstract fun goBack(result: Any?)

    abstract fun removeAllFlakes()

    abstract fun onBackPressed(): Boolean

    protected abstract val stack: MutableList<FlakeState>
    protected abstract var current: RetainedState?
    protected abstract var previous: RetainedState?

    companion object {
        @JvmStatic
        fun create(flakeLayout: FlakeLayout, flakeContext: FlakeContext): FlakeManager {
            return FlakeManagerImpl(flakeLayout, flakeContext)
        }
    }
}