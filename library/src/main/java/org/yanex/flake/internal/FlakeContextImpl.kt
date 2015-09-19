package org.yanex.flake.internal

import android.content.res.Configuration
import android.os.Bundle
import android.util.SparseArray
import org.yanex.flake.Flake
import org.yanex.flake.FlakeContext
import org.yanex.flake.FlakeHolder
import org.yanex.flake.FlakeManager
import java.lang.ref.WeakReference
import java.util.*

internal class FlakeContextImpl(savedInstanceState: Bundle?) : FlakeContext() {
    override var messageListener: ((Any) -> Unit)? = null

    private val managers = arrayListOf<WeakReference<FlakeManagerImpl>>() // Must not be private
    private var restoredState: SparseArray<List<Flake<*>>>? = null

    private val components = hashMapOf<Class<*>, Any>()

    init {
        val cacheKey = savedInstanceState?.getString(BUNDLE_KEY)
        if (cacheKey != null) {
            restoredState = Cache.get(cacheKey)
        }
    }

    override fun saveInstanceState(outState: Bundle?) {
        if (outState == null) return
        val managers = getAllManagers(this)
        val sa = SparseArray<List<Flake<*>>>(managers.size())
        managers.forEachByIndex { manager ->
            sa.put(manager.flakeLayout.id, manager.saveState())
        }
        outState.putString(BUNDLE_KEY, Cache.put(sa))
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        if (newConfig == null) return
        for (manager in getAllManagers(this)) {
            val state = manager.internalGetActiveFlakeState ?: continue
            @Suppress("UNCHECKED_CAST")
            val flake = state.flake as Flake<FlakeHolder>
            flake.onConfigurationChanged(state.holder, manager, newConfig)
        }
    }

    override fun <T : Any> useComponent(type: Class<T>, instance: T) {
        components[type] = instance
    }

    override fun <T : Any> getComponent(type: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return components[type] as? T
                ?: throw IllegalArgumentException("No instance was found for ${type.canonicalName}")
    }

    override fun <T : Flake<*>> sendMessage(flakeClass: Class<T>, message: Any): Boolean {
        val found = findFlake(this) { flakeClass.isInstance(it) } ?: return false
        return doSendMessage(found.first, found.second, message)
    }

    override fun sendMessageToContext(message: Any): Boolean {
        val listener = messageListener ?: return false
        listener(message)
        return true
    }

    override fun sendBroadcastMessage(message: Any) {
        for (manager in getAllManagers(this)) {
            val state = manager.internalGetActiveFlakeState ?: continue
            doSendMessage(manager, state, message)
        }
    }

    internal fun getSavedManagerState(layoutId: Int): List<FlakeState>? {
        val data = restoredState ?: return null
        val flakes = data.get(layoutId) ?: return null
        data.remove(layoutId)
        return flakes.map { FlakeState(it) }
    }

    internal fun register(manager: FlakeManagerImpl) {
        managers.add(WeakReference(manager))
    }

    private fun getAllManagers(me: FlakeContextImpl): List<FlakeManagerImpl> {
        val managers = arrayListOf<FlakeManagerImpl>()
        forAllManagers(me) {
            managers.add(it)
        }
        return managers
    }

    private fun findFlake(me: FlakeContextImpl, f: (Flake<*>) -> Boolean): Pair<FlakeManager, FlakeState>? {
        forAllManagers(me) { manager ->
            val state = manager.internalGetActiveFlakeState
            if (state != null) {
                val flake = state.flake
                if (f(flake)) return Pair(manager, state)
            }
        }
        return null
    }

    private fun doSendMessage(manager: FlakeManager, state: FlakeState, message: Any): Boolean {
        val retainedState = state as? RetainedState ?: return false
        @Suppress("UNCHECKED_CAST")
        val flake = retainedState.flake as Flake<FlakeHolder>
        flake.messageReceived(retainedState.holder, manager, message)
        return true
    }

    private inline fun forAllManagers(me: FlakeContextImpl, f: (FlakeManagerImpl) -> Unit) {
        val iterator = me.managers.iterator()
        for (ref in iterator) {
            val manager = ref.get()
            if (manager == null) {
                iterator.remove()
                continue
            }
            f(manager)
        }
    }

    private object Cache {
        private val cache = hashMapOf<String, SparseArray<List<Flake<*>>>>()

        public fun put(m: SparseArray<List<Flake<*>>>): String {
            val id = "FlakeContext." + UUID.randomUUID()
            cache.put(id, m)
            return id
        }

        public fun get(id: String): SparseArray<List<Flake<*>>>? {
            return cache.remove(id)
        }
    }

    private companion object {
        private val BUNDLE_KEY = FlakeContext::class.java.canonicalName + ":flakes"
    }

}