package com.setoskins.thermal.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ConfigWriter {
    private const val DEBOUNCE_MS = 280L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()

    private val pending = LinkedHashMap<String, String>()
    private val lastWritten = HashMap<String, String>()
    private var flushJob: Job? = null
    private var lastSubmitNanos = 0L

    internal var writer: suspend (Map<String, String>) -> Boolean = { ModuleDetector.updateConfigs(it) }

    fun submit(key: String, value: String) = submitAll(mapOf(key to value))

    fun submitAll(values: Map<String, String>) {
        if (values.isEmpty()) return
        synchronized(lock) {
            var dirty = false
            for ((key, value) in values) {
                if (pending[key] == value) continue
                if (pending[key] == null && lastWritten[key] == value) continue
                pending[key] = value
                dirty = true
            }
            if (!dirty) return
            lastSubmitNanos = System.nanoTime()
            if (flushJob == null) {
                flushJob = scope.launch { flushLoop() }
            }
        }
    }

    private suspend fun flushLoop() {
        while (true) {
            val waitNanos: Long
            synchronized(lock) {
                if (pending.isEmpty()) {
                    flushJob = null
                    return
                }
                waitNanos = DEBOUNCE_MS * 1_000_000L - (System.nanoTime() - lastSubmitNanos)
            }
            if (waitNanos > 0) delay(waitNanos / 1_000_000L + 1)
            val batch: Map<String, String>
            synchronized(lock) {
                if (pending.isEmpty()) {
                    flushJob = null
                    return
                }
                batch = LinkedHashMap(pending)
                pending.clear()
            }
            val written = writer(batch)
            if (written) {
                synchronized(lock) { lastWritten.putAll(batch) }
            }
        }
    }

    fun invalidate() {
        synchronized(lock) { lastWritten.clear() }
    }

    internal fun resetForTest() {
        synchronized(lock) {
            flushJob?.cancel()
            flushJob = null
            pending.clear()
            lastWritten.clear()
            lastSubmitNanos = 0L
        }
    }
}
