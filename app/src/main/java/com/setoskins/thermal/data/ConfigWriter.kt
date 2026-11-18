package com.setoskins.thermal.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 配置文件写入节流器。
 *
 * 模块配置原本是「每次数值变化就拉起一次 su + sed」：拖动一个温度滑块会连续产生几十次
 * root 进程，既卡 UI 又费电。这里把短时间内的多次写入合并成一次 su 调用，并跳过与上次
 * 已写入内容相同的值。
 *
 * 用法：界面状态立即更新，落盘走 [submit] / [submitAll]，最多延迟 [DEBOUNCE_MS]。
 */
object ConfigWriter {

    private const val DEBOUNCE_MS = 280L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()

    private val pending = LinkedHashMap<String, String>()
    private val lastWritten = HashMap<String, String>()
    private var flushJob: Job? = null
    private var lastSubmitNanos = 0L

    /** 实际落盘实现，便于单元测试替换。 */
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

    /**
     * 等「最后一次输入之后」再落盘：拖动滑块期间不会产生任何 su 调用，
     * 松手 [DEBOUNCE_MS] 后合并成一次写入。
     */
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

    /** 丢弃去重缓存（例如模块配置被外部改写后需要强制重写）。 */
    fun invalidate() {
        synchronized(lock) { lastWritten.clear() }
    }

    /** 仅用于单元测试：取消挂起的写入并清空内部状态。 */
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
