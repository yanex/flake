package org.yanex.flaketest.flakes

import android.view.View
import android.widget.ListView
import org.yanex.flake.FlakeLayout
import org.yanex.flake.FlakeManager
import org.yanex.flake.IdHolder
import org.yanex.flake.XmlFlake
import org.yanex.flake.internal.MyAdapter
import org.yanex.flaketest.R

class NestedFlake : XmlFlake<NestedFlake.Holder>() {
    override val layoutResource: Int = R.layout.flake_nested
    override fun createHolder(manager: FlakeManager, root: View) = Holder(root)

    override fun init(h: Holder, manager: FlakeManager) {
        val manager1 = FlakeManager.create(h.first, manager.flakeContext)
        val manager2 = FlakeManager.create(h.second, manager.flakeContext)
        manager1.restoreStateOrShow { LeftFlake() }
        manager2.restoreStateOrShow { RightFlake() }
    }

    class Holder(root: View) : IdHolder(root) {
        val first: FlakeLayout by id(R.id.first)
        val second: FlakeLayout by id(R.id.second)
    }
}

abstract class LeftRightFlake : XmlFlake<LeftRightFlake.Holder>() {
    private val items = (1..10).mapTo(arrayListOf<String>()) { "Item $it" }

    override val layoutResource = R.layout.flake_list_simple
    override fun createHolder(manager: FlakeManager, root: View) = Holder(root, items)

    override fun init(h: Holder, manager: FlakeManager) {
        h.list.adapter = h.adapter
        h.list.setOnItemClickListener { adapterView, view, pos, l ->
            items.removeAt(pos)
            h.adapter.notifyDataSetChanged()
            sendMessageToOtherFlake(manager, pos)
        }
    }

    abstract fun sendMessageToOtherFlake(manager: FlakeManager, pos: Int)

    override fun messageReceived(h: Holder, manager: FlakeManager, message: Any) {
        if (message is RemoveListItem) {
            items.removeAt(message.pos)
            h.adapter.notifyDataSetChanged()
        }
    }

    class Holder(root: View, items: List<String>) : IdHolder(root) {
        val list: ListView by id(R.id.list)
        val adapter = MyAdapter(root.context, android.R.layout.simple_list_item_1, items)
    }
}

class RemoveListItem(val pos: Int)

class LeftFlake: LeftRightFlake() {
    override fun sendMessageToOtherFlake(manager: FlakeManager, pos: Int) {
        manager.flakeContext.sendMessage<RightFlake>(RemoveListItem(pos))
    }
}

class RightFlake: LeftRightFlake() {
    override fun sendMessageToOtherFlake(manager: FlakeManager, pos: Int) {
        manager.flakeContext.sendMessage<LeftFlake>(RemoveListItem(pos))
    }
}