package com.setoskins.thermal.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Collections

class ConfigWriterTest {
    private val writes = Collections.synchronizedList(mutableListOf<Map<String, String>>())

    @Before
    fun setUp() {
        ConfigWriter.resetForTest()
        writes.clear()
        ConfigWriter.writer = { batch ->
            writes += batch
            true
        }
    }

    @After
    fun tearDown() {
        ConfigWriter.resetForTest()
    }

    @Test
    fun `连续提交只落盘一次且保留最后一个值`() = runBlocking {
        repeat(30) { ConfigWriter.submit("全局旁路到达温度", it.toString()) }

        awaitWrites(1)
        delay(500)

        assertEquals(1, writes.size)
        assertEquals("29", writes[0]["全局旁路到达温度"])
    }

    @Test
    fun `多键合并为一次写入`() = runBlocking {
        repeat(10) {
            ConfigWriter.submitAll(
                mapOf(
                    "全局旁路到达温度" to it.toString(),
                    "全局旁路恢复温度" to (it + 1).toString(),
                )
            )
        }

        awaitWrites(1)
        delay(500)

        assertEquals(1, writes.size)
        assertEquals("9", writes[0]["全局旁路到达温度"])
        assertEquals("10", writes[0]["全局旁路恢复温度"])
    }

    @Test
    fun `相同数值不会重复写入`() = runBlocking {
        ConfigWriter.submit("一限温度阈值", "40")
        awaitWrites(1)
        delay(500)

        ConfigWriter.submit("一限温度阈值", "40")
        delay(600)

        assertEquals(1, writes.size)
    }

    @Test
    fun `写入完成后的新数值会再次触发写入`() = runBlocking {
        ConfigWriter.submit("一限温度阈值", "40")
        awaitWrites(1)
        delay(500)

        ConfigWriter.submit("一限温度阈值", "41")
        awaitWrites(2)

        assertEquals(2, writes.size)
        assertEquals("41", writes[1]["一限温度阈值"])
    }

    private suspend fun awaitWrites(expected: Int) {
        withTimeout(5_000) {
            while (writes.size < expected) delay(10)
        }
        assertTrue("expected at least $expected writes, got ${writes.size}", writes.size >= expected)
    }
}
