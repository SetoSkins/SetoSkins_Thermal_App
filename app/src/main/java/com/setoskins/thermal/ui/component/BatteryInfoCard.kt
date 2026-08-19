package com.setoskins.thermal.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setoskins.thermal.data.ModuleDetector
import kotlinx.coroutines.isActive
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BatteryInfoCard() {
    var info by remember { mutableStateOf(ModuleDetector.BatteryInfo()) }
    var throttleCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (isActive) {
            info = ModuleDetector.readBatteryInfo()
            throttleCount = ModuleDetector.readThermalThrottleCount()
            kotlinx.coroutines.delay(3000)
        }
    }
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
        info.capacity.isNotEmpty() && info.capacity.toInt() >= 100 -> "已充满"
        info.status == "Charging" -> "充电中"
        info.status == "Discharging" -> "放电中"
        info.status == "Not charging" -> "未充电"
        info.status == "Full" -> "已充满"
        info.status == "Unknown" -> "未知"
        else -> info.status.ifEmpty { "..." }
    }
    MiuixCard {
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 2.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                BatteryStatItem("温度", tempText, Modifier.weight(1f))
                BatteryStatItem("电流", currentText, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                BatteryStatItem("电量", cap, Modifier.weight(1f))
                BatteryStatItem("充电状态", st, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "触发内核墙限流：$throttleCount",
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