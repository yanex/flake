package org.yanex.flake.test

import android.view.View
import android.widget.TextView
import org.yanex.flake.*

class SimpleTest : FlakeTestCase() {
    fun testSimple() = test { activity ->
        val manager = createFlakeManager(activity)
        manager.show(SimpleFlake())

        val textView = activity.findViewById(SimpleFlake.ID) as? TextView
        assertNotNull(textView)
        assertEquals(textView, manager.activeFlakeRootView)
        assertEquals(SimpleFlake.TEXT, textView!!.text.toString())
    }
}

class SimpleFlake : Flake<SimpleFlake.Holder>() {
    companion object {
        val ID = 1001
        val TEXT = "test"
    }

    override fun createView(manager: FlakeManager) = TextView(manager.activity).apply { id = ID }
    override fun createHolder(manager: FlakeManager, root: View) = Holder(root)

    override fun setup(h: Holder, manager: FlakeManager) {
        h.textView.text = TEXT
    }

    class Holder(root: View) : FlakeHolder(root) {
        val textView = root as TextView
    }
}