package org.yanex.flake

import android.view.View

abstract class XmlFlake<T: FlakeHolder>: Flake<T>() {
    abstract val layoutResource: Int

    final override fun createHolder(manager: FlakeManager): T {
        val view = manager.activity.layoutInflater.inflate(layoutResource, null)
        return createHolder(manager, view)
    }

    protected abstract fun createHolder(manager: FlakeManager, root: View): T
}