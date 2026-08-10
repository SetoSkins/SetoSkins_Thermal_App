package com.setoskins.thermal.ui.screen

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.setoskins.thermal.data.ModuleDetector
import com.setoskins.thermal.service.SuperIslandService
import com.setoskins.thermal.ui.component.ConfigDialog
import com.setoskins.thermal.ui.component.GreenActivatedCard
import com.setoskins.thermal.ui.component.MiuixCard
import com.setoskins.thermal.ui.component.RedNotInstalledCard
import com.setoskins.thermal.ui.component.SliderRow
import com.setoskins.thermal.ui.component.ThemedSwitch
import com.setoskins.thermal.ui.component.ThemedTextField
import com.setoskins.thermal.ui.component.YellowUpdateCard
import com.setoskins.thermal.ui.component.compactSmallTitle
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    useMonet: Boolean,
    reloadTrigger: Int = 0,
    scrollBehavior: ScrollBehavior? = null,
    contentPaddingTop: Dp = 0.dp,
    onNavigateToBlacklist: () -> Unit = {},
    onNavigateToWhitelist: () -> Unit = {},
    onNavigateToBypassList: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    // ── 非调速区开关状态 ──
    var switch2 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch2", false)) }
    var switch3 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch3", true)) }
    var switch5 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch5", false)) }
    var switch6 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch6", false)) }
    var switch7 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch7", false)) }
    var switch8 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch8", false)) }
    var switch9 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch9", false)) }
    var switch14 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch14", false)) }
    var switch15 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch15", false)) }
    var switch16 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch16", false)) }
    var switch17 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch17", false)) }
    var switch18 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch18", false)) }
    var switch4 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch4", false)) }
    var switch10 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch10", false)) }
    var switch11 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch11", false)) }
    var switch12 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch12", false)) }
    var switch13 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch13", false)) }
    var appBypassTargetTemp by rememberSaveable { mutableStateOf(prefs.getString("appBypassTargetTemp", "40") ?: "40") }
    var appBypassRecoveryTemp by rememberSaveable { mutableStateOf(prefs.getString("appBypassRecoveryTemp", "35") ?: "35") }
    var appBypassMaxCurrent by rememberSaveable { mutableStateOf(prefs.getString("appBypassMaxCurrent", "22000000") ?: "22000000") }
    var appBypassStopLevel by rememberSaveable { mutableStateOf(prefs.getString("appBypassStopLevel", "10") ?: "10") }
    var globalBypassTargetTemp by rememberSaveable { mutableStateOf(prefs.getString("globalBypassTargetTemp", "40") ?: "40") }
    var globalBypassRecoveryTemp by rememberSaveable { mutableStateOf(prefs.getString("globalBypassRecoveryTemp", "35") ?: "35") }
    var globalBypassMaxCurrent by rememberSaveable { mutableStateOf(prefs.getString("globalBypassMaxCurrent", "22000000") ?: "22000000") }
    var globalBypassStopLevel by rememberSaveable { mutableStateOf(prefs.getString("globalBypassStopLevel", "10") ?: "10") }
    var dialogState by remember { mutableStateOf<DialogState?>(null) }
    val isChargingState = rememberSaveable { mutableStateOf(false) }

    // ── 模块状态 ──
    var moduleInstalled by rememberSaveable { mutableStateOf(false) }
    var hasUpdate by rememberSaveable { mutableStateOf(false) }
    var moduleVersion by rememberSaveable { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            if (switch16 && isChargingState.value) {
                val intent = Intent(context, SuperIslandService::class.java)
                ContextCompat.startForegroundService(context, intent)
            }
        }
    }

    LaunchedEffect(switch16) {
        val batteryInfo = ModuleDetector.readBatteryInfo()
        isChargingState.value = batteryInfo.status == "Charging"
        if (switch16) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            
            if (Build.VERSION.SDK_INT >= 35) {
                val canPost = try {
                    val method = notificationManager.javaClass.getMethod("canPostPromotedNotifications")
                    method.invoke(notificationManager) as Boolean
                } catch (e: Exception) { true }

                if (!canPost) {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    Toast.makeText(context, "请在设置中允许\"推荐通知\"以开启超级岛", Toast.LENGTH_LONG).show()
                }
            }

            if (isChargingState.value) {
                val intent = Intent(context, SuperIslandService::class.java)
                ContextCompat.startForegroundService(context, intent)
            }
        } else {
            context.stopService(Intent(context, SuperIslandService::class.java))
        }
    }

    // 使用广播监听充电状态变化，替代轮询
    DisposableEffect(switch16) {
        var isRegistered = false
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val nowCharging = intent.action == Intent.ACTION_POWER_CONNECTED
                isChargingState.value = nowCharging
                if (switch16) {
                    val serviceIntent = Intent(context, SuperIslandService::class.java)
                    if (nowCharging) {
                        ContextCompat.startForegroundService(context, serviceIntent)
                    } else {
                        context.stopService(serviceIntent)
                    }
                }
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

    // ── 配置同步：顺序执行避免并行 su 调用，config 传给 SpeedControlSection 避免重复读取 ──
    var externalConfig by rememberSaveable { mutableStateOf<Map<String, String>>(emptyMap()) }
    LaunchedEffect(reloadTrigger) {
        // 顺序执行避免并行 su 调用
        moduleInstalled = ModuleDetector.isModuleInstalled()
        moduleVersion = ModuleDetector.getModuleVersion()
        hasUpdate = ModuleDetector.checkUpdate()
        val config = ModuleDetector.readConfig()
        externalConfig = config

        val editor = prefs.edit()
        fun syncSwitch(key: String, currentVal: Boolean, update: (Boolean) -> Unit, prefKey: String) {
            val fileVal = config[key]?.trim()?.lowercase() == "true"
            if (currentVal != fileVal) { update(fileVal); editor.putBoolean(prefKey, fileVal) }
        }
        fun syncSwitchPrefOnly(configKey: String, prefKey: String) {
            val fileVal = config[configKey]?.trim()?.lowercase() == "true"
            editor.putBoolean(prefKey, fileVal)
        }
        syncSwitch("快充模式", switch2, { switch2 = it }, "switch2")
        syncSwitch("温控空挂载模式", switch3, { switch3 = it }, "switch3")
        syncSwitch("模块简介显示充电信息", switch5, { switch5 = it }, "switch5")
        syncSwitch("还原均衡模式温控", switch15, { switch15 = it }, "switch15")
        syncSwitch("还原性能模式温控", switch6, { switch6 = it }, "switch6")
        syncSwitch("游戏均衡式性能温控", switch7, { switch7 = it }, "switch7")
        syncSwitch("系统均衡式性能温控", switch14, { switch14 = it }, "switch14")
        syncSwitch("分应用调速", switch8, { switch8 = it }, "switch8")
        syncSwitch("无温控应用", switch9, { switch9 = it }, "switch9")
        syncSwitch("分应用旁路", switch17, { switch17 = it }, "switch17")
        syncSwitch("温度旁路", switch18, { switch18 = it }, "switch18")
        syncSwitch("修改最大电流数", switch4, { switch4 = it }, "switch4")
        syncSwitch("充电调速", switch11, { switch11 = it }, "switch11")
        syncSwitch("亮息屏调速", switch10, { switch10 = it }, "switch10")
        syncSwitch("当电流低于阈值执行停充", switch12, { switch12 = it }, "switch12")
        syncSwitch("自定义阶梯模式", switch13, { switch13 = it }, "switch13")
        // 分应用旁路温度滑块
        config["分应用旁路到达温度"]?.trim()?.let { v ->
            if (appBypassTargetTemp != v) { appBypassTargetTemp = v; editor.putString("appBypassTargetTemp", v) }
        }
        config["分应用旁路恢复温度"]?.trim()?.let { v ->
            if (appBypassRecoveryTemp != v) { appBypassRecoveryTemp = v; editor.putString("appBypassRecoveryTemp", v) }
        }
        config["分应用旁路最大电流数"]?.trim()?.let { v ->
            if (appBypassMaxCurrent != v) { appBypassMaxCurrent = v; editor.putString("appBypassMaxCurrent", v) }
        }
        config["分应用旁路停充电量"]?.trim()?.let { v ->
            if (appBypassStopLevel != v) { appBypassStopLevel = v; editor.putString("appBypassStopLevel", v) }
        }
        // 全局旁路温度滑块
        config["全局旁路到达温度"]?.trim()?.let { v ->
            if (globalBypassTargetTemp != v) { globalBypassTargetTemp = v; editor.putString("globalBypassTargetTemp", v) }
        }
        config["全局旁路恢复温度"]?.trim()?.let { v ->
            if (globalBypassRecoveryTemp != v) { globalBypassRecoveryTemp = v; editor.putString("globalBypassRecoveryTemp", v) }
        }
        config["全局旁路最大电流数"]?.trim()?.let { v ->
            if (globalBypassMaxCurrent != v) { globalBypassMaxCurrent = v; editor.putString("globalBypassMaxCurrent", v) }
        }
        config["全局旁路停充电量"]?.trim()?.let { v ->
            if (globalBypassStopLevel != v) { globalBypassStopLevel = v; editor.putString("globalBypassStopLevel", v) }
        }
        editor.apply()
    }

    val updateSwitch = remember(scope, prefs) { { prefKey: String, configKey: String, newValue: Boolean, setter: (Boolean) -> Unit ->
        setter(newValue); prefs.edit().putBoolean(prefKey, newValue).apply()
        scope.launch { ModuleDetector.updateConfig(configKey, newValue); ModuleDetector.executeThermalScript() }
        Unit
    } }
    val updateText = remember(scope, prefs) { { prefKey: String, configKey: String, newValue: String, setter: (String) -> Unit ->
        setter(newValue); prefs.edit().putString(prefKey, newValue).apply()
        scope.launch { ModuleDetector.updateConfig(configKey, newValue) }
        Unit
    } }

    // ── 旁路充电配置写入 /data/adb/modules/SetoSkins/旁路充电配置.prop ──
    val writeBypassConfigFile = remember(scope) { {
        val config = buildMap {
            put("分应用旁路", switch17.toString())
            put("应用到达温度", appBypassTargetTemp)
            put("应用恢复温度", appBypassRecoveryTemp)
            put("分应用最大电流数", appBypassMaxCurrent)
            put("分应用停充电量", appBypassStopLevel)
            put("全局旁路", switch18.toString())
            put("全局到达温度", globalBypassTargetTemp)
            put("全局恢复温度", globalBypassRecoveryTemp)
            put("全局最大电流数", globalBypassMaxCurrent)
            put("全局停充电量", globalBypassStopLevel)
        }
        scope.launch { ModuleDetector.writeBypassConfig(config) }
    } }

    // ── 旁路滑块值变化时 300ms 防抖写入配置 ──
    LaunchedEffect(appBypassTargetTemp, appBypassRecoveryTemp, appBypassMaxCurrent, appBypassStopLevel) {
        kotlinx.coroutines.delay(300)
        writeBypassConfigFile()
    }
    LaunchedEffect(globalBypassTargetTemp, globalBypassRecoveryTemp, globalBypassMaxCurrent, globalBypassStopLevel) {
        kotlinx.coroutines.delay(300)
        writeBypassConfigFile()
    }

    // ── 九开关互斥：调速区 + 旁路充电区，任一个打开时关闭其余八个 ──
    val homeExclusiveActive = switch17 || switch18
    val speedExclusiveActive = switch4 || switch10 || switch11 || switch12 || switch13 || switch8 || switch9
    val turnOffAllOtherExclusive: (String) -> Unit = { exceptKey ->
        if (exceptKey != "switch4" && switch4) updateSwitch("switch4", "修改最大电流数", false) { switch4 = false }
        if (exceptKey != "switch10" && switch10) updateSwitch("switch10", "亮息屏调速", false) { switch10 = false }
        if (exceptKey != "switch11" && switch11) updateSwitch("switch11", "充电调速", false) { switch11 = false }
        if (exceptKey != "switch12" && switch12) updateSwitch("switch12", "当电流低于阈值执行停充", false) { switch12 = false }
        if (exceptKey != "switch13" && switch13) updateSwitch("switch13", "自定义阶梯模式", false) { switch13 = false }
        if (exceptKey != "switch8" && switch8) updateSwitch("switch8", "分应用调速", false) { switch8 = false }
        if (exceptKey != "switch9" && switch9) updateSwitch("switch9", "无温控应用", false) { switch9 = false }
        if (exceptKey != "switch17" && switch17) updateSwitch("switch17", "分应用旁路", false) { switch17 = false }
        if (exceptKey != "switch18" && switch18) updateSwitch("switch18", "全局旁路", false) { switch18 = false }
    }

    LazyColumn(
            modifier = modifier.fillMaxSize().then(if (scrollBehavior != null) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else Modifier),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = contentPaddingTop, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
        item(key = "status_card") {
            if (!moduleInstalled) RedNotInstalledCard()
            else if (hasUpdate) YellowUpdateCard()
            else GreenActivatedCard(useMonet = useMonet, version = moduleVersion)
        }
        item(key = "config_title") {
            SmallTitle(text = "配置", modifier = Modifier.offset(x = (-12).dp).layout { measurable, constraints -> val placeable = measurable.measure(constraints); layout(placeable.width, placeable.height - 10.dp.roundToPx()) { placeable.place(0, 0) } })
        }
        item(key = "config_card") {
            MiuixCard {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    BasicComponent(title = "模块简介显示充电信息", summary = "Magisk/KSU里显示电流、电量等充电信息,可能耗一丢丢电", endActions = { ThemedSwitch(checked = switch5, onCheckedChange = null, useMonet = useMonet) }, onClick = { updateSwitch("switch5", "模块简介显示充电信息", !switch5) { switch5 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                    BasicComponent(title = "充电时显示灵动岛", summary = "充电时在屏幕显示灵动岛风格充电信息（需后台运行）", endActions = { ThemedSwitch(checked = switch16, onCheckedChange = null, useMonet = useMonet) }, onClick = { switch16 = !switch16; prefs.edit().putBoolean("switch16", switch16).apply(); hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                }
            }
        }
        item(key = "thermal_title") {
            Spacer(modifier = Modifier.height(12.dp))
            SmallTitle(text = "温控", modifier = Modifier.padding(start = 4.dp).offset(x = (-13).dp, y = (11).dp).layout { measurable, constraints -> val placeable = measurable.measure(constraints); val reduce = 24.dp.roundToPx(); layout(placeable.width, (placeable.height - reduce).coerceAtLeast(1)) { placeable.place(0, -reduce) } })
        }
        item(key = "thermal_card") {
            MiuixCard {
                BasicComponent(title = "快充模式", endActions = { Row(verticalAlignment = Alignment.CenterVertically) { Text(text = if (switch2) "True" else "False", fontSize = 17.sp, color = if (switch2) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceSecondary); Spacer(modifier = Modifier.width(8.dp)); ThemedSwitch(checked = switch2, onCheckedChange = null, useMonet = useMonet) } }, onClick = { updateSwitch("switch2", "快充模式", !switch2) { switch2 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    BasicComponent(title = "温控空挂载模式", summary = "非必要建议不开启此选项", endActions = { ThemedSwitch(checked = switch3, onCheckedChange = null, useMonet = useMonet) }, onClick = { updateSwitch("switch3", "温控空挂载模式", !switch3) { switch3 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                    BasicComponent(title = "还原均衡模式温控", enabled = switch15 || !switch6, endActions = { ThemedSwitch(checked = switch15, onCheckedChange = null, enabled = switch15 || !switch6, useMonet = useMonet) }, onClick = { if (switch15 || !switch6) { updateSwitch("switch15", "还原均衡模式温控", !switch15) { switch15 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) } })
                    BasicComponent(title = "还原性能模式温控", enabled = switch6 || !switch15, endActions = { ThemedSwitch(checked = switch6, onCheckedChange = null, enabled = switch6 || !switch15, useMonet = useMonet) }, onClick = { if (switch6 || !switch15) { updateSwitch("switch6", "还原性能模式温控", !switch6) { switch6 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) } })
                    BasicComponent(title = "游戏均衡式性能温控", summary = "把游戏中均衡模式的温控改成性能模式的原有温控,性能模式则无温控", endActions = { ThemedSwitch(checked = switch7, onCheckedChange = null, useMonet = useMonet) }, onClick = { updateSwitch("switch7", "游戏均衡式性能温控", !switch7) { switch7 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                    BasicComponent(title = "系统均衡式性能温控", summary = "把系统中均衡模式的温控改成性能模式的原有温控,性能模式则无温控", endActions = { ThemedSwitch(checked = switch14, onCheckedChange = null, useMonet = useMonet) }, onClick = { updateSwitch("switch14", "系统均衡式性能温控", !switch14) { switch14 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                }
            }
        }
        item(key = "speed_title") {
            Spacer(modifier = Modifier.height(12.dp))
            SmallTitle(text = "调速 (22000mA＝22000000μA)(重启生效)", modifier = Modifier.compactSmallTitle())
        }
        item(key = "speed_tip") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                cornerRadius = 16.dp,
                colors = CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceContainer,
                    contentColor = MiuixTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = if (isZh) "Tip：部分机型可能因电池节点差异无法使用调速或旁路充电功能，请自行测试。" else "Tip：Speed control may not work on some devices due to battery node differences. Please test yourself.",
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }

        item(key = "speed_control") {
            SpeedControlSection(
                useMonet = useMonet,
                prefs = prefs,
                externalConfig = externalConfig,
                updateSwitch = updateSwitch,
                updateText = updateText,
                onShowBlacklist = onNavigateToBlacklist,
                onShowWhitelist = onNavigateToWhitelist,
                switch4 = switch4,
                switch10 = switch10,
                switch11 = switch11,
                switch12 = switch12,
                switch13 = switch13,
                switch8 = switch8,
                switch9 = switch9,
                homeExclusiveActive = homeExclusiveActive,
                onToggleSwitch4 = { newVal ->
                    if (newVal) turnOffAllOtherExclusive("switch4")
                    updateSwitch("switch4", "修改最大电流数", newVal) { switch4 = newVal }
                },
                onToggleSwitch10 = { newVal ->
                    if (newVal) turnOffAllOtherExclusive("switch10")
                    updateSwitch("switch10", "亮息屏调速", newVal) { switch10 = newVal }
                },
                onToggleSwitch11 = { newVal ->
                    if (newVal) turnOffAllOtherExclusive("switch11")
                    updateSwitch("switch11", "充电调速", newVal) { switch11 = newVal }
                },
                onToggleSwitch12 = { newVal ->
                    if (newVal) turnOffAllOtherExclusive("switch12")
                    updateSwitch("switch12", "当电流低于阈值执行停充", newVal) { switch12 = newVal }
                },
                onToggleSwitch13 = { newVal ->
                    if (newVal) turnOffAllOtherExclusive("switch13")
                    updateSwitch("switch13", "自定义阶梯模式", newVal) { switch13 = newVal }
                },
                onToggleSwitch8 = { newVal ->
                    if (newVal) turnOffAllOtherExclusive("switch8")
                    updateSwitch("switch8", "分应用调速", newVal) { switch8 = newVal }
                },
                onToggleSwitch9 = { newVal ->
                    if (newVal) turnOffAllOtherExclusive("switch9")
                    updateSwitch("switch9", "无温控应用", newVal) { switch9 = newVal }
                }
            )
        }

        item(key = "bypass_charge_title") {
            Spacer(modifier = Modifier.height(12.dp))
            SmallTitle(text = "旁路充电", modifier = Modifier.padding(start = 4.dp).offset(x = (-13).dp, y = (11).dp).layout { measurable, constraints -> val placeable = measurable.measure(constraints); val reduce = 24.dp.roundToPx(); layout(placeable.width, (placeable.height - reduce).coerceAtLeast(1)) { placeable.place(0, -reduce) } })
        }

        item(key = "bypass_charge_card") {
            MiuixCard {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    BasicComponent(
                        title = "全局旁路",
                        enabled = switch18 || !speedExclusiveActive,
                        endActions = { ThemedSwitch(checked = switch18, onCheckedChange = null, enabled = switch18 || !speedExclusiveActive, useMonet = useMonet) },
                        onClick = {
                            if (switch18 || !speedExclusiveActive) {
                                val newVal = !switch18
                                if (newVal) {
                                    turnOffAllOtherExclusive("switch18")
                                    if (switch17) updateSwitch("switch17", "分应用旁路", false) { switch17 = false }
                                }
                                updateSwitch("switch18", "全局旁路", newVal) { switch18 = newVal }
                                writeBypassConfigFile()
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    )
                    AnimatedVisibility(visible = switch18, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        Column {
                            key("global_bypass_target_temp") {
                                SliderRow(
                                    title = "全局到达温度",
                                    value = globalBypassTargetTemp.toFloatOrNull() ?: 40f,
                                    onValueChange = {
                                        globalBypassTargetTemp = it.toInt().toString()
                                        updateText("globalBypassTargetTemp", "全局旁路到达温度", globalBypassTargetTemp) {}
                                    },
                                    valueRange = 20f..50f, steps = 30, suffix = "°C",
                                    onClickLabel = {
                                        dialogState = DialogState(
                                            "调整全局到达温度",
                                            "输入温度 (20-50 °C)",
                                            globalBypassTargetTemp,
                                            20..50
                                        ) { v ->
                                            globalBypassTargetTemp = v.toString()
                                        }
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                            }
                            key("global_bypass_recovery_temp") {
                                SliderRow(
                                    title = "全局恢复温度",
                                    value = globalBypassRecoveryTemp.toFloatOrNull() ?: 35f,
                                    onValueChange = {
                                        globalBypassRecoveryTemp = it.toInt().toString()
                                        updateText("globalBypassRecoveryTemp", "全局旁路恢复温度", globalBypassRecoveryTemp) {}
                                    },
                                    valueRange = 20f..50f, steps = 30, suffix = "°C",
                                    onClickLabel = {
                                        dialogState = DialogState("调整全局恢复温度", "输入温度 (20-50 °C)", globalBypassRecoveryTemp, 20..50) { v ->
                                            globalBypassRecoveryTemp = v.toString()
                                        }
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                            }
                            key("global_bypass_max_current") {
                                ThemedTextField(
                                    value = globalBypassMaxCurrent,
                                    onValueChange = {
                                        globalBypassMaxCurrent = it
                                        updateText("globalBypassMaxCurrent", "全局旁路最大电流数", it) {}
                                    },
                                    label = "22A＝22000mA＝22000000",
                                    useMonet = useMonet,
                                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                                    inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } }
                                )
                            }
                            key("global_bypass_stop_level") {
                                ThemedTextField(
                                    value = globalBypassStopLevel,
                                    onValueChange = {
                                        globalBypassStopLevel = it
                                        updateText("globalBypassStopLevel", "全局旁路停充电量", it) {}
                                    },
                                    label = "电量达到以下时关闭旁路充电",
                                    useMonet = useMonet,
                                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                                    inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } }
                                )
                            }
                        }
                    }
                    BasicComponent(
                        title = "分应用旁路",
                        enabled = switch17 || !speedExclusiveActive,
                        endActions = { ThemedSwitch(checked = switch17, onCheckedChange = null, enabled = switch17 || !speedExclusiveActive, useMonet = useMonet) },
                        onClick = {
                            if (switch17 || !speedExclusiveActive) {
                                val newVal = !switch17
                                if (newVal) {
                                    turnOffAllOtherExclusive("switch17")
                                    if (switch18) updateSwitch("switch18", "全局旁路", false) { switch18 = false }
                                }
                                updateSwitch("switch17", "分应用旁路", newVal) { switch17 = newVal }
                                writeBypassConfigFile()
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    )
                    AnimatedVisibility(visible = switch17, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        Column {
                            key("app_bypass_target_temp") {
                                SliderRow(
                                    title = "应用到达温度",
                                    value = appBypassTargetTemp.toFloatOrNull() ?: 40f,
                                    onValueChange = {
                                        appBypassTargetTemp = it.toInt().toString()
                                        updateText("appBypassTargetTemp", "分应用旁路到达温度", appBypassTargetTemp) {}
                                    },
                                    valueRange = 20f..50f, steps = 30, suffix = "°C",
                                    onClickLabel = {
                                        dialogState = DialogState("调整应用到达温度", "输入温度 (20-50 °C)", appBypassTargetTemp, 20..50) { v ->
                                            appBypassTargetTemp = v.toString()
                                        }
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                            }
                            key("app_bypass_recovery_temp") {
                                SliderRow(
                                    title = "应用恢复温度",
                                    value = appBypassRecoveryTemp.toFloatOrNull() ?: 35f,
                                    onValueChange = {
                                        appBypassRecoveryTemp = it.toInt().toString()
                                        updateText("appBypassRecoveryTemp", "分应用旁路恢复温度", appBypassRecoveryTemp) {}
                                    },
                                    valueRange = 20f..50f, steps = 30, suffix = "°C",
                                    onClickLabel = {
                                        dialogState = DialogState("调整应用恢复温度", "输入温度 (20-50 °C)", appBypassRecoveryTemp, 20..50) { v ->
                                            appBypassRecoveryTemp = v.toString()
                                        }
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                            }
                            key("app_bypass_max_current") {
                                ThemedTextField(
                                    value = appBypassMaxCurrent,
                                    onValueChange = {
                                        appBypassMaxCurrent = it
                                        updateText("appBypassMaxCurrent", "分应用旁路最大电流数", it) {}
                                    },
                                    label = "22A＝22000mA＝22000000",
                                    useMonet = useMonet,
                                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                                    inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } }
                                )
                            }
                            key("app_bypass_stop_level") {
                                ThemedTextField(
                                    value = appBypassStopLevel,
                                    onValueChange = {
                                        appBypassStopLevel = it
                                        updateText("appBypassStopLevel", "分应用旁路停充电量", it) {}
                                    },
                                    label = "电量达到以下时关闭旁路充电",
                                    useMonet = useMonet,
                                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                                    inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } }
                                )
                            }
                            key("app_bypass_list") {
                                BasicComponent(
                                    title = "旁路充电名单",
                                    onClick = {
                                        onNavigateToBypassList()
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

    }

    // ── 滑块数值输入对话框 ──
    val state = dialogState
    if (state != null) {
        ConfigDialog(
            show = true,
            title = state.title,
            summary = state.summary,
            initialValue = state.initialValue,
            validationRange = state.range,
            isZh = isZh,
            onConfirm = state.onConfirm,
            onDismiss = { dialogState = null }
        )
    }

}