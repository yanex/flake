package org.yanex.flake

import android.content.res.Configuration
import android.view.View
import android.view.animation.Animation

interface AnimatedFlake {
    fun getAnimationOnShow(manager: FlakeManager): Animation? = null
    fun getAnimationOnShowForPrevious(manager: FlakeManager): Animation? = null

    fun getAnimationOnHide(manager: FlakeManager): Animation? = null
    fun getAnimationOnHideForPrevious(manager: FlakeManager): Animation? = null

    //TODO
    fun getAnimationOnReplace(manager: FlakeManager): Animation? = null
    fun getAnimationOnReplaceForPrevious(manager: FlakeManager): Animation? = null
}

interface FlakeBase<T : FlakeHolder> {
    /**
     * [init] is called each time the flake holder is created.
     */
    fun init(h: T, manager: FlakeManager) {}

    /**
     * [setup] is called each time before the flake become shown.
     */
    fun setup(h: T, manager: FlakeManager) {}

    /**
     * [update] is called instead of [init] if the holder is already created for this flake.
     */
    fun update(h: T, manager: FlakeManager, result: Any?) {}

    fun messageReceived(h: T, manager: FlakeManager, message: Any) {}

    fun onAttach(manager: FlakeManager) {}
    fun onDetach(manager: FlakeManager) {}

    fun onConfigurationChanged(h: T, manager: FlakeManager, newConfig: Configuration) {}
}

abstract class Flake<T: FlakeHolder> : FlakeBase<T> {
    protected abstract fun createHolder(manager: FlakeManager): T

    open fun onBackPressed(manager: FlakeManager): Boolean {
        if (!manager.canGoBack) return false
        manager.goBack()
        return true
    }

    internal fun init(manager: FlakeManager): T {
        val holder = createHolder(manager)
        init(holder, manager)
        setup(holder, manager)
        return holder
    }
}