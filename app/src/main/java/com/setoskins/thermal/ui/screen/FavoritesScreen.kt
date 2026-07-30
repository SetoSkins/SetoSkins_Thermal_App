package com.setoskins.thermal.ui.screen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setoskins.thermal.data.ModuleDetector
import com.setoskins.thermal.ui.component.ColorLevel
import com.setoskins.thermal.ui.component.ColorTemp
import com.setoskins.thermal.ui.component.ColorWatt
import com.setoskins.thermal.ui.component.BatteryInfoCard
import com.setoskins.thermal.ui.component.LogLineChart
import com.setoskins.thermal.ui.component.MiuixCard
import com.setoskins.thermal.ui.component.SectionTitle
import com.setoskins.thermal.ui.component.ThemedSwitch
import kotlinx.coroutines.isActive
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun FavoritesScreen(useMonet: Boolean, reloadTrigger: Int = 0, scrollBehavior: ScrollBehavior? = null, contentPaddingTop: Dp = 0.dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    var selectedIndex by rememberSaveable { mutableIntStateOf(prefs.getInt("logViewStyle", 0)) }
    var logContent by remember { mutableStateOf("正在加载日志...") }
    var logPoints by remember { mutableStateOf<List<ModuleDetector.LogDataPoint>>(emptyList()) }
    var showWatt by rememberSaveable { mutableStateOf(true) }
    var showLevel by rememberSaveable { mutableStateOf(true) }
    var showTemp by rememberSaveable { mutableStateOf(true) }
    var onlyWattMode by rememberSaveable { mutableStateOf(false) }
    var isCharging by remember { mutableStateOf(false) }
    val config = LocalConfiguration.current
    val isZh = remember { config.locales.get(0).language == "zh" }
    val isCenterText = remember(logContent) { logContent == "日志文件为空" || logContent == "无法读取日志文件" || logContent == "正在加载日志..." }

    // 使用广播监听充电状态变化，替代轮询中 readBatteryInfo 调用
    DisposableEffect(Unit) {
        var isRegistered = false
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                isCharging = intent.action == Intent.ACTION_POWER_CONNECTED
            }
        }
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            context.registerReceiver(receiver, filter)
            isRegistered = true
        } catch (_: Exception) {}
        onDispose {
            if (isRegistered) {
                try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
            }
        }
    }

    // 初始读取一次充电状态
    LaunchedEffect(Unit) {
        val batteryInfo = ModuleDetector.readBatteryInfo()
        isCharging = batteryInfo.status == "Charging"
    }

    LaunchedEffect(reloadTrigger, selectedIndex, isCharging) {
        if (selectedIndex == 0) {
            var lastLogContent = ""
            while (isActive) {
                val newContent = ModuleDetector.readLog()
                // 仅当内容变化时更新状态，避免无意义重组
                if (newContent != lastLogContent) {
                    lastLogContent = newContent
                    logContent = newContent
                }
                if (isCharging) kotlinx.coroutines.delay(15000) else break
            }
        } else {
            var lastPointsHash = 0
            while (isActive) {
                val newPoints = ModuleDetector.getParsedLogData()
                // 使用 size + 前 3 个和后 3 个数据点 hash 快速判断数据是否变化
                val newHash = newPoints.size * 31 +
                    newPoints.take(3).fold(0) { acc, p -> acc * 31 + p.hashCode() } +
                    newPoints.takeLast(3).fold(0) { acc, p -> acc * 31 + p.hashCode() }
                if (newHash != lastPointsHash) {
                    lastPointsHash = newHash
                    logPoints = newPoints
                }
                if (isCharging) kotlinx.coroutines.delay(15000) else break
            }
        }
    }

    LazyColumn(modifier = modifier.fillMaxSize().then(if (scrollBehavior != null) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else Modifier), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = contentPaddingTop, bottom = 16.dp)) {
        item(key = "battery_card") { BatteryInfoCard() }
        item(key = "log_title") { SectionTitle { SmallTitle(text = "日志", modifier = Modifier.offset(y = (8).dp)) } }
        item(key = "log_style") { MiuixCard(modifier = Modifier.padding(top = 11.dp)) { Column {
                WindowDropdownPreference(items = listOf(if (isZh) "文字样式" else "Text", if (isZh) "曲线样式" else "Curve"), selectedIndex = selectedIndex, title = if (isZh) "显示样式" else "View Mode", onSelectedIndexChange = { selectedIndex = it; prefs.edit().putInt("logViewStyle", it).apply() })
                AnimatedVisibility(visible = selectedIndex == 1, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    BasicComponent(title = if (isZh) "仅显示功耗曲线" else "Watt Only", endActions = { ThemedSwitch(checked = onlyWattMode, onCheckedChange = { onlyWattMode = it }, useMonet = useMonet) })
                }
            } } }
        item(key = "log_content") { MiuixCard(modifier = Modifier.padding(top = 16.dp)) {
                if (selectedIndex == 0) {
                    val secondaryColor = MiuixTheme.colorScheme.onSurfaceSecondary
                    val onSurfaceColor = MiuixTheme.colorScheme.onSurface
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = if (isCenterText) Alignment.Center else Alignment.TopStart) {
                        Text(text = logContent, fontSize = 13.sp, color = if (isCenterText && logContent != "正在加载日志...") secondaryColor else onSurfaceColor, textAlign = if (isCenterText) TextAlign.Center else TextAlign.Start, modifier = if (isCenterText) Modifier.fillMaxWidth() else Modifier)
                    }
                } else {
                    if (logPoints.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) { Text(if (isZh) "暂无曲线数据" else "No Data", color = MiuixTheme.colorScheme.onSurfaceSecondary) } }
                    else {
                        Column {
                                val legendWeight by animateFloatAsState(
                                    targetValue = if (onlyWattMode) 0.001f else 1f,
                                    animationSpec = tween(durationMillis = 420, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)),
                                    label = "legend_weight"
                                )
                                val legendAlpha by animateFloatAsState(
                                    targetValue = if (onlyWattMode) 0f else 1f,
                                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                                    label = "legend_alpha"
                                )
                                val legendScale by animateFloatAsState(
                                    targetValue = if (onlyWattMode) 0.82f else 1f,
                                    animationSpec = tween(durationMillis = 420, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)),
                                    label = "legend_scale"
                                )
                                Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        FilterLegend(if (isZh) "功耗" else "Watt", ColorWatt, showWatt) { showWatt = !showWatt }
                                    }
                                    Box(modifier = Modifier.weight(legendWeight).wrapContentSize(unbounded = true).graphicsLayer { alpha = legendAlpha; scaleX = legendScale }, contentAlignment = Alignment.Center) {
                                        FilterLegend(if (isZh) "电量" else "Bat", ColorLevel, showLevel) { showLevel = !showLevel }
                                    }
                                    Box(modifier = Modifier.weight(legendWeight).wrapContentSize(unbounded = true).graphicsLayer { alpha = legendAlpha; scaleX = legendScale }, contentAlignment = Alignment.Center) {
                                        FilterLegend(if (isZh) "温度" else "Temp", ColorTemp, showTemp) { showTemp = !showTemp }
                                    }
                                }
                                LogLineChart(points = logPoints, isZh = isZh, showWatt = if (onlyWattMode) true else showWatt, showLevel = showLevel, showTemp = showTemp, isCharging = isCharging, onlyWattMode = onlyWattMode)
                            }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterLegend(label: String, color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onClick() }.padding(horizontal = 8.dp, vertical = 4.dp).graphicsLayer { alpha = if (isSelected) 1f else 0.4f }) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape)); Spacer(modifier = Modifier.width(6.dp)); Text(label, fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurface, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
    }
}