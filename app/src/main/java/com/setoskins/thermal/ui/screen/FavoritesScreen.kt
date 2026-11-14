package com.setoskins.thermal.ui.screen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
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
import androidx.compose.material3.MaterialTheme
import com.setoskins.thermal.data.ModuleDetector
import com.setoskins.thermal.ui.component.BatteryInfoCard
import com.setoskins.thermal.ui.component.LogLineChart
import com.setoskins.thermal.ui.component.MiuixCard
import com.setoskins.thermal.ui.component.ThemedOptionCard
import com.setoskins.thermal.ui.component.MaterialSegmentedColumn
import com.setoskins.thermal.ui.component.MaterialDropdownItem
import com.setoskins.thermal.ui.component.IconTile
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DisplaySettings
import com.setoskins.thermal.ui.component.SectionTitle
import com.setoskins.thermal.ui.component.VerticalScrollBar
import com.setoskins.thermal.ui.component.rememberScrollBarAdapter
import kotlinx.coroutines.isActive
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import com.setoskins.thermal.ui.component.animation.customOverScroll
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.ExperimentalFoundationApi
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(top.yukonga.miuix.kmp.interfaces.ExperimentalScrollBarApi::class)
@Composable
fun FavoritesScreen(useMonet: Boolean, reloadTrigger: Int = 0, scrollBehavior: ScrollBehavior? = null, materialScrollConnection: NestedScrollConnection? = null, contentPaddingTop: Dp = 0.dp, isPageActive: Boolean = true, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    var selectedIndex by rememberSaveable { mutableIntStateOf(prefs.getInt("logViewStyle", 0)) }
    var logContent by remember { mutableStateOf("正在加载日志...") }
    var logPoints by remember { mutableStateOf<List<ModuleDetector.LogDataPoint>>(emptyList()) }
    var showWatt by rememberSaveable { mutableStateOf(true) }
    var showLevel by rememberSaveable { mutableStateOf(true) }
    var showTemp by rememberSaveable { mutableStateOf(true) }
    var singleCurveMode by rememberSaveable { mutableStateOf<String?>(null) }
    var batteryInfo by remember { mutableStateOf(ModuleDetector.BatteryInfo()) }
    var throttleCount by remember { mutableIntStateOf(0) }
    var powerConnected by remember { mutableStateOf(false) }
    // 充电状态优先取电池信息里的 status，广播作为兜底（不必为这一项单独再起一次 su）
    val isCharging = batteryInfo.status == "Charging" || (batteryInfo.status.isEmpty() && powerConnected)
    val config = LocalConfiguration.current
    val isZh = remember { config.locales.get(0).language == "zh" }
    val isCenterText = remember(logContent) { logContent == "日志文件为空" || logContent == "无法读取日志文件" || logContent == "正在加载日志..." }
    val displayLogContent = if (isZh) logContent else when (logContent) {
        "正在加载日志..." -> "Loading log..."
        "日志文件为空" -> "Log file is empty"
        "无法读取日志文件" -> "Unable to read log file"
        else -> logContent
    }

    // 使用广播监听充电状态变化，替代轮询中 readBatteryInfo 调用
    DisposableEffect(Unit) {
        var isRegistered = false
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                powerConnected = intent.action == Intent.ACTION_POWER_CONNECTED
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

    // 首次进页优先取 App 启动时的预热结果，避免在切页动画里起 su 进程
    LaunchedEffect(Unit) {
        batteryInfo = ModuleDetector.takePrewarmedBattery() ?: ModuleDetector.readBatteryInfo()
        throttleCount = ModuleDetector.takePrewarmedThrottle() ?: ModuleDetector.readThermalThrottleCount()
    }

    // 电池信息轮询：只在停留在日志页时进行（原先在 BatteryInfoCard 内部，上移以便全页共享同一份数据）
    LaunchedEffect(isPageActive) {
        if (!isPageActive) return@LaunchedEffect
        while (isActive) {
            kotlinx.coroutines.delay(3000)
            batteryInfo = ModuleDetector.readBatteryInfo()
            throttleCount = ModuleDetector.readThermalThrottleCount()
        }
    }

    // 数据预加载：启动/刷新/样式切换时先读一次，保证进入页面即有数据
    // 不再把 isCharging 作为 key：它只影响曲线配色，作为 key 会在首次进页时再触发一次 su 读日志
    LaunchedEffect(reloadTrigger, selectedIndex) {
        if (selectedIndex == 0) {
            logContent = ModuleDetector.takePrewarmedLog() ?: ModuleDetector.readLog()
        } else {
            logPoints = ModuleDetector.getParsedLogData()
        }
    }

    // 周期刷新仅在停留在日志页且充电时进行：进入页面时不立即重复读取，先延迟避免切页动画与 su 调用叠加
    LaunchedEffect(isPageActive, isCharging) {
        if (!isPageActive || !isCharging) return@LaunchedEffect
        while (isActive) {
            kotlinx.coroutines.delay(15000)
            if (selectedIndex == 0) {
                logContent = ModuleDetector.readLog()
            } else {
                logPoints = ModuleDetector.getParsedLogData()
            }
        }
    }

    val listState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize()) {
        @OptIn(ExperimentalFoundationApi::class)
        CompositionLocalProvider(LocalOverscrollFactory provides null) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().customOverScroll()
                    .then(if (scrollBehavior != null) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else Modifier)
                    // Material 顶栏：必须挂在列表自身（overscroll 之后），否则顶部下拉会被 overscroll 吃掉
                    .then(if (materialScrollConnection != null) Modifier.nestedScroll(materialScrollConnection) else Modifier),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = contentPaddingTop, bottom = 106.dp)
            ) {
                item(key = "battery_card") { BatteryInfoCard(info = batteryInfo, throttleCount = throttleCount, useMonet = useMonet) }
                item(key = "log_title") { SectionTitle { SmallTitle(text = if (isZh) "日志" else "Log", modifier = Modifier.offset(y = (4).dp)) } }
                item(key = "log_style") {
                    if (useMonet) {
                        MaterialSegmentedColumn(
                            modifier = Modifier.padding(top = 11.dp),
                            items = listOf({
                                MaterialDropdownItem(
                                    headline = if (isZh) "显示样式" else "View Mode",
                                    items = listOf(if (isZh) "文字样式" else "Text", if (isZh) "曲线样式" else "Curve"),
                                    selectedIndex = selectedIndex,
                                    onItemSelected = {
                                        selectedIndex = it
                                        prefs.edit().putInt("logViewStyle", it).apply()
                                    },
                                    leading = { IconTile(Icons.Filled.DisplaySettings, contentDescription = if (isZh) "显示样式" else "View Mode") }
                                )
                            })
                        )
                    } else {
                        ThemedOptionCard(useMonet = useMonet, modifier = Modifier.padding(top = 11.dp)) {
                            WindowDropdownPreference(items = listOf(if (isZh) "文字样式" else "Text", if (isZh) "曲线样式" else "Curve"), selectedIndex = selectedIndex, title = if (isZh) "显示样式" else "View Mode", onSelectedIndexChange = { selectedIndex = it; prefs.edit().putInt("logViewStyle", it).apply() })
                        }
                    }
                }
                item(key = "log_content") {
                    ThemedOptionCard(useMonet = useMonet, modifier = Modifier.padding(top = 16.dp)) {
                        if (selectedIndex == 0) {
                            val secondaryColor = if (useMonet) MaterialTheme.colorScheme.onSurfaceVariant else MiuixTheme.colorScheme.onSurfaceSecondary
                            val onSurfaceColor = if (useMonet) MaterialTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurface
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = if (isCenterText) Alignment.Center else Alignment.TopStart) {
                                Text(text = displayLogContent, fontSize = 13.sp, color = if (isCenterText && logContent != "正在加载日志...") secondaryColor else onSurfaceColor, textAlign = if (isCenterText) TextAlign.Center else TextAlign.Start, modifier = if (isCenterText) Modifier.fillMaxWidth() else Modifier)
                            }
                        } else {
                            if (logPoints.isEmpty() || logPoints.size < 2) { Box(modifier = Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) { Text(if (isZh) "暂无曲线数据" else "No data", color = if (useMonet) MaterialTheme.colorScheme.onSurfaceVariant else MiuixTheme.colorScheme.onSurfaceSecondary) } }
                            else {
                                LogLineChart(points = logPoints, isZh = isZh, showWatt = if (singleCurveMode == "watt") true else showWatt, showLevel = showLevel, showTemp = showTemp, isCharging = isCharging, singleCurveMode = singleCurveMode, onSingleCurveModeChange = { singleCurveMode = it })
                            }
                        }
                    }
                }
            }
        }
        VerticalScrollBar(
            adapter = rememberScrollBarAdapter(listState)
        )
    }
}

@Composable
fun FilterLegend(label: String, color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onClick() }.padding(horizontal = 8.dp, vertical = 4.dp).graphicsLayer { alpha = if (isSelected) 1f else 0.4f }) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape)); Spacer(modifier = Modifier.width(6.dp)); Text(label, fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurface, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
    }
}
