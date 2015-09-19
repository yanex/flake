package org.yanex.flake

import android.view.View

public abstract class XmlFlake<T: FlakeHolder>: Flake<T>() {
    public abstract val layoutResource: Int

    final override fun createView(manager: FlakeManager): View {
        return manager.activity.layoutInflater.inflate(layoutResource, null)
    }
}