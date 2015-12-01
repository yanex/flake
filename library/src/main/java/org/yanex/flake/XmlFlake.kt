package org.yanex.flake

import android.view.View

abstract class XmlFlake<T: FlakeHolder>: Flake<T>() {
    abstract val layoutResource: Int

    final override fun createView(manager: FlakeManager): View {
        return manager.activity.layoutInflater.inflate(layoutResource, null)
    }
}