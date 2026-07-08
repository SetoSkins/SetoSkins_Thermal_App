package com.setoskins.thermal.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit
import android.util.Log

/**
 * 根权限及模块探测工具。
 */
object ModuleDetector {

    private const val TAG = "SetoSkins_ModuleDetector"
    private const val MODULE_PATH = "/data/adb/modules/SetoSkins"
    private const val CONFIG_PATH = "/data/adb/modules/SetoSkins/配置.prop"
    private const val MODULE_PROP_PATH = "/data/adb/modules/SetoSkins/module.prop"
    private const val LOG_PATH = "/data/adb/modules/SetoSkins/log.log"
    private const val THERMAL_SCRIPT_PATH = "/data/adb/modules/SetoSkins/system/Seto_fuckthermal.sh"
    private const val UPDATE_URL = "https://raw.githubusercontent.com/SetoSkins/SetoSkins_Thermal/refs/heads/master/SetoSkins.json"
    private const val TIMEOUT_MS = 1500L

    /**
     * 检测设备是否有 root 权限。
     */
    suspend fun requestRoot(): Boolean = withContext(Dispatchers.IO) {
        val process = try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        } catch (e: Exception) {
            return@withContext false
        }
        val finished = try {
            process.waitFor(8000, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            false
        }
        if (!finished) {
            process.destroyForcibly()
            return@withContext false
        }
        val output = try {
            process.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
        process.exitValue() == 0 && output.contains("uid=0")
    }

    /**
     * 检测模块是否已安装。
     */
    suspend fun isModuleInstalled(): Boolean = withContext(Dispatchers.IO) {
        val process = try {
            Runtime.getRuntime().exec(
                arrayOf("su", "-c", "[ -e $MODULE_PATH ] && echo 1 || echo 0")
            )
        } catch (e: Exception) {
            return@withContext false
        }
        val result = withTimeoutOrNull(TIMEOUT_MS) {
            try {
                val output = process.inputStream.bufferedReader().use { it.readText().trim() }
                val exit = process.waitFor()
                exit == 0 && output == "1"
            } catch (e: Exception) {
                false
            }
        }
        if (result == null) process.destroyForcibly()
        result ?: false
    }

    suspend fun updateConfig(key: String, value: Any): Unit = withContext(Dispatchers.IO) {
        runCatching {
            val strValue = value.toString()
            val command = "sed -i 's/^$key=.*/$key=$strValue/g' '$CONFIG_PATH'"
            Runtime.getRuntime().exec(arrayOf("su", "-c", command)).waitFor()
        }
    }

    /**
     * 执行温控脚本，在每次开关变更时触发。
     */
    suspend fun executeThermalScript(): Unit = withContext(Dispatchers.IO) {
        runCatching {
            val command = "sh '$THERMAL_SCRIPT_PATH'"
            Runtime.getRuntime().exec(arrayOf("su", "-c", command)).waitFor()
        }
    }

    suspend fun readConfig(): Map<String, String> = withContext(Dispatchers.IO) {
        runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat '$CONFIG_PATH'"))
            val lines = process.inputStream.bufferedReader().readLines()
            val configMap = mutableMapOf<String, String>()
            lines.forEach { line ->
                if (line.contains("=")) {
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) configMap[parts[0].trim()] = parts[1].trim()
                }
            }
            configMap
        }.getOrDefault(emptyMap())
    }

    suspend fun resetConfig(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val cmd1 = "sed -i '/^[^#]/ s/=.*/=false/' '$CONFIG_PATH'"
            val keysToKeepTrue = listOf("开启充电Log", "关闭录制温控", "关闭相机温控", "加快部分游戏启动速度")
            val cmd2 = keysToKeepTrue.joinToString(" && ") { "sed -i 's/^$it=.*/$it=true/g' '$CONFIG_PATH'" }
            Runtime.getRuntime().exec(arrayOf("su", "-c", "$cmd1 && $cmd2")).waitFor() == 0
        }.getOrDefault(false)
    }

    suspend fun importConfigFile(tempSourcePath: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val command = "cp -f '$tempSourcePath' '$CONFIG_PATH' && chmod 644 '$CONFIG_PATH'"
            Runtime.getRuntime().exec(arrayOf("su", "-c", command)).waitFor() == 0
        }.getOrDefault(false)
    }

    suspend fun readConfigRaw(): String = withContext(Dispatchers.IO) {
        runCatching {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "cat '$CONFIG_PATH'")).inputStream.bufferedReader().use { it.readText() }
        }.getOrDefault("")
    }

    suspend fun readLog(): String = withContext(Dispatchers.IO) {
        runCatching {
            val output = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat '$LOG_PATH'")).inputStream.bufferedReader().use { it.readText() }
            output.ifEmpty { "日志文件为空" }
        }.getOrDefault("无法读取日志文件")
    }

    suspend fun clearLog(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "sed -i '5,\$d' '$LOG_PATH'")).waitFor() == 0
        }.getOrDefault(false)
    }

    suspend fun getModuleVersion(): String = withContext(Dispatchers.IO) {
        runCatching {
            val lines = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat '$MODULE_PROP_PATH'")).inputStream.bufferedReader().readLines()
            lines.firstOrNull { it.startsWith("version=") }?.split("=")?.getOrNull(1)?.trim() ?: "未知版本"
        }.getOrDefault("未知版本")
    }

    suspend fun getLocalVersionCode(): Int = withContext(Dispatchers.IO) {
        runCatching {
            val lines = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat '$MODULE_PROP_PATH'")).inputStream.bufferedReader().readLines()
            lines.firstOrNull { it.startsWith("versionCode=") }?.split("=")?.getOrNull(1)?.trim()?.toInt() ?: 0
        }.getOrDefault(0)
    }

    suspend fun checkUpdate(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val localVersion = getLocalVersionCode()
            val connection = java.net.URL(UPDATE_URL).openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 5000
            val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
            val remoteVersionMatch = "\"versionCode\"\\s*:\\s*(\\d+)".toRegex().find(jsonText)
            val remoteVersion = remoteVersionMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            remoteVersion > localVersion
        }.getOrDefault(false)
    }

    data class BatteryInfo(val temperature: String = "", val current: String = "", val capacity: String = "", val status: String = "")

    suspend fun readBatteryInfo(): BatteryInfo = withContext(Dispatchers.IO) {
        runCatching {
            val script = "echo temp=\$(cat /sys/class/power_supply/battery/temp 2>/dev/null); echo current=\$(cat /sys/class/power_supply/battery/current_now 2>/dev/null); echo capacity=\$(cat /sys/class/power_supply/battery/capacity 2>/dev/null); echo status=\$(cat /sys/class/power_supply/battery/status 2>/dev/null)"
            val output = Runtime.getRuntime().exec(arrayOf("su", "-c", script)).inputStream.bufferedReader().readText()
            var temp = ""; var current = ""; var capacity = ""; var status = ""
            output.lines().forEach { line ->
                when {
                    line.startsWith("temp=") -> temp = line.removePrefix("temp=").trim()
                    line.startsWith("current=") -> current = line.removePrefix("current=").trim()
                    line.startsWith("capacity=") -> capacity = line.removePrefix("capacity=").trim()
                    line.startsWith("status=") -> status = line.removePrefix("status=").trim()
                }
            }
            BatteryInfo(temp, current, capacity, status)
        }.getOrDefault(BatteryInfo())
    }

    data class LogDataPoint(val time: String, val level: Float, val temp: Float, val watt: Float)

    suspend fun getParsedLogData(): List<LogDataPoint> = withContext(Dispatchers.IO) {
        runCatching {
            val lines = Runtime.getRuntime().exec(arrayOf("su", "-c", "sed -n '5,\$p' '$LOG_PATH'")).inputStream.bufferedReader().readLines()
            lines.mapNotNull { line ->
                try {
                    // 精准匹配格式: 07-07 18:06:20 电量 73% 温度 43℃ 电流 995mA
                    
                    // 1. 匹配时间 (格式: MM-DD HH:mm:ss)
                    val timeMatch = Regex("(\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2})").find(line)
                    val time = timeMatch?.groupValues?.get(1) ?: ""
                    
                    // 2. 匹配电量 (取数字)
                    val levelMatch = Regex("电量\\s+(\\d+)%?").find(line)
                    val level = levelMatch?.groupValues?.get(1)?.toFloat() ?: 0f
                    
                    // 3. 匹配温度 (支持 ℃ 符号)
                    val tempMatch = Regex("温度\\s+(\\d+)").find(line)
                    val temp = tempMatch?.groupValues?.get(1)?.toFloat() ?: 0f
                    
                    // 4. 匹配电流 (支持 mA 符号)
                    val maMatch = Regex("电流\\s+(-?\\d+)").find(line)
                    val ma = maMatch?.groupValues?.get(1)?.toFloat() ?: 0f
                    
                    // 计算瓦数 (W = mA * 4.0 / 1000)
                    val watt = Math.abs(ma * 4.0f / 1000f)
                    
                    if (time.isNotEmpty() && (level != 0f || temp != 0f)) {
                        LogDataPoint(time, level, temp, watt)
                    } else null
                } catch (e: Exception) { null }
            }
        }.getOrDefault(emptyList())
    }
}
