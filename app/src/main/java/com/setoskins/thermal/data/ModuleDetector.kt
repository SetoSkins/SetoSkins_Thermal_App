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
        Log.d(TAG, "Requesting root access...")
        val process = try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        } catch (e: Exception) {
            Log.e(TAG, "Exec failed: ${e.message}")
            return@withContext false
        }

        // 增加到 8 秒超时，确保用户有足够时间点击系统授权弹窗
        val finished = try {
            process.waitFor(8000, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            Log.e(TAG, "Wait failed: ${e.message}")
            false
        }

        if (!finished) {
            Log.w(TAG, "Root check timed out")
            process.destroyForcibly()
            return@withContext false
        }

        val output = try {
            process.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, "Read output failed: ${e.message}")
            ""
        }

        val exitCode = process.exitValue()
        val hasRoot = exitCode == 0 && output.contains("uid=0")
        Log.d(TAG, "Root check result: $hasRoot, Exit code: $exitCode, Output: $output")
        hasRoot
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

    /**
     * 更新配置文件中的特定键值。
     */
    suspend fun updateConfig(key: String, value: Any): Unit = withContext(Dispatchers.IO) {
        runCatching {
            val strValue = value.toString()
            val command = "sed -i 's/^$key=.*/$key=$strValue/g' '$CONFIG_PATH'"
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            process.waitFor()
        }
    }

    /**
     * 执行温控脚本，在每次开关变更时触发。
     */
    suspend fun executeThermalScript(): Unit = withContext(Dispatchers.IO) {
        runCatching {
            val command = "sh '$THERMAL_SCRIPT_PATH'"
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            process.waitFor()
        }
    }

    /**
     * 读取配置文件中的所有键值对。
     */
    suspend fun readConfig(): Map<String, String> = withContext(Dispatchers.IO) {
        runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat '$CONFIG_PATH'"))
            val lines = process.inputStream.bufferedReader().readLines()
            process.waitFor()

            val configMap = mutableMapOf<String, String>()
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() && trimmed.contains("=")) {
                    val parts = trimmed.split("=", limit = 2)
                    if (parts.size == 2) {
                        configMap[parts[0].trim()] = parts[1].trim()
                    }
                }
            }
            configMap
        }.getOrDefault(emptyMap())
    }

    /**
     * 重置模块配置：将选项改为 false，但保留特定项为 true，不删除文件内容。
     */
    suspend fun resetConfig(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            // 1. 先将所有非注释行的等号后内容改为 false
            val cmd1 = "sed -i '/^[^#]/ s/=.*/=false/' '$CONFIG_PATH'"
            // 2. 强制将特定项设为 true
            val keysToKeepTrue = listOf("开启充电Log", "关闭录制温控", "关闭相机温控", "加快部分游戏启动速度")
            val cmd2 = keysToKeepTrue.joinToString(" && ") { 
                "sed -i 's/^$it=.*/$it=true/g' '$CONFIG_PATH'" 
            }
            
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "$cmd1 && $cmd2"))
            process.waitFor() == 0
        }.getOrDefault(false)
    }

    /**
     * 从临时路径直接复制并替换配置文件。
     */
    suspend fun importConfigFile(tempSourcePath: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            // 直接使用 cp 命令强制覆盖,并设置 644 权限
            val command = "cp -f '$tempSourcePath' '$CONFIG_PATH' && chmod 644 '$CONFIG_PATH'"
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            process.waitFor() == 0
        }.getOrDefault(false)
    }

    /**
     * 读取配置文件的原始内容（用于导出）。
     */
    suspend fun readConfigRaw(): String = withContext(Dispatchers.IO) {
        runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat '$CONFIG_PATH'"))
            val content = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            content
        }.getOrDefault("")
    }

    /**
     * 读取日志文件内容。
     */
    suspend fun readLog(): String = withContext(Dispatchers.IO) {
        runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat '$LOG_PATH'"))
            val output = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            output.ifEmpty { "日志文件为空" }
        }.getOrDefault("无法读取日志文件")
    }

    /**
     * 获取模块版本号。
     */
    suspend fun getModuleVersion(): String = withContext(Dispatchers.IO) {
        runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat '$MODULE_PROP_PATH'"))
            val lines = process.inputStream.bufferedReader().readLines()
            process.waitFor()
            lines.firstOrNull { it.startsWith("version=") }?.split("=")?.getOrNull(1)?.trim() ?: "未知版本"
        }.getOrDefault("未知版本")
    }

    /**
     * 获取本地模块的 versionCode。
     */
    suspend fun getLocalVersionCode(): Int = withContext(Dispatchers.IO) {
        runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat '$MODULE_PROP_PATH'"))
            val lines = process.inputStream.bufferedReader().readLines()
            process.waitFor()
            lines.firstOrNull { it.startsWith("versionCode=") }?.split("=")?.getOrNull(1)?.trim()?.toInt() ?: 0
        }.getOrDefault(0)
    }

    /**
     * 检查更新。
     */
    suspend fun checkUpdate(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val localVersion = getLocalVersionCode()
            
            // 使用简易方式获取远程内容 (需要网络权限)
            val connection = java.net.URL(UPDATE_URL).openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
            
            // 简单的正则匹配提取 versionCode
            val remoteVersionMatch = "\"versionCode\"\\s*:\\s*(\\d+)".toRegex().find(jsonText)
            val remoteVersion = remoteVersionMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            
            remoteVersion > localVersion
        }.getOrDefault(false)
    }

    data class BatteryInfo(
        val temperature: String = "",
        val current: String = "",
        val capacity: String = "",
        val status: String = ""
    )

    suspend fun readBatteryInfo(): BatteryInfo = withContext(Dispatchers.IO) {
        runCatching {
            val script = "echo temp=\$(cat /sys/class/power_supply/battery/temp 2>/dev/null); echo current=\$(cat /sys/class/power_supply/battery/current_now 2>/dev/null); echo capacity=\$(cat /sys/class/power_supply/battery/capacity 2>/dev/null); echo status=\$(cat /sys/class/power_supply/battery/status 2>/dev/null)"
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", script))
            val output = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()

            var temp = ""
            var current = ""
            var capacity = ""
            var status = ""

            output.lines().forEach { line ->
                when {
                    line.startsWith("temp=") -> temp = line.removePrefix("temp=").trim()
                    line.startsWith("current=") -> current = line.removePrefix("current=").trim()
                    line.startsWith("capacity=") -> capacity = line.removePrefix("capacity=").trim()
                    line.startsWith("status=") -> status = line.removePrefix("status=").trim()
                }
            }
            BatteryInfo(temperature = temp, current = current, capacity = capacity, status = status)
        }.getOrDefault(BatteryInfo())
    }
}
