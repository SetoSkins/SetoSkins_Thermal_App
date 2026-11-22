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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.setoskins.thermal.data.BatteryMonitor
import com.setoskins.thermal.data.ConfigWriter
import com.setoskins.thermal.data.ModuleDetector
import com.setoskins.thermal.service.SuperIslandService
import com.setoskins.thermal.ui.component.ConfigDialog
import com.setoskins.thermal.ui.component.GreenActivatedCard
import com.setoskins.thermal.ui.component.MiuixCard
import com.setoskins.thermal.ui.component.ThemedOptionCard
import com.setoskins.thermal.ui.component.MaterialSegmentedColumn
import com.setoskins.thermal.ui.component.MaterialSwitchItem
import com.setoskins.thermal.ui.component.MaterialListItem
import com.setoskins.thermal.ui.component.MaterialChevron
import com.setoskins.thermal.ui.component.IconTile
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Apps
import com.setoskins.thermal.ui.component.RedNotInstalledCard
import com.setoskins.thermal.ui.component.SliderRow
import com.setoskins.thermal.ui.component.ThemedSwitch
import com.setoskins.thermal.ui.component.ThemedTextField
import com.setoskins.thermal.ui.component.UpdateWarningCard
import com.setoskins.thermal.ui.component.miuixSmallTitle
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import androidx.compose.ui.Alignment
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(
    ExperimentalMaterial3Api::class,
    top.yukonga.miuix.kmp.interfaces.ExperimentalScrollBarApi::class,
    kotlinx.coroutines.FlowPreview::class,
)
@Composable
fun HomeScreen(
    useMonet: Boolean,
    reloadTrigger: Int = 0,
    scrollBehavior: ScrollBehavior? = null,
    materialScrollConnection: NestedScrollConnection? = null,
    contentPaddingTop: Dp = 0.dp,
    bottomContentInset: Dp = 0.dp,
    floatingNavBar: Boolean = false,
    onNavigateToBlacklist: () -> Unit = {},
    onNavigateToWhitelist: () -> Unit = {},
    onNavigateToBypassList: () -> Unit = {},
    onDialogVisibilityChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    val tipIndex = remember { kotlin.random.Random.nextInt(4) }

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
    var switch19 by rememberSaveable { mutableStateOf(prefs.getBoolean("switch19", false)) }
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
    var dialogDismissStarted by remember { mutableStateOf(false) }
    LaunchedEffect(dialogState) {
        if (dialogState != null) {
            dialogDismissStarted = false
            onDialogVisibilityChange(true)
        } else if (!dialogDismissStarted) {
            onDialogVisibilityChange(false)
        }
    }
    val isChargingState = rememberSaveable { mutableStateOf(false) }

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
        isChargingState.value = withContext(kotlinx.coroutines.Dispatchers.IO) {
            BatteryMonitor.getBatteryState(context).isCharging
        }
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
                    Toast.makeText(context, if (isZh) "请在设置中允许\"推荐通知\"以开启超级岛" else "Please allow \"Promoted notifications\" in Settings to enable the charging island", Toast.LENGTH_LONG).show()
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

    var externalConfig by rememberSaveable { mutableStateOf<Map<String, String>>(emptyMap()) }
    LaunchedEffect(reloadTrigger) {
        val snapshot = ModuleDetector.readModuleSnapshot()
        moduleInstalled = snapshot.installed
        moduleVersion = snapshot.version
        val config = snapshot.config
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
        syncSwitch("温控空挂载模式", switch3, { switch3 = it }, "switch3")
        syncSwitch("模块简介显示充电信息", switch5, { switch5 = it }, "switch5")
        syncSwitch("还原均衡模式温控", switch15, { switch15 = it }, "switch15")
        syncSwitch("还原性能模式温控", switch6, { switch6 = it }, "switch6")
        syncSwitch("游戏均衡式性能温控", switch7, { switch7 = it }, "switch7")
        syncSwitch("系统均衡式性能温控", switch14, { switch14 = it }, "switch14")
        syncSwitch("分应用调速", switch8, { switch8 = it }, "switch8")
        syncSwitch("无温控应用", switch9, { switch9 = it }, "switch9")
        syncSwitch("分应用旁路", switch17, { switch17 = it }, "switch17")
        syncSwitch("全局旁路", switch18, { switch18 = it }, "switch18")
        syncSwitch("默认温控强制高刷", switch19, { switch19 = it }, "switch19")
        syncSwitch("修改最大电流数", switch4, { switch4 = it }, "switch4")
        syncSwitch("充电调速", switch11, { switch11 = it }, "switch11")
        syncSwitch("亮息屏调速", switch10, { switch10 = it }, "switch10")
        syncSwitch("当电流低于阈值执行停充", switch12, { switch12 = it }, "switch12")
        syncSwitch("自定义阶梯模式", switch13, { switch13 = it }, "switch13")

        if (switch17 && switch18) {
            switch17 = false
            editor.putBoolean("switch17", false)
            ModuleDetector.updateConfig("分应用旁路", false)
        }

        if (switch3) {
            if (switch6) { switch6 = false; editor.putBoolean("switch6", false); ModuleDetector.updateConfig("还原性能模式温控", false) }
            if (switch7) { switch7 = false; editor.putBoolean("switch7", false); ModuleDetector.updateConfig("游戏均衡式性能温控", false) }
            if (switch14) { switch14 = false; editor.putBoolean("switch14", false); ModuleDetector.updateConfig("系统均衡式性能温控", false) }
            if (switch15) { switch15 = false; editor.putBoolean("switch15", false); ModuleDetector.updateConfig("还原均衡模式温控", false) }
        } else if (switch14) {
            if (switch6) { switch6 = false; editor.putBoolean("switch6", false); ModuleDetector.updateConfig("还原性能模式温控", false) }
            if (switch7) { switch7 = false; editor.putBoolean("switch7", false); ModuleDetector.updateConfig("游戏均衡式性能温控", false) }
            if (switch15) { switch15 = false; editor.putBoolean("switch15", false); ModuleDetector.updateConfig("还原均衡模式温控", false) }
        } else if (switch6 && switch15) {
            switch6 = false
            editor.putBoolean("switch6", false)
            ModuleDetector.updateConfig("还原性能模式温控", false)
        }

        if (!switch6 && !switch7 && !switch14 && !switch15 && switch19) {
            switch19 = false
            editor.putBoolean("switch19", false)
            ModuleDetector.updateConfig("默认温控强制高刷", false)
        }

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

        hasUpdate = ModuleDetector.checkUpdate(snapshot.localVersionCode)
    }

    val updateSwitch = remember(scope, prefs) { { prefKey: String, configKey: String, newValue: Boolean, setter: (Boolean) -> Unit ->
        setter(newValue); prefs.edit().putBoolean(prefKey, newValue).apply()
        scope.launch { ModuleDetector.updateConfig(configKey, newValue); ModuleDetector.executeThermalScript() }
        Unit
    } }
    val updateText = remember(scope, prefs) { { prefKey: String, configKey: String, newValue: String, setter: (String) -> Unit ->
        setter(newValue); prefs.edit().putString(prefKey, newValue).apply()

        ConfigWriter.submit(configKey, newValue)
        Unit
    } }

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
        ConfigWriter.submitAll(
            mapOf(
                "分应用旁路到达温度" to appBypassTargetTemp,
                "分应用旁路恢复温度" to appBypassRecoveryTemp,
                "分应用旁路最大电流数" to appBypassMaxCurrent,
                "分应用旁路停充电量" to appBypassStopLevel,
                "全局旁路到达温度" to globalBypassTargetTemp,
                "全局旁路恢复温度" to globalBypassRecoveryTemp,
                "全局旁路最大电流数" to globalBypassMaxCurrent,
                "全局旁路停充电量" to globalBypassStopLevel,
            )
        )
        scope.launch { ModuleDetector.writeBypassConfig(config) }
    } }

    LaunchedEffect(Unit) {
        snapshotFlow {
            listOf(
                appBypassTargetTemp, appBypassRecoveryTemp, appBypassMaxCurrent, appBypassStopLevel,
                globalBypassTargetTemp, globalBypassRecoveryTemp, globalBypassMaxCurrent, globalBypassStopLevel,
                switch17, switch18,
            )
        }
            .distinctUntilChanged()
            .drop(1)
            .debounce(400)
            .collect { writeBypassConfigFile() }
    }

    val exclusives = remember {
        derivedStateOf {
            Exclusives(
                home = switch17 || switch18,
                speed = switch4 || switch10 || switch11 || switch12 || switch13 || switch8 || switch9,
            )
        }
    }
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

    val turnOffOtherThermalModes: (String) -> Unit = { exceptKey ->
        val keepGame = exceptKey == "switch7" || exceptKey == "switch6" || exceptKey == "switch15"
        if (exceptKey != "switch3" && switch3) updateSwitch("switch3", "温控空挂载模式", false) { switch3 = false }
        if (exceptKey != "switch6" && exceptKey != "switch7" && switch6) updateSwitch("switch6", "还原性能模式温控", false) { switch6 = false }
        if (exceptKey != "switch7" && !keepGame && switch7) updateSwitch("switch7", "游戏均衡式性能温控", false) { switch7 = false }
        if (exceptKey != "switch14" && switch14) updateSwitch("switch14", "系统均衡式性能温控", false) { switch14 = false }
        if (exceptKey != "switch15" && exceptKey != "switch7" && switch15) updateSwitch("switch15", "还原均衡模式温控", false) { switch15 = false }
    }

    val listState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
                modifier = Modifier.fillMaxSize()
                    .then(if (!useMonet) Modifier.scrollEndHaptic() else Modifier)
                    .then(if (scrollBehavior != null) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else Modifier)

                    .then(if (materialScrollConnection != null) Modifier.nestedScroll(materialScrollConnection) else Modifier),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = contentPaddingTop, bottom = 46.dp + bottomContentInset),
                // miuix 卡片之间是 12dp；SmallTitle 的上 8dp 叠加后正好是 20dp
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
        item(key = "status_card") {
            val cardMode = when {
                !moduleInstalled -> 0
                hasUpdate -> 1
                else -> 2
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                if (moduleInstalled) {
                    ThemedOptionCard(
                        useMonet = useMonet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                    ) {
                        Text(
                            text = if (isZh) {
                                when (tipIndex) {
                                    0 -> "Tip：部分机型可能因电池节点差异无法使用调速或旁路充电功能，请自行测试。"
                                    1 -> "Tip：更新温控模块会让所有配置选项重置，注意！"
                                    2 -> "Tip：默认温控强制高刷开关，修改后需要重启才能生效。"
                                    3 -> "22A＝22000mA＝22000000μA"
                                    else -> "Tip：充电宝或充电器中显示的瓦数和日志的瓦数不一样为正常现象。两者出现差异，主要是因为电压不同、转换损耗以及测量位置不同。"
                                }
                            } else {
                                when (tipIndex) {
                                    0 -> "Tip: Speed control or bypass charging may not work on some devices due to battery node differences. Please test on your own."
                                    1 -> "Tip: Updating the thermal module will reset all configuration options. Note!"
                                    2 -> "Tip: Changes to the 'Force high refresh rate on default thermal' switch take effect only after a reboot."
                                    3 -> "22A＝22000mA＝22000000μA"
                                    else -> "Tip: The wattage shown on a power bank or charger differs from the logged wattage, which is normal. The difference is mainly caused by different voltages, conversion losses, and different measurement points."
                                }
                            },
                            fontSize = 13.sp,
                            color = if (useMonet) MaterialTheme.colorScheme.onSurfaceVariant else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
                when (cardMode) {
                    0 -> RedNotInstalledCard(useMonet = useMonet)
                    1 -> {
                        UpdateWarningCard(useMonet = useMonet)
                        GreenActivatedCard(useMonet = useMonet, version = moduleVersion)
                    }
                    else -> GreenActivatedCard(useMonet = useMonet, version = moduleVersion)
                }
            }
        }
        item(key = "config_title") {
            SmallTitle(text = if (isZh) "配置" else "Configuration", modifier = Modifier.miuixSmallTitle())
        }
        item(key = "config_card") {
            if (useMonet) {
                MaterialSegmentedColumn(
                    items = listOf(
                        {
                            MaterialSwitchItem(
                                headline = if (isZh) "模块简介显示充电信息" else "Show charging info in module description",
                                supporting = if (isZh) "Magisk/KSU里显示电流、电量等充电信息,可能耗一丢丢电" else "Show current, battery level and other charging info in Magisk/KSU, may use a little extra power",
                                checked = switch5,
                                onCheckedChange = { newVal ->
                                    updateSwitch("switch5", "模块简介显示充电信息", newVal) { switch5 = newVal }
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                leading = { IconTile(Icons.Filled.Description, contentDescription = null) }
                            )
                        },
                        {
                            MaterialSwitchItem(
                                headline = if (isZh) "充电时显示灵动岛" else "Show charging island while charging",
                                supporting = if (isZh) "充电时在屏幕显示灵动岛风格充电信息（需后台运行）" else "Show island-style charging info on screen while charging (requires running in background)",
                                checked = switch16,
                                onCheckedChange = { newVal ->
                                    switch16 = newVal
                                    prefs.edit().putBoolean("switch16", switch16).apply()
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                leading = { IconTile(Icons.Filled.Widgets, contentDescription = null) }
                            )
                        }
                    )
                )
            } else {
                MiuixCard {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        BasicComponent(title = if (isZh) "模块简介显示充电信息" else "Show charging info in module description", summary = if (isZh) "Magisk/KSU里显示电流、电量等充电信息,可能耗一丢丢电" else "Show current, battery level and other charging info in Magisk/KSU, may use a little extra power", endActions = { ThemedSwitch(checked = switch5, onCheckedChange = null, useMonet = useMonet) }, onClick = { updateSwitch("switch5", "模块简介显示充电信息", !switch5) { switch5 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                        BasicComponent(title = if (isZh) "充电时显示灵动岛" else "Show charging island while charging", summary = if (isZh) "充电时在屏幕显示灵动岛风格充电信息（需后台运行）" else "Show island-style charging info on screen while charging (requires running in background)", endActions = { ThemedSwitch(checked = switch16, onCheckedChange = null, useMonet = useMonet) }, onClick = { switch16 = !switch16; prefs.edit().putBoolean("switch16", switch16).apply(); hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                    }
                }
            }
        }
        item(key = "thermal_title") {
            SmallTitle(text = if (isZh) "温控" else "Thermal control", modifier = Modifier.miuixSmallTitle())
        }
        item(key = "thermal_card") {
            if (useMonet) {
                MaterialSegmentedColumn(
                    items = buildList<@Composable () -> Unit> {
                        add {
                            MaterialSwitchItem(
                                headline = if (isZh) "温控空挂载模式" else "Empty-mount thermal mode",
                                supporting = if (isZh) "非必要建议不开启此选项" else "Not recommended unless necessary",
                                enabled = !switch6 && !switch7 && !switch14 && !switch15,
                                checked = switch3,
                                onCheckedChange = { newVal ->
                                    if (newVal) turnOffOtherThermalModes("switch3")
                                    updateSwitch("switch3", "温控空挂载模式", newVal) { switch3 = newVal }
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                leading = { IconTile(Icons.Filled.Layers, contentDescription = null) }
                            )
                        }
                        add {
                            Column {
                                MaterialSwitchItem(
                                    headline = if (isZh) "还原均衡模式温控" else "Restore Balanced mode thermal",
                                    enabled = !switch3 && !switch6 && !switch14,
                                    checked = switch15,
                                    onCheckedChange = { newVal ->
                                        if (newVal) turnOffOtherThermalModes("switch15")
                                        updateSwitch("switch15", "还原均衡模式温控", newVal) { switch15 = newVal }
                                        if (!newVal && !switch6 && !switch7 && !switch14 && switch19) updateSwitch("switch19", "默认温控强制高刷", false) { switch19 = false }
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    leading = { IconTile(Icons.Filled.Balance, contentDescription = null) }
                                )
                                AnimatedVisibility(
                                    visible = switch15 && !switch7,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    MaterialSwitchItem(
                                        headline = if (isZh) "默认温控强制高刷" else "Force high refresh rate on default thermal",
                                        supporting = if (isZh) "解除高温影响刷新率限制" else "Remove the high-temperature refresh rate limit",
                                        checked = switch19,
                                        onCheckedChange = { newVal ->
                                            updateSwitch("switch19", "默认温控强制高刷", newVal) { switch19 = newVal }
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        leading = { IconTile(Icons.Filled.Speed, contentDescription = null) }
                                    )
                                }
                            }
                        }
                        add {
                            Column {
                                MaterialSwitchItem(
                                    headline = if (isZh) "还原性能模式温控" else "Restore Performance mode thermal",
                                    enabled = !switch3 && !switch14 && !switch15,
                                    checked = switch6,
                                    onCheckedChange = { newVal ->
                                        if (newVal) turnOffOtherThermalModes("switch6")
                                        updateSwitch("switch6", "还原性能模式温控", newVal) { switch6 = newVal }
                                        if (!newVal && !switch7 && !switch14 && !switch15 && switch19) updateSwitch("switch19", "默认温控强制高刷", false) { switch19 = false }
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    leading = { IconTile(Icons.Filled.Bolt, contentDescription = null) }
                                )
                                AnimatedVisibility(
                                    visible = switch6 && !switch7,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    MaterialSwitchItem(
                                        headline = if (isZh) "默认温控强制高刷" else "Force high refresh rate on default thermal",
                                        supporting = if (isZh) "解除高温影响刷新率限制" else "Remove the high-temperature refresh rate limit",
                                        checked = switch19,
                                        onCheckedChange = { newVal ->
                                            updateSwitch("switch19", "默认温控强制高刷", newVal) { switch19 = newVal }
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        leading = { IconTile(Icons.Filled.Speed, contentDescription = null) }
                                    )
                                }
                            }
                        }
                        add {
                            Column {
                                MaterialSwitchItem(
                                    headline = if (isZh) "游戏均衡式性能温控" else "Game: Balanced uses Performance thermal",
                                    supporting = if (isZh) "把游戏中均衡模式的温控改成性能模式的原有温控,性能模式则无温控" else "Use Performance mode's original thermal for Balanced mode in games; Performance mode then has no thermal control",
                                    enabled = !switch3 && !switch14,
                                    checked = switch7,
                                    onCheckedChange = { newVal ->
                                        if (newVal) turnOffOtherThermalModes("switch7")
                                        updateSwitch("switch7", "游戏均衡式性能温控", newVal) { switch7 = newVal }
                                        if (!newVal && !switch6 && !switch14 && !switch15 && switch19) updateSwitch("switch19", "默认温控强制高刷", false) { switch19 = false }
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    leading = { IconTile(Icons.Filled.SportsEsports, contentDescription = null) }
                                )
                                AnimatedVisibility(
                                    visible = switch7,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    MaterialSwitchItem(
                                        headline = if (isZh) "默认温控强制高刷" else "Force high refresh rate on default thermal",
                                        supporting = if (isZh) "解除高温影响刷新率限制" else "Remove the high-temperature refresh rate limit",
                                        checked = switch19,
                                        onCheckedChange = { newVal ->
                                            updateSwitch("switch19", "默认温控强制高刷", newVal) { switch19 = newVal }
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        leading = { IconTile(Icons.Filled.Speed, contentDescription = null) }
                                    )
                                }
                            }
                        }
                        add {
                            Column {
                                MaterialSwitchItem(
                                    headline = if (isZh) "系统均衡式性能温控" else "System: Balanced uses Performance thermal",
                                    supporting = if (isZh) "把系统中均衡模式的温控改成性能模式的原有温控,性能模式则无温控" else "Use Performance mode's original thermal for Balanced mode in system; Performance mode then has no thermal control",
                                    enabled = !switch3 && !switch6 && !switch7 && !switch15,
                                    checked = switch14,
                                    onCheckedChange = { newVal ->
                                        if (newVal) turnOffOtherThermalModes("switch14")
                                        updateSwitch("switch14", "系统均衡式性能温控", newVal) { switch14 = newVal }
                                        if (!newVal && !switch6 && !switch7 && !switch15 && switch19) updateSwitch("switch19", "默认温控强制高刷", false) { switch19 = false }
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    leading = { IconTile(Icons.Filled.PhoneAndroid, contentDescription = null) }
                                )
                                AnimatedVisibility(
                                    visible = switch14,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    MaterialSwitchItem(
                                        headline = if (isZh) "默认温控强制高刷" else "Force high refresh rate on default thermal",
                                        supporting = if (isZh) "解除高温影响刷新率限制" else "Remove the high-temperature refresh rate limit",
                                        checked = switch19,
                                        onCheckedChange = { newVal ->
                                            updateSwitch("switch19", "默认温控强制高刷", newVal) { switch19 = newVal }
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        leading = { IconTile(Icons.Filled.Speed, contentDescription = null) }
                                    )
                                }
                            }
                        }
                    }
                )
            } else {
                MiuixCard {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        BasicComponent(title = if (isZh) "温控空挂载模式" else "Empty-mount thermal mode", summary = if (isZh) "非必要建议不开启此选项" else "Not recommended unless necessary", enabled = !switch6 && !switch7 && !switch14 && !switch15, endActions = { ThemedSwitch(checked = switch3, onCheckedChange = null, enabled = !switch6 && !switch7 && !switch14 && !switch15, useMonet = useMonet) }, onClick = { if (!switch6 && !switch7 && !switch14 && !switch15) { turnOffOtherThermalModes("switch3"); updateSwitch("switch3", "温控空挂载模式", !switch3) { switch3 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) } })
                        BasicComponent(title = if (isZh) "还原均衡模式温控" else "Restore Balanced mode thermal", enabled = !switch3 && !switch6 && !switch14, endActions = { ThemedSwitch(checked = switch15, onCheckedChange = null, enabled = !switch3 && !switch6 && !switch14, useMonet = useMonet) }, onClick = { if (!switch3 && !switch6 && !switch14) { if (!switch15) turnOffOtherThermalModes("switch15"); updateSwitch("switch15", "还原均衡模式温控", !switch15) { switch15 = it }; if (!switch15 && !switch6 && !switch7 && !switch14 && switch19) updateSwitch("switch19", "默认温控强制高刷", false) { switch19 = false }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) } })
                        AnimatedVisibility(visible = switch15 && !switch7, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                            BasicComponent(title = if (isZh) "默认温控强制高刷" else "Force high refresh rate on default thermal", summary = if (isZh) "解除高温影响刷新率限制" else "Remove the high-temperature refresh rate limit", endActions = { ThemedSwitch(checked = switch19, onCheckedChange = null, useMonet = useMonet) }, onClick = { updateSwitch("switch19", "默认温控强制高刷", !switch19) { switch19 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                        }
                        BasicComponent(title = if (isZh) "还原性能模式温控" else "Restore Performance mode thermal", enabled = !switch3 && !switch14 && !switch15, endActions = { ThemedSwitch(checked = switch6, onCheckedChange = null, enabled = !switch3 && !switch14 && !switch15, useMonet = useMonet) }, onClick = { if (!switch3 && !switch14 && !switch15) { if (!switch6) turnOffOtherThermalModes("switch6"); updateSwitch("switch6", "还原性能模式温控", !switch6) { switch6 = it }; if (!switch6 && !switch7 && !switch14 && !switch15 && switch19) updateSwitch("switch19", "默认温控强制高刷", false) { switch19 = false }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) } })
                        AnimatedVisibility(visible = switch6 && !switch7, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                            BasicComponent(title = if (isZh) "默认温控强制高刷" else "Force high refresh rate on default thermal", summary = if (isZh) "解除高温影响刷新率限制" else "Remove the high-temperature refresh rate limit", endActions = { ThemedSwitch(checked = switch19, onCheckedChange = null, useMonet = useMonet) }, onClick = { updateSwitch("switch19", "默认温控强制高刷", !switch19) { switch19 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                        }
                        BasicComponent(title = if (isZh) "游戏均衡式性能温控" else "Game: Balanced uses Performance thermal", summary = if (isZh) "把游戏中均衡模式的温控改成性能模式的原有温控,性能模式则无温控" else "Use Performance mode's original thermal for Balanced mode in games; Performance mode then has no thermal control", enabled = !switch3 && !switch14, endActions = { ThemedSwitch(checked = switch7, onCheckedChange = null, enabled = !switch3 && !switch14, useMonet = useMonet) }, onClick = { if (!switch3 && !switch14) { if (!switch7) turnOffOtherThermalModes("switch7"); updateSwitch("switch7", "游戏均衡式性能温控", !switch7) { switch7 = it }; if (!switch7 && !switch6 && !switch14 && !switch15 && switch19) updateSwitch("switch19", "默认温控强制高刷", false) { switch19 = false }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) } })
                        AnimatedVisibility(visible = switch7, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                            BasicComponent(title = if (isZh) "默认温控强制高刷" else "Force high refresh rate on default thermal", summary = if (isZh) "解除高温影响刷新率限制" else "Remove the high-temperature refresh rate limit", endActions = { ThemedSwitch(checked = switch19, onCheckedChange = null, useMonet = useMonet) }, onClick = { updateSwitch("switch19", "默认温控强制高刷", !switch19) { switch19 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                        }
                        BasicComponent(title = if (isZh) "系统均衡式性能温控" else "System: Balanced uses Performance thermal", summary = if (isZh) "把系统中均衡模式的温控改成性能模式的原有温控,性能模式则无温控" else "Use Performance mode's original thermal for Balanced mode in system; Performance mode then has no thermal control", enabled = !switch3 && !switch6 && !switch7 && !switch15, endActions = { ThemedSwitch(checked = switch14, onCheckedChange = null, enabled = !switch3 && !switch6 && !switch7 && !switch15, useMonet = useMonet) }, onClick = { if (!switch3 && !switch6 && !switch7 && !switch15) { if (!switch14) turnOffOtherThermalModes("switch14"); updateSwitch("switch14", "系统均衡式性能温控", !switch14) { switch14 = it }; if (!switch14 && !switch6 && !switch7 && !switch15 && switch19) updateSwitch("switch19", "默认温控强制高刷", false) { switch19 = false }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) } })
                        AnimatedVisibility(visible = switch14, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                            BasicComponent(title = if (isZh) "默认温控强制高刷" else "Force high refresh rate on default thermal", summary = if (isZh) "解除高温影响刷新率限制" else "Remove the high-temperature refresh rate limit", endActions = { ThemedSwitch(checked = switch19, onCheckedChange = null, useMonet = useMonet) }, onClick = { updateSwitch("switch19", "默认温控强制高刷", !switch19) { switch19 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                        }
                    }
                }
            }
        }
        item(key = "speed_title") {
            SmallTitle(text = if (isZh) "调速 (填写单位为微安）（开关重启生效)" else "Speed control (unit: μA, switch restart required)", modifier = Modifier.miuixSmallTitle())
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
                onDialogVisibilityChange = onDialogVisibilityChange,
                switch4 = switch4,
                switch10 = switch10,
                switch11 = switch11,
                switch12 = switch12,
                switch13 = switch13,
                switch8 = switch8,
                switch9 = switch9,
                homeExclusiveActive = exclusives.value.home,
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
            SmallTitle(text = if (isZh) "旁路充电" else "Bypass charging", modifier = Modifier.miuixSmallTitle())
        }

        item(key = "bypass_charge_card") {
            if (useMonet) {
                MaterialSegmentedColumn(
                    items = listOf(
                        {
                            Column {
                                MaterialSwitchItem(
                                    headline = if (isZh) "全局旁路" else "Global bypass",
                                    enabled = switch18 || (!exclusives.value.speed && !switch17),
                                    checked = switch18,
                                    onCheckedChange = { newVal ->
                                        if (newVal || (!exclusives.value.speed && !switch17)) {
                                            if (newVal) {
                                                turnOffAllOtherExclusive("switch18")
                                                if (switch17) updateSwitch("switch17", "分应用旁路", false) { switch17 = false }
                                            }
                                            updateSwitch("switch18", "全局旁路", newVal) { switch18 = newVal }
                                            writeBypassConfigFile()
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    },
                                    leading = { IconTile(Icons.Filled.Power, contentDescription = null) }
                                )
                                AnimatedVisibility(visible = switch18, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        key("global_bypass_target_temp") {
                                            SliderRow(
                                                title = if (isZh) "全局到达温度" else "Global target temperature",
                                                value = globalBypassTargetTemp.toFloatOrNull() ?: 40f,
                                                onValueChange = {
                                                    globalBypassTargetTemp = it.toInt().toString()
                                                    updateText("globalBypassTargetTemp", "全局旁路到达温度", globalBypassTargetTemp) {}
                                                },
                                                valueRange = 20f..50f, steps = 30, suffix = "°C",
                                                onClickLabel = {
                                                    dialogState = DialogState(
                                                        if (isZh) "调整全局到达温度" else "Adjust global target temperature",
                                                        if (isZh) "输入温度 (20°C-50°C)" else "Enter temperature (20°C-50°C)",
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
                                                title = if (isZh) "全局恢复温度" else "Global recovery temperature",
                                                value = globalBypassRecoveryTemp.toFloatOrNull() ?: 35f,
                                                onValueChange = {
                                                    globalBypassRecoveryTemp = it.toInt().toString()
                                                    updateText("globalBypassRecoveryTemp", "全局旁路恢复温度", globalBypassRecoveryTemp) {}
                                                },
                                                valueRange = 20f..50f, steps = 30, suffix = "°C",
                                                onClickLabel = {
                                                    dialogState = DialogState(if (isZh) "调整全局恢复温度" else "Adjust global recovery temperature", if (isZh) "输入温度 (20°C-50°C)" else "Enter temperature (20°C-50°C)", globalBypassRecoveryTemp, 20..50) { v ->
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
                                                label = "22A＝22000mA＝22000000μA",
                                                useMonet = useMonet,
                                                modifier = Modifier.fillMaxWidth(),
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
                                                label = if (isZh) "电量达到以下时关闭旁路充电" else "Turn off bypass charging when battery reaches this level",
                                                useMonet = useMonet,
                                                modifier = Modifier.fillMaxWidth(),
                                                inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } }
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        {
                            Column {
                                MaterialSwitchItem(
                                    headline = if (isZh) "分应用旁路" else "Per-app bypass",
                                    enabled = switch17 || (!exclusives.value.speed && !switch18),
                                    checked = switch17,
                                    onCheckedChange = { newVal ->
                                        if (newVal || (!exclusives.value.speed && !switch18)) {
                                            if (newVal) {
                                                turnOffAllOtherExclusive("switch17")
                                                if (switch18) updateSwitch("switch18", "全局旁路", false) { switch18 = false }
                                            }
                                            updateSwitch("switch17", "分应用旁路", newVal) { switch17 = newVal }
                                            writeBypassConfigFile()
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    },
                                    leading = { IconTile(Icons.Filled.Apps, contentDescription = null) }
                                )
                                AnimatedVisibility(visible = switch17, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        key("app_bypass_target_temp") {
                                            SliderRow(
                                                title = if (isZh) "应用到达温度" else "App target temperature",
                                                value = appBypassTargetTemp.toFloatOrNull() ?: 40f,
                                                onValueChange = {
                                                    appBypassTargetTemp = it.toInt().toString()
                                                    updateText("appBypassTargetTemp", "分应用旁路到达温度", appBypassTargetTemp) {}
                                                },
                                                valueRange = 20f..50f, steps = 30, suffix = "°C",
                                                onClickLabel = {
                                                    dialogState = DialogState(if (isZh) "调整应用到达温度" else "Adjust app target temperature", if (isZh) "输入温度 (20°C-50°C)" else "Enter temperature (20°C-50°C)", appBypassTargetTemp, 20..50) { v ->
                                                        appBypassTargetTemp = v.toString()
                                                    }
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                            )
                                        }
                                        key("app_bypass_recovery_temp") {
                                            SliderRow(
                                                title = if (isZh) "应用恢复温度" else "App recovery temperature",
                                                value = appBypassRecoveryTemp.toFloatOrNull() ?: 35f,
                                                onValueChange = {
                                                    appBypassRecoveryTemp = it.toInt().toString()
                                                    updateText("appBypassRecoveryTemp", "分应用旁路恢复温度", appBypassRecoveryTemp) {}
                                                },
                                                valueRange = 20f..50f, steps = 30, suffix = "°C",
                                                onClickLabel = {
                                                    dialogState = DialogState(if (isZh) "调整应用恢复温度" else "Adjust app recovery temperature", if (isZh) "输入温度 (20°C-50°C)" else "Enter temperature (20°C-50°C)", appBypassRecoveryTemp, 20..50) { v ->
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
                                                label = "22A＝22000mA＝22000000μA",
                                                useMonet = useMonet,
                                                modifier = Modifier.fillMaxWidth(),
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
                                                label = if (isZh) "电量达到以下时关闭旁路充电" else "Turn off bypass charging when battery reaches this level",
                                                useMonet = useMonet,
                                                modifier = Modifier.fillMaxWidth(),
                                                inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } }
                                            )
                                        }
                                        key("app_bypass_list") {
                                            MaterialListItem(
                                                headline = if (isZh) "旁路充电名单" else "Bypass charging app list",
                                                trailing = { MaterialChevron() },
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
                    )
                )
            } else {
                MiuixCard {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        BasicComponent(
                            title = if (isZh) "全局旁路" else "Global bypass",
                            enabled = switch18 || (!exclusives.value.speed && !switch17),
                            endActions = { ThemedSwitch(checked = switch18, onCheckedChange = null, enabled = switch18 || (!exclusives.value.speed && !switch17), useMonet = useMonet) },
                            onClick = {
                                if (switch18 || (!exclusives.value.speed && !switch17)) {
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
                                        title = if (isZh) "全局到达温度" else "Global target temperature",
                                        value = globalBypassTargetTemp.toFloatOrNull() ?: 40f,
                                        onValueChange = {
                                            globalBypassTargetTemp = it.toInt().toString()
                                            updateText("globalBypassTargetTemp", "全局旁路到达温度", globalBypassTargetTemp) {}
                                        },
                                        valueRange = 20f..50f, steps = 30, suffix = "°C",
                                        onClickLabel = {
                                            dialogState = DialogState(
                                                if (isZh) "调整全局到达温度" else "Adjust global target temperature",
                                                if (isZh) "输入温度 (20°C-50°C)" else "Enter temperature (20°C-50°C)",
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
                                        title = if (isZh) "全局恢复温度" else "Global recovery temperature",
                                        value = globalBypassRecoveryTemp.toFloatOrNull() ?: 35f,
                                        onValueChange = {
                                            globalBypassRecoveryTemp = it.toInt().toString()
                                            updateText("globalBypassRecoveryTemp", "全局旁路恢复温度", globalBypassRecoveryTemp) {}
                                        },
                                        valueRange = 20f..50f, steps = 30, suffix = "°C",
                                        onClickLabel = {
                                            dialogState = DialogState(if (isZh) "调整全局恢复温度" else "Adjust global recovery temperature", if (isZh) "输入温度 (20°C-50°C)" else "Enter temperature (20°C-50°C)", globalBypassRecoveryTemp, 20..50) { v ->
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
                                        label = "22A＝22000mA＝22000000μA",
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
                                        label = if (isZh) "电量达到以下时关闭旁路充电" else "Turn off bypass charging when battery reaches this level",
                                        useMonet = useMonet,
                                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                                        inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } }
                                    )
                                }
                            }
                        }
                        BasicComponent(
                            title = if (isZh) "分应用旁路" else "Per-app bypass",
                            enabled = switch17 || (!exclusives.value.speed && !switch18),
                            endActions = { ThemedSwitch(checked = switch17, onCheckedChange = null, enabled = switch17 || (!exclusives.value.speed && !switch18), useMonet = useMonet) },
                            onClick = {
                                if (switch17 || (!exclusives.value.speed && !switch18)) {
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
                                        title = if (isZh) "应用到达温度" else "App target temperature",
                                        value = appBypassTargetTemp.toFloatOrNull() ?: 40f,
                                        onValueChange = {
                                            appBypassTargetTemp = it.toInt().toString()
                                            updateText("appBypassTargetTemp", "分应用旁路到达温度", appBypassTargetTemp) {}
                                        },
                                        valueRange = 20f..50f, steps = 30, suffix = "°C",
                                        onClickLabel = {
                                            dialogState = DialogState(if (isZh) "调整应用到达温度" else "Adjust app target temperature", if (isZh) "输入温度 (20°C-50°C)" else "Enter temperature (20°C-50°C)", appBypassTargetTemp, 20..50) { v ->
                                                appBypassTargetTemp = v.toString()
                                            }
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    )
                                }
                                key("app_bypass_recovery_temp") {
                                    SliderRow(
                                        title = if (isZh) "应用恢复温度" else "App recovery temperature",
                                        value = appBypassRecoveryTemp.toFloatOrNull() ?: 35f,
                                        onValueChange = {
                                            appBypassRecoveryTemp = it.toInt().toString()
                                            updateText("appBypassRecoveryTemp", "分应用旁路恢复温度", appBypassRecoveryTemp) {}
                                        },
                                        valueRange = 20f..50f, steps = 30, suffix = "°C",
                                        onClickLabel = {
                                            dialogState = DialogState(if (isZh) "调整应用恢复温度" else "Adjust app recovery temperature", if (isZh) "输入温度 (20°C-50°C)" else "Enter temperature (20°C-50°C)", appBypassRecoveryTemp, 20..50) { v ->
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
                                        label = "22A＝22000mA＝22000000μA",
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
                                        label = if (isZh) "电量达到以下时关闭旁路充电" else "Turn off bypass charging when battery reaches this level",
                                        useMonet = useMonet,
                                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                                        inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } }
                                    )
                                }
                                key("app_bypass_list") {
                                    BasicComponent(
                                        title = if (isZh) "旁路充电名单" else "Bypass charging app list",
                                        endActions = {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = MiuixTheme.colorScheme.onSurfaceVariantActions
                                            )
                                        },
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

        if (floatingNavBar) {
            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }

        VerticalScrollBar(
            adapter = rememberScrollBarAdapter(listState),
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }

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
            onDismiss = { dialogState = null },
            onDismissStart = { dialogDismissStarted = true; onDialogVisibilityChange(false) }
        )
    }
}

private data class Exclusives(val home: Boolean, val speed: Boolean)
