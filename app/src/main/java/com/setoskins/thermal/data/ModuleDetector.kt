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
    private const val BLACKLIST_PATH = "/data/adb/modules/SetoSkins/分应用调速.prop"
    private const val BLACKLIST_NAME = "分应用调速.prop"
    private const val WHITELIST_APP_PATH = "/data/adb/modules/SetoSkins/无温控应用.prop"
    private const val WHITELIST_APP_NAME = "无温控应用.prop"
    private const val BYPASS_LIST_PATH = "/data/adb/modules/SetoSkins/旁路充电名单.prop"
    private const val BYPASS_LIST_NAME = "旁路充电名单.prop"
    private const val BYPASS_CONFIG_PATH = "/data/adb/modules/SetoSkins/旁路充电配置.prop"
    private const val MODULE_PROP_PATH = "/data/adb/modules/SetoSkins/module.prop"
    private const val LOG_PATH = "/data/adb/modules/SetoSkins/log.log"
    private const val THERMAL_SCRIPT_PATH = "/data/adb/modules/SetoSkins/system/Seto_fuckthermal.sh"
    private const val CURRENT_SCRIPT_PATH = "/data/adb/SetoSkins/Seto_fuckthermal.sh"
    private const val UPDATE_URL = "https://raw.githubusercontent.com/SetoSkins/SetoSkins_Thermal/refs/heads/master/SetoSkins.json"
    private const val TIMEOUT_MS = 5000L

    // 预编译正则表达式，避免每次调用时重复编译
    private val VERSION_CODE_REGEX = Regex("\"versionCode\"\\s*:\\s*(\\d+)")
    private val TIME_REGEX = Regex("(\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2})")
    private val LEVEL_REGEX = Regex("电量\\s+(\\d+)%?")
    private val TEMP_REGEX = Regex("温度\\s+(\\d+)")
    private val CURRENT_REGEX = Regex("电流\\s+(-?\\d+)")

    /**
     * 检测设备是否有 root 权限。
     */
    suspend fun requestRoot(): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Requesting root...")
        
        val process = try {
            // 直接执行 su -c id，不再预检 which su，因为某些环境下 which 可能被限
            Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to exec su: ${e.message}")
            return@withContext false
        }
        
        val result = withTimeoutOrNull(TIMEOUT_MS) {
            try {
                // 读取输出
                val output = process.inputStream.bufferedReader().use { it.readText() }
                val errorOutput = process.errorStream.bufferedReader().use { it.readText() }
                val exitCode = process.waitFor()
                Log.d(TAG, "su -c id output: $output, error: $errorOutput, exitCode: $exitCode")
                
                // 只要包含 uid=0 就算成功
                exitCode == 0 && output.contains("uid=0")
            } catch (e: Exception) {
                Log.e(TAG, "Error during root check: ${e.message}")
                false
            }
        }
        
        if (result == null) {
            Log.w(TAG, "Root check timed out (5s), killing process")
            process.destroyForcibly()
        }
        
        val finalResult = result ?: false
        Log.d(TAG, "Root check final result: $finalResult")
        finalResult
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
            val command = "if grep -q '^$key=' '$CONFIG_PATH'; then sed -i 's/^$key=.*/$key=$strValue/g' '$CONFIG_PATH'; else echo '$key=$strValue' >> '$CONFIG_PATH'; fi"
            Runtime.getRuntime().exec(arrayOf("su", "-c", command)).waitFor()
        }
    }

    /**
     * 执行温控脚本，在每次开关变更时触发。
     * 超时保护：最多等待 TIMEOUT_MS，防止脚本卡死阻塞线程。
     */
    suspend fun executeThermalScript(): Unit = withContext(Dispatchers.IO) {
        runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "sh '$THERMAL_SCRIPT_PATH'"))
            val result = withTimeoutOrNull(TIMEOUT_MS) {
                process.waitFor()
            }
            if (result == null) {
                Log.w(TAG, "Thermal script execution timed out, killing process")
                process.destroyForcibly()
            }
        }
    }

    /**
     * 执行电流脚本，在修改最大电流数开关变更时触发。
     */
    suspend fun executeCurrentScript(): Unit = withContext(Dispatchers.IO) {
        runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "sh '$CURRENT_SCRIPT_PATH'"))
            val result = withTimeoutOrNull(TIMEOUT_MS) {
                process.waitFor()
            }
            if (result == null) {
                Log.w(TAG, "Current script execution timed out, killing process")
                process.destroyForcibly()
            }
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

    suspend fun readBlacklistConfig(): Map<String, String> = withContext(Dispatchers.IO) {
        val path = findFilePath(BLACKLIST_NAME) ?: BLACKLIST_PATH
        readAppConfig(path)
    }

    suspend fun readAppConfig(path: String): Map<String, String> = withContext(Dispatchers.IO) {
        runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat '$path'"))
            val lines = process.inputStream.bufferedReader().readLines()
            val map = mutableMapOf<String, String>()
            lines.forEach { line ->
                val parts = line.trim().split(" ", limit = 2)
                if (parts.size == 2) map[parts[0]] = parts[1]
            }
            map
        }.getOrDefault(emptyMap())
    }

    suspend fun writeBlacklistConfig(entries: Map<String, String>): Unit = withContext(Dispatchers.IO) {
        val path = findFilePath(BLACKLIST_NAME) ?: BLACKLIST_PATH
        writeAppConfig(path, entries)
    }

    suspend fun writeAppConfig(path: String, entries: Map<String, String>): Unit = withContext(Dispatchers.IO) {
        runCatching {
            val content = entries.entries.joinToString("\n") { "${it.key} ${it.value}" }
            val command = "echo '$content' > '$path'"
            Runtime.getRuntime().exec(arrayOf("su", "-c", command)).waitFor()
        }
    }

    suspend fun readWhitelistAppPackages(): Set<String> = withContext(Dispatchers.IO) {
        runCatching {
            val path = findFilePath(WHITELIST_APP_NAME) ?: WHITELIST_APP_PATH
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat '$path'"))
            process.inputStream.bufferedReader().readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
        }.getOrDefault(emptySet())
    }

    suspend fun writeWhitelistAppPackages(packages: Set<String>): Unit = withContext(Dispatchers.IO) {
        runCatching {
            val path = findFilePath(WHITELIST_APP_NAME) ?: WHITELIST_APP_PATH
            val content = packages.joinToString("\n")
            val command = "echo '$content' > '$path'"
            Runtime.getRuntime().exec(arrayOf("su", "-c", command)).waitFor()
        }
    }

    suspend fun readBypassListPackages(): Set<String> = withContext(Dispatchers.IO) {
        runCatching {
            val path = findFilePath(BYPASS_LIST_NAME) ?: BYPASS_LIST_PATH
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat '$path'"))
            process.inputStream.bufferedReader().readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
        }.getOrDefault(emptySet())
    }

    suspend fun writeBypassListPackages(packages: Set<String>): Unit = withContext(Dispatchers.IO) {
        runCatching {
            val path = findFilePath(BYPASS_LIST_NAME) ?: BYPASS_LIST_PATH
            val content = packages.joinToString("\n")
            val command = "echo '$content' > '$path'"
            Runtime.getRuntime().exec(arrayOf("su", "-c", command)).waitFor()
        }
    }

    private suspend fun findFilePath(fileName: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val process = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "find '$MODULE_PATH' -name '$fileName' -type f 2>/dev/null | head -1")
            )
            process.inputStream.bufferedReader().readLine()?.trim()?.takeIf { it.isNotEmpty() }
        }.getOrNull()
    }

    suspend fun writeBypassConfig(config: Map<String, String>): Unit = withContext(Dispatchers.IO) {
        runCatching {
            val content = config.entries.joinToString("\n") { "${it.key}=${it.value}" }
            val command = "echo '$content' > '$BYPASS_CONFIG_PATH'"
            Runtime.getRuntime().exec(arrayOf("su", "-c", command)).waitFor()
        }
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
            connection.readTimeout = 5000
            val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
            val remoteVersionMatch = VERSION_CODE_REGEX.find(jsonText)
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
            val rawPoints = lines.mapNotNull { line ->
                try {
                    // 精准匹配格式: 07-07 18:06:20 电量 73% 温度 43℃ 电流 995mA
                    
                    // 1. 匹配时间 (格式: MM-DD HH:mm:ss)
                    val timeMatch = TIME_REGEX.find(line)
                    val time = timeMatch?.groupValues?.get(1) ?: ""

                    // 2. 匹配电量 (取数字)
                    val levelMatch = LEVEL_REGEX.find(line)
                    // 3. 匹配温度 (支持 ℃ 符号)
                    val tempMatch = TEMP_REGEX.find(line)
                    // 4. 匹配电流 (支持 mA 符号)
                    val maMatch = CURRENT_REGEX.find(line)

                    // 如果缺少电量或电流数据，跳过这条记录，防止曲线归零
                    if (time.isNotEmpty() && levelMatch != null && maMatch != null && tempMatch != null) {
                        val level = levelMatch.groupValues[1].toFloat()
                        val temp = tempMatch.groupValues[1].toFloat()
                        val ma = maMatch.groupValues[1].toFloat()
                        // 计算瓦数 (W = mA * 4.0 / 1000)
                        val watt = Math.abs(ma * 4.0f / 1000f)
                        LogDataPoint(time, level, temp, watt)
                    } else null
                } catch (e: Exception) { null }
            }
            // 按分钟去重：80%电量前保留第一条，80%及之后保留最后一条
            rawPoints.groupBy { it.time.substringBeforeLast(":") }.values.map { group ->
                val first80Index = group.indexOfFirst { it.level >= 80f }
                if (first80Index >= 0) group.last() else group.first()
            }
        }.getOrDefault(emptyList())
    }
}
