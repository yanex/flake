package org.yanex.flake.test

import android.app.Activity
import android.test.ActivityInstrumentationTestCase2
import org.yanex.flake.FlakeActivity
import org.yanex.flake.FlakeContext
import org.yanex.flake.FlakeLayout
import org.yanex.flake.FlakeManager

abstract class FlakeTestCase : ActivityInstrumentationTestCase2<FlakeActivity>(FlakeActivity::class.java)

fun createFlakeManager(activity: Activity): FlakeManager {
    val flakeLayout = FlakeLayout(activity).apply { activity.setContentView(this) }
    val flakeContext = FlakeContext.create(activity, null)
    return FlakeManager.create(flakeLayout, flakeContext)
}

fun <T: Activity> ActivityInstrumentationTestCase2<T>.test(f: (T) -> Unit) {
    setActivityInitialTouchMode(true)
    val act = activity
    instrumentation.runOnMainSync {
        f(act)
    }
}