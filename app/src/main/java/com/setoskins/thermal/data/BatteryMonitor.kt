package com.setoskins.thermal.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.io.File

object BatteryMonitor {

    data class BatteryState(
        val isCharging: Boolean,
        val level: Int,
        val temperature: Float,
        val voltage: Int, // mV
        val currentNow: Int, // uA
        val wattage: Float
    )

    fun getBatteryState(context: Context): BatteryState {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else 0
        
        val temp = (intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
        val broadcastVoltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val apiCurrentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val sysCurrentNow = readLongFromSysfs("/sys/class/power_supply/battery/current_now")
        val sysVoltageNow = readLongFromSysfs("/sys/class/power_supply/battery/voltage_now")

        val currentNow = (sysCurrentNow ?: apiCurrentNow.toLong()).toInt()
        val voltage = when {
            sysVoltageNow != null && sysVoltageNow > 100_000L -> (sysVoltageNow / 1000L).toInt()
            sysVoltageNow != null && sysVoltageNow > 0L -> sysVoltageNow.toInt()
            else -> broadcastVoltage
        }
        
        // W = (V / 1000) * (abs(mA) / 1000)
        // current_now is usually in uA on modern devices.
        val currentA = kotlin.math.abs(currentNow) / 1_000_000f
        val wattage = (voltage / 1000f) * currentA

        return BatteryState(
            isCharging = isCharging,
            level = batteryPct,
            temperature = temp,
            voltage = voltage,
            currentNow = currentNow,
            wattage = wattage
        )
    }

    private fun readLongFromSysfs(path: String): Long? {
        return try {
            File(path).takeIf { it.canRead() }?.readText()?.trim()?.toLongOrNull()
        } catch (_: Exception) {
            null
        }
    }
}
