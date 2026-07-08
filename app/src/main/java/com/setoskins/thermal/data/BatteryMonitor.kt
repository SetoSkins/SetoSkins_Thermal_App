package com.setoskins.thermal.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

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
        val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        
        // W = (V / 1000) * (abs(mA) / 1000)
        // currentNow is usually in uA on most devices, some are mA. We assume uA for modern devices.
        val currentMa = Math.abs(currentNow / 1000f)
        val wattage = (voltage / 1000f) * (currentMa / 1000f)

        return BatteryState(
            isCharging = isCharging,
            level = batteryPct,
            temperature = temp,
            voltage = voltage,
            currentNow = currentNow,
            wattage = wattage
        )
    }
}
