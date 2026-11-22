package com.setoskins.thermal.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class ChargingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val switch16 = prefs.getBoolean("switch16", false)
        if (!switch16) return

        val serviceIntent = Intent(context, SuperIslandService::class.java)
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                ContextCompat.startForegroundService(context, serviceIntent)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                context.stopService(serviceIntent)
            }
        }
    }
}
