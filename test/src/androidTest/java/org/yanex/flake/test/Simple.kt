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

    override fun createHolder(manager: FlakeManager): Holder {
        val view = TextView(manager.activity)
        return Holder(view.apply { id = ID })
    }

    override fun setup(h: Holder, manager: FlakeManager) {
        h.textView.text = TEXT
    }

    class Holder(root: View) : AbstractFlakeHolder(root) {
        val textView = root as TextView
    }
}