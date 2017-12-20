package net.gegy1000.terrarium.server.map.source

import java.util.LinkedList

object LoadingStateHandler {
    private const val STATE_LIFETIME = 8000

    private val stateBuffer = LinkedList<StateEntry>()
    private val lock = Object()

    fun putState(state: LoadingState) = synchronized(lock) {
        stateBuffer.add(StateEntry(state, System.currentTimeMillis()))
    }

    fun checkState(): LoadingState? {
        removeExpired()
        return synchronized(lock) {
            stateBuffer.associate { (state, _) ->
                Pair(state, stateBuffer.count { state == it.state })
            }.maxBy { it.value }?.key
        }
    }

    private fun removeExpired() = synchronized(lock) {
        stateBuffer.removeIf(StateEntry::expired)
    }

    private class StateEntry(val state: LoadingState, val time: Long) {
        val expired: Boolean
            get() = System.currentTimeMillis() - time > STATE_LIFETIME

        operator fun component1() = state

        operator fun component2() = time
    }
}
