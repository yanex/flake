package org.yanex.flake

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle

open class FlakeActivity : Activity() {
    private var internalFlakeContext: FlakeContext? = null

    protected val flakeContext: FlakeContext
        get() = internalFlakeContext!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val flakeContext = FlakeContext.create(this, savedInstanceState)

        flakeContext.messageListener = { messageReceived(it) }
        this.internalFlakeContext = flakeContext
    }

    open fun messageReceived(message: Any) {}

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        flakeContext.onConfigurationChanged(newConfig)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        flakeContext.saveInstanceState(outState)
    }
}