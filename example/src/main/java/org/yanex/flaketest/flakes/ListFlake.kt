package org.yanex.flaketest.flakes

import android.view.View
import android.widget.Button
import android.widget.ListView
import org.yanex.flake.FlakeManager
import org.yanex.flake.IdHolder
import org.yanex.flake.XmlFlake
import org.yanex.flake.internal.MyAdapter
import org.yanex.flaketest.R

public class Person(val familyName: String, val firstName: String, val age: Int? = null) {
    override fun toString() = "$firstName $familyName" + (if (age != null) " ($age)" else "")
}

public object ClearListMessage

public class ListFlake : XmlFlake<ListFlake.Holder>(), MyFlakeAnimation {
    private val items = arrayListOf(Person("Smith", "John", 30))

    override val layoutResource = R.layout.flake_list
    override fun createHolder(root: View) = Holder(root, items)

    override fun setup(h: Holder, manager: FlakeManager) {
        h.list.adapter = h.adapter
        h.nested.setOnClickListener { manager.show(NestedFlake()) }
        h.add.setOnClickListener { manager.show(SimpleNewPersonFlake()) }
        h.clearList.setOnClickListener { manager.show(ClearListFlake()) }
    }

    override fun update(h: Holder, manager: FlakeManager, result: Any?) {
        when (result) {
            is Person -> h.adapter.add(result)
            is ClearListMessage -> h.adapter.clear()
        }
    }

    private class Holder(root: View, items: List<Person>): IdHolder(root) {
        val list: ListView by id(R.id.list)
        val add: Button by id(R.id.add)
        val clearList: Button by id(R.id.clearList)
        val nested: Button by id(R.id.nested)

        val adapter = MyAdapter(root.context, android.R.layout.simple_list_item_1, items)
    }
}
