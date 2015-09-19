package org.yanex.flake

import android.content.res.Configuration
import android.view.View
import android.view.animation.Animation

interface AnimatedFlake {
    open fun getAnimationOnShow(manager: FlakeManager): Animation? = null
    open fun getAnimationOnShowForPrevious(manager: FlakeManager): Animation? = null

    open fun getAnimationOnHide(manager: FlakeManager): Animation? = null
    open fun getAnimationOnHideForPrevious(manager: FlakeManager): Animation? = null

    //TODO
    open fun getAnimationOnReplace(manager: FlakeManager): Animation? = null
    open fun getAnimationOnReplaceForPrevious(manager: FlakeManager): Animation? = null
}

abstract class Flake<T: FlakeHolder> {
    protected abstract fun createView(manager: FlakeManager): View
    protected abstract fun createHolder(root: View): T

    open fun setup(h: T, manager: FlakeManager) {}
    open fun update(h: T, manager: FlakeManager, result: Any?) {}

    open fun messageReceived(h: T, manager: FlakeManager, message: Any) {}

    open fun onAttach(manager: FlakeManager) {}
    open fun onDetach() {}

    open fun onConfigurationChanged(h: T, manager: FlakeManager, newConfig: Configuration) {}

    open fun onBackPressed(manager: FlakeManager): Boolean {
        if (!manager.canGoBack) return false
        manager.goBack()
        return true
    }

    internal fun init(manager: FlakeManager): T {
        val view = createView(manager)
        val holder = createHolder(view)
        setup(holder, manager)
        return holder
    }
}