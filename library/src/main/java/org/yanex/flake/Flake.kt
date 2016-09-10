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
    fun setup(h: T, manager: FlakeManager) {}
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
        setup(holder, manager)
        return holder
    }
}