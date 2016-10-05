package org.yanex.flaketest.flakes

import android.view.View
import android.widget.Button
import android.widget.EditText
import org.yanex.flake.FlakeManager
import org.yanex.flake.IdHolder
import org.yanex.flake.XmlFlake
import org.yanex.flaketest.R

abstract class AbstractNewPersonFlake : XmlFlake<AbstractNewPersonFlake.Holder>(), MyFlakeAnimation {
    override fun createHolder(manager: FlakeManager, root: View) = Holder(root)

    override fun init(h: Holder, manager: FlakeManager) {
        h.ok.setOnClickListener {
            manager.goBack(Person(h.familyNameString, h.firstNameString, h.ageInt))
        }
        h.cancel.setOnClickListener {
            manager.goBack()
        }

        h.showAllFields?.setOnClickListener {
            manager.replace(DetailedNewPersonFlake(h.firstNameString, h.familyNameString))
        }
    }

    class Holder(root: View): IdHolder(root) {
        val familyName: EditText by id(R.id.familyName)
        val firstName: EditText by id(R.id.firstName)
        val age: EditText? by opt(R.id.age)

        val familyNameString: String get() = familyName.text.toString()
        val firstNameString: String get() = firstName.text.toString()
        val ageInt: Int? get() = age?.text?.toString()?.toIntOpt()

        val showAllFields: Button? by opt(R.id.allFields)
        val ok: Button by id(R.id.ok)
        val cancel: Button by id(R.id.cancel)

        private fun String.toIntOpt(): Int? {
            return try {
                toInt()
            } catch (e: NumberFormatException) { null }
        }
    }
}

class SimpleNewPersonFlake : AbstractNewPersonFlake() {
    override val layoutResource = R.layout.flake_new_person
}

class DetailedNewPersonFlake(
        private val firstName: String,
        private val familyName: String
) : AbstractNewPersonFlake() {
    override val layoutResource = R.layout.flake_new_person_detailed

    override fun init(h: AbstractNewPersonFlake.Holder, manager: FlakeManager) {
        super.init(h, manager)
        h.familyName.setText(familyName)
        h.firstName.setText(firstName)
    }
}
