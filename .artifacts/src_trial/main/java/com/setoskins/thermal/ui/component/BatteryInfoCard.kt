package com.setoskins.thermal.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setoskins.thermal.data.ModuleDetector
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BatteryInfoCard(info: ModuleDetector.BatteryInfo, throttleCount: Int, useMonet: Boolean) {
    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"
    val tempText = info.temperature.let {
        if (it.isNotEmpty()) try {
            "${"%.1f".format(it.toInt() / 10.0)}°C"
        } catch (_: Exception) {
            "${it}°C"
        } else "..."
    }
    val currentText = info.current.let {
        if (it.isNotEmpty()) try {
            "${it.toInt() / -1000} mA"
        } catch (_: Exception) {
            "${it} mA"
        } else "..."
    }
    val cap = info.capacity.let { if (it.isNotEmpty()) "${it}%" else "..." }
    val st = when {
        info.capacity.isNotEmpty() && info.capacity.toInt() >= 100 -> if (isZh) "已充满" else "Full"
        info.status == "Charging" -> if (isZh) "充电中" else "Charging"
        info.status == "Discharging" -> if (isZh) "放电中" else "Discharging"
        info.status == "Not charging" -> if (isZh) "未充电" else "Not charging"
        info.status == "Full" -> if (isZh) "已充满" else "Full"
        info.status == "Unknown" -> if (isZh) "未知" else "Unknown"
        else -> info.status.ifEmpty { "..." }
    }
    ThemedOptionCard(useMonet = useMonet) {
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 2.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                BatteryStatItem(if (isZh) "温度" else "Temp", tempText, Modifier.weight(1f))
                BatteryStatItem(if (isZh) "电流" else "Current", currentText, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                BatteryStatItem(if (isZh) "电量" else "Battery", cap, Modifier.weight(1f))
                BatteryStatItem(if (isZh) "充电状态" else "Charging status", st, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isZh) "触发内核墙限流：$throttleCount" else "Kernel throttling triggered: $throttleCount",
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BatteryStatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onSurfaceSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
