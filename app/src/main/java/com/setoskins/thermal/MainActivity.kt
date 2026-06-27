package com.setoskins.thermal

import android.R.attr.progress
import android.content.Context
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import top.yukonga.miuix.kmp.basic.Switch as MiuixSwitch
import androidx.compose.material3.Switch
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.BlendMode as ComposeBlendMode
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import com.setoskins.thermal.ui.component.AnimatedBackground
import com.setoskins.thermal.ui.component.BlurredBar
import com.setoskins.thermal.ui.component.rememberBlurBackdrop
import com.setoskins.thermal.ui.component.effect.BgEffectBackground
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import com.setoskins.thermal.data.ModuleDetector
import com.setoskins.thermal.data.ThemePreferences
import com.setoskins.thermal.ui.theme.MyApplicationTheme
import com.setoskins.thermal.R
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.ui.graphics.RectangleShape
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.preference.ArrowPreference

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 只做“状态栏沉浸”：允许内容绘制到状态栏下面 + 状态栏透明
        // 导航栏保持系统/主题原样，不做沉浸与改色
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val defaultNavBarColor = window.navigationBarColor
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = AndroidColor.TRANSPARENT,
                darkScrim = AndroidColor.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = defaultNavBarColor,
                darkScrim = defaultNavBarColor
            )
        )

        val prefs = getSharedPreferences(
            "settings",
            MODE_PRIVATE
        )

        val savedUseMonet =
            prefs.getBoolean("useMonet", false)

        setContent {

            var useMonet by remember {
                mutableStateOf(savedUseMonet)
            }

            MyApplicationTheme(
                useMonet = useMonet
            ) {

                MyApplicationApp(
                    useMonet = useMonet,

                    onUseMonetChange = { value ->

                        useMonet = value

                        prefs.edit()
                            .putBoolean("useMonet", value)
                            .apply()
                    }
                )
            }
        }
    }
}

@Composable

fun MyApplicationApp(
    useMonet: Boolean,
    onUseMonetChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var currentDestination by rememberSaveable {
        mutableStateOf(AppDestinations.HOME)
    }

    var reloadTrigger by remember { mutableIntStateOf(0) }

    var rootState by remember { mutableStateOf<Boolean?>(null) }
    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"

    LaunchedEffect(Unit) {
        rootState = ModuleDetector.requestRoot()
    }

    val scrollBehavior =
        MiuixScrollBehavior(
            rememberTopAppBarState()
        )

    val backdrop = rememberBlurBackdrop()
    val shaderSupported = remember { isRuntimeShaderSupported() }
    val showBlur = useMonet && shaderSupported && backdrop != null

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(useMonet = useMonet)

        Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    if (currentDestination != AppDestinations.PROFILE) {
                        BlurredBar(backdrop, showBlur) {
                            TopAppBar(
                                title = currentDestination.label,
                                largeTitle = currentDestination.label,
                                scrollBehavior = scrollBehavior,
                                color = if (showBlur) Color.Transparent else MiuixTheme.colorScheme.surface
                            )
                        }
                    }
                },
                bottomBar = {
                    ThemedNavigationBar(
                        currentDestination = currentDestination,
                        onDestinationSelected = { currentDestination = it },
                        useMonet = useMonet,
                        backdrop = backdrop,
                        showBlur = showBlur
                    )
                }
            ) { innerPadding ->
                AnimatedContent(
                    targetState = currentDestination,
                    transitionSpec = {
                        val direction =
                            if (targetState.ordinal > initialState.ordinal) 1 else -1
                        val slideAnimation = tween<IntOffset>(
                            durationMillis = 320,
                            easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
                        )
                        slideInHorizontally(
                            animationSpec = slideAnimation,
                            initialOffsetX = { it * direction }
                        ) + fadeIn(
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) togetherWith
                            slideOutHorizontally(
                                animationSpec = slideAnimation,
                                targetOffsetX = { -it * direction }
                            ) + fadeOut(
                                animationSpec = tween(220, easing = LinearOutSlowInEasing)
                            )
                    },
                    label = "page_transition"
                ) { destination ->
                    when (destination) {
                        AppDestinations.HOME -> {
                            HomeScreen(
                                useMonet = useMonet,
                                reloadTrigger = reloadTrigger,
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .overScrollVertical()
                                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                            )
                        }
                        AppDestinations.FAVORITES -> {
                            FavoritesScreen(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .overScrollVertical()
                                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                            )
                        }
                        AppDestinations.PROFILE -> {
                            ProfileScreen(
                                useMonet = useMonet,
                                onUseMonetChange = onUseMonetChange,
                                onConfigImported = { reloadTrigger++ },
                                modifier = Modifier
                                    .padding(bottom = innerPadding.calculateBottomPadding())
                                    .overScrollVertical()
                                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                            )
                        }
                    }
                }
            }

        if (rootState == false) {
            OverlayDialog(
                show = true,
                title = if (isZh) "权限缺失" else "Root Permission Required",
                summary = if (isZh) {
                    "检测到设备未获取 Root 权限或拒绝了授权，本软件无法正常工作，请授权后重新进入。"
                } else {
                    "Root access was not detected or was denied. This app requires Root to function properly. Please grant Root permission and reopen the app."
                },
                onDismissRequest = { },
                content = {
                    BackHandler(enabled = true) { }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { (context as? android.app.Activity)?.finish() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColorsPrimary()
                        ) {
                            Text(if (isZh) "退出应用" else "Exit App")
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemedNavigationBar(
    currentDestination: AppDestinations,
    onDestinationSelected: (AppDestinations) -> Unit,
    useMonet: Boolean,
    backdrop: LayerBackdrop? = null,
    showBlur: Boolean = false,
    modifier: Modifier = Modifier
) {
    val barAlpha = 0.25f
    if (useMonet) {
        val miuixColors = MiuixTheme.colorScheme
        val navModifier = if (showBlur) {
            modifier
                .fillMaxWidth()
                .then(
                    if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier
                )
        } else {
            modifier.fillMaxWidth()
        }
        NavigationBar(
            modifier = navModifier,
            containerColor = miuixColors.surface.copy(alpha = barAlpha),
            contentColor = miuixColors.onSurface
        ) {
            AppDestinations.entries.forEach { destination ->
                NavigationBarItem(
                    icon = {
                        androidx.compose.material3.Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = {
                        androidx.compose.material3.Text(text = destination.label)
                    },
                    selected = destination == currentDestination,
                    onClick = { onDestinationSelected(destination) },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = miuixColors.primary,
                        selectedTextColor = miuixColors.primary,
                        unselectedIconColor = miuixColors.onSurfaceSecondary,
                        unselectedTextColor = miuixColors.onSurfaceSecondary,
                        indicatorColor = miuixColors.primary.copy(alpha = 0.12f)
                    )
                )
            }
        }
    } else {
        MiuixNavigationBar(
            modifier = modifier.fillMaxWidth(),
            color = MiuixTheme.colorScheme.surface.copy(alpha = barAlpha)
        ) {
            AppDestinations.entries.forEach { destination ->
                MiuixNavigationBarItem(
                    icon = destination.icon,
                    label = destination.label,
                    selected = destination == currentDestination,
                    onClick = { onDestinationSelected(destination) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    useMonet: Boolean,
    reloadTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    var switch2 by remember { mutableStateOf(prefs.getBoolean("switch2", false)) }
    var switch3 by remember { mutableStateOf(prefs.getBoolean("switch3", true)) }
    var switch4 by remember { mutableStateOf(prefs.getBoolean("switch4", false)) }
    // 模块简介显示充电信息(快充模式下追加的子项)
    var switch5 by remember { mutableStateOf(prefs.getBoolean("switch5", false)) }
    // 还原均衡模式温控(温控空挂载模式下追加的子项)
    var switch15 by remember { mutableStateOf(prefs.getBoolean("switch15", false)) }
    // 还原性能模式温控(温控空挂载模式下追加的子项)
    var switch6 by remember { mutableStateOf(prefs.getBoolean("switch6", false)) }
    // 游戏均衡式性能温控(温控空挂载模式下追加的子项)
    var switch7 by remember { mutableStateOf(prefs.getBoolean("switch7", false)) }
    // 系统均衡式性能温控
    var switch14 by remember { mutableStateOf(prefs.getBoolean("switch14", false)) }
    var switch8 by remember { mutableStateOf(prefs.getBoolean("switch8", false)) }
    var switch9 by remember { mutableStateOf(prefs.getBoolean("switch9", false)) }
    var switch10 by remember { mutableStateOf(prefs.getBoolean("switch10", false)) }
    var switch11 by remember { mutableStateOf(prefs.getBoolean("switch11", false)) }
    var switch12 by remember { mutableStateOf(prefs.getBoolean("switch12", false)) }
    var switch13 by remember { mutableStateOf(prefs.getBoolean("switch13", false)) }

    // 充电调速相关
    var limit1Temp by rememberSaveable { mutableStateOf(prefs.getString("limit1Temp", "40") ?: "40") }
    var limit1Current by rememberSaveable { mutableStateOf(prefs.getString("limit1Current", "") ?: "") }
    var limit2Temp by rememberSaveable { mutableStateOf(prefs.getString("limit2Temp", "43") ?: "43") }
    var limit2Current by rememberSaveable { mutableStateOf(prefs.getString("limit2Current", "") ?: "") }
    var limit3Temp by rememberSaveable { mutableStateOf(prefs.getString("limit3Temp", "46") ?: "46") }
    var limit3Current by rememberSaveable { mutableStateOf(prefs.getString("limit3Current", "") ?: "") }
    var delayTempThreshold by rememberSaveable { mutableStateOf(prefs.getString("delayTempThreshold", "10") ?: "10") }

    // 自定义阶梯模式相关
    var step1Level by rememberSaveable { mutableStateOf(prefs.getString("step1Level", "20") ?: "20") }
    var step1Current by rememberSaveable { mutableStateOf(prefs.getString("step1Current", "") ?: "") }
    var step2Level by rememberSaveable { mutableStateOf(prefs.getString("step2Level", "50") ?: "50") }
    var step2Current by rememberSaveable { mutableStateOf(prefs.getString("step2Current", "") ?: "") }
    var step3Level by rememberSaveable { mutableStateOf(prefs.getString("step3Level", "80") ?: "80") }
    var step3Current by rememberSaveable { mutableStateOf(prefs.getString("step3Current", "") ?: "") }

    // 停充相关
    var stopChargeLevel by rememberSaveable { mutableStateOf(prefs.getString("stopChargeLevel", "80") ?: "80") }
    var resumeChargeLevel by rememberSaveable { mutableStateOf(prefs.getString("resumeChargeLevel", "75") ?: "75") }
    var stopChargeCurrent by rememberSaveable { mutableStateOf(prefs.getString("stopChargeCurrent", "500") ?: "500") }

    // Dialog 控制
    var showLimit1Dialog by remember { mutableStateOf(false) }
    var showLimit2Dialog by remember { mutableStateOf(false) }
    var showLimit3Dialog by remember { mutableStateOf(false) }
    var showStopLevelDialog by remember { mutableStateOf(false) }
    var showResumeLevelDialog by remember { mutableStateOf(false) }
    var showStep1Dialog by remember { mutableStateOf(false) }
    var showStep2Dialog by remember { mutableStateOf(false) }
    var showStep3Dialog by remember { mutableStateOf(false) }
    var dialogInputValue by remember { mutableStateOf("") }

    var currentValue by rememberSaveable { mutableStateOf(prefs.getString("currentValue", "") ?: "") }
    var screenOnValue by rememberSaveable { mutableStateOf(prefs.getString("screenOnValue", "") ?: "") }
    var screenOffValue by rememberSaveable { mutableStateOf(prefs.getString("screenOffValue", "") ?: "") }

    // 模块安装状态:
    // - false = 未检测到 / 探测失败 / 设备无 root → 显示"未安装"红色卡片(默认)
    // - true  = su 探测到 /data/adb/modules/SetoSkins 存在 → 显示"已激活"绿色卡片
    var moduleInstalled by remember { mutableStateOf(false) }
    
    // 是否检测到模块更新
    var hasUpdate by remember { mutableStateOf(false) }

    // 模块版本号
    var moduleVersion by remember { mutableStateOf("") }

    // 同时监听 reloadTrigger, 导入或重置配置后立即刷新
    LaunchedEffect(reloadTrigger) {
        // 1. 开启并行加载，不再一个等一个
        launch { moduleInstalled = ModuleDetector.isModuleInstalled() }
        launch { moduleVersion = ModuleDetector.getModuleVersion() }
        launch { hasUpdate = ModuleDetector.checkUpdate() }

        // 2. 读取配置并同步 UI
        launch {
            val externalConfig = ModuleDetector.readConfig()
            val editor = prefs.edit()

            // 辅助函数：同步 Boolean 状态
            fun syncSwitch(key: String, currentVal: Boolean, update: (Boolean) -> Unit, prefKey: String) {
                val fileVal = externalConfig[key]?.trim()?.lowercase() == "true"
                if (currentVal != fileVal) {
                    update(fileVal)
                    editor.putBoolean(prefKey, fileVal)
                }
            }

            // 辅助函数：同步 String 状态
            fun syncText(key: String, currentVal: String, update: (String) -> Unit, prefKey: String) {
                val raw = externalConfig[key]?.trim()
                val fileVal = if (raw == null || raw.equals("false", ignoreCase = true)) "" else raw
                if (currentVal != fileVal) {
                    update(fileVal)
                    editor.putString(prefKey, fileVal)
                }
            }

            // 同步所有状态...
            syncSwitch("快充模式", switch2, { switch2 = it }, "switch2")
            syncSwitch("温控空挂载模式", switch3, { switch3 = it }, "switch3")
            syncSwitch("修改最大电流数", switch4, { switch4 = it }, "switch4")
            syncSwitch("模块简介显示充电信息", switch5, { switch5 = it }, "switch5")
            syncSwitch("还原均衡模式温控", switch15, { switch15 = it }, "switch15")
            syncSwitch("还原性能模式温控", switch6, { switch6 = it }, "switch6")
            syncSwitch("游戏均衡式性能温控", switch7, { switch7 = it }, "switch7")
            syncSwitch("系统均衡式性能温控", switch14, { switch14 = it }, "switch14")
            syncSwitch("分应用调速", switch8, { switch8 = it }, "switch8")
            syncSwitch("无温控应用", switch9, { switch9 = it }, "switch9")
            syncSwitch("亮息屏调速", switch10, { switch10 = it }, "switch10")
            syncSwitch("充电调速", switch11, { switch11 = it }, "switch11")
            syncSwitch("当电流低于阈值执行停充", switch12, { switch12 = it }, "switch12")
            syncSwitch("自定义阶梯模式", switch13, { switch13 = it }, "switch13")

            syncText("最大电流数", currentValue, { currentValue = it }, "currentValue")
            syncText("一限温度阈值", limit1Temp, { limit1Temp = it }, "limit1Temp")
            syncText("一限限制电流", limit1Current, { limit1Current = it }, "limit1Current")
            syncText("二限温度阈值", limit2Temp, { limit2Temp = it }, "limit2Temp")
            syncText("二限限制电流", limit2Current, { limit2Current = it }, "limit2Current")
            syncText("三限温度阈值", limit3Temp, { limit3Temp = it }, "limit3Temp")
            syncText("三限限制电流", limit3Current, { limit3Current = it }, "limit3Current")
            syncText("延迟温度阈值", delayTempThreshold, { delayTempThreshold = it }, "delayTempThreshold")
            syncText("一限电量阈值", step1Level, { step1Level = it }, "step1Level")
            syncText("一限电量限制电流", step1Current, { step1Current = it }, "step1Current")
            syncText("二限电量阈值", step2Level, { step2Level = it }, "step2Level")
            syncText("二限电量限制电流", step2Current, { step2Current = it }, "step2Current")
            syncText("三限电量阈值", step3Level, { step3Level = it }, "step3Level")
            syncText("三限电量限制电流", step3Current, { step3Current = it }, "step3Current")
            syncText("电量检测阈值", stopChargeLevel, { stopChargeLevel = it }, "stopChargeLevel")
            syncText("恢复充电电量", resumeChargeLevel, { resumeChargeLevel = it }, "resumeChargeLevel")
            syncText("停充电流阈值", stopChargeCurrent, { stopChargeCurrent = it }, "stopChargeCurrent")
            syncText("亮屏限制电流", screenOnValue, { screenOnValue = it }, "screenOnValue")
            syncText("锁屏限制电流", screenOffValue, { screenOffValue = it }, "screenOffValue")

            editor.apply()
        }
    }

    // 同步状态变化到本地并同步到配置文件
    fun updateSwitch(prefKey: String, configKey: String, newValue: Boolean, setter: (Boolean) -> Unit) {
        setter(newValue)
        prefs.edit().putBoolean(prefKey, newValue).apply()
        scope.launch {
            ModuleDetector.updateConfig(configKey, newValue)
            ModuleDetector.executeThermalScript()
        }
    }

    fun updateText(prefKey: String, configKey: String, newValue: String, setter: (String) -> Unit) {
        setter(newValue)
        prefs.edit().putString(prefKey, newValue).apply()
        scope.launch { ModuleDetector.updateConfig(configKey, newValue) }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        // 状态卡片(已激活 / 未安装 二选一)
        // - moduleInstalled = true  → 显示绿色"已激活"卡片
        // - moduleInstalled = false → 显示红色"未安装"卡片(默认)
        // 两张卡互斥,任何时候只渲染一张
        item {
            if (!moduleInstalled) {
                RedNotInstalledCard()
            } else {
                if (hasUpdate) {
                    YellowUpdateCard()
                } else {
                    GreenActivatedCard(useMonet = useMonet, version = moduleVersion)
                }
            }
        }

        item {
            SmallTitle(
                text = "配置",
                modifier = Modifier
                    .offset(x = (-12).dp)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        // 高度减 8dp，文字位置不变，上方空隙缩小
                        layout(placeable.width, placeable.height - 8.dp.roundToPx()) {
                            placeable.place(0, 0)
                        }
                    }
            )
        }

        // 开关选项卡片（4个开关，前2个带tips）
        // 开关卡片
        item {
            MiuixCard {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {

                    BasicComponent(
                        title = "简洁版配置",
                        summary = "目前无法更改",
                        endActions = {
                            ThemedSwitch(
                                checked = false,
                                onCheckedChange = null,
                                enabled = false,
                                useMonet = useMonet
                            )
                        }
                    )



                    BasicComponent(
                        title = "模块简介显示充电信息",
                        summary = "Magisk/KSU里显示电流、电量等充电信息,可能耗一丢丢电",
                        endActions = {
                            ThemedSwitch(
                                checked = switch5,
                                onCheckedChange = null,
                                useMonet = useMonet
                            )
                        },
                        onClick = {
                            updateSwitch("switch5", "模块简介显示充电信息", !switch5) { switch5 = it }
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))

            SmallTitle(
                text = "温控",
                modifier = Modifier
                    .padding(start = 4.dp)
                    .offset(x = (-13).dp, y = (11).dp)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val reduce = 24.dp.roundToPx()

                        layout(
                            placeable.width,
                            (placeable.height - reduce).coerceAtLeast(1)
                        ) {
                            placeable.place(0, -reduce)
                        }
                    }
            )
        }

        item {
            MiuixCard {
                BasicComponent(
                    title = "快充模式",
                    endActions = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (switch2) "True" else "False",
                                fontSize = 17.sp,
                                color = if (switch2) {
                                    MiuixTheme.colorScheme.primary
                                } else {
                                    MiuixTheme.colorScheme.onSurfaceSecondary
                                }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            ThemedSwitch(
                                checked = switch2,
                                onCheckedChange = null,
                                useMonet = useMonet
                            )
                        }
                    },
                    onClick = {
                        updateSwitch("switch2", "快充模式", !switch2) { switch2 = it }
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
                Column(modifier = Modifier.padding(vertical = 4.dp)) {

                    BasicComponent(
                        title = "温控空挂载模式",
                        summary = "非必要建议不开启此选项",
                        endActions = {
                            ThemedSwitch(
                                checked = switch3,
                                onCheckedChange = null,
                                useMonet = useMonet
                            )
                        },
                        onClick = {
                            updateSwitch("switch3", "温控空挂载模式", !switch3) { switch3 = it }
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )

                    BasicComponent(
                        title = "还原均衡模式温控",
                        enabled = switch15 || !switch6,
                        endActions = {
                            ThemedSwitch(
                                checked = switch15,
                                onCheckedChange = null,
                                enabled = switch15 || !switch6,
                                useMonet = useMonet
                            )
                        },
                        onClick = {
                            if (switch15 || !switch6) {
                                updateSwitch("switch15", "还原均衡模式温控", !switch15) { switch15 = it }
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    )

                    BasicComponent(
                        title = "还原性能模式温控",
                        enabled = switch6 || !switch15,
                        endActions = {
                            ThemedSwitch(
                                checked = switch6,
                                onCheckedChange = null,
                                enabled = switch6 || !switch15,
                                useMonet = useMonet
                            )
                        },
                        onClick = {
                            if (switch6 || !switch15) {
                                updateSwitch("switch6", "还原性能模式温控", !switch6) { switch6 = it }
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    )

                    BasicComponent(
                        title = "游戏均衡式性能温控",
                        summary = "把游戏中均衡模式的温控改成性能模式的原有温控,性能模式则无温控",
                        endActions = {
                            ThemedSwitch(
                                checked = switch7,
                                onCheckedChange = null,
                                useMonet = useMonet
                            )
                        },
                        onClick = {
                            updateSwitch("switch7", "游戏均衡式性能温控", !switch7) { switch7 = it }
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )

                    BasicComponent(
                        title = "系统均衡式性能温控",
                        summary = "把系统中均衡模式的温控改成性能模式的原有温控,性能模式则无温控",
                        endActions = {
                            ThemedSwitch(
                                checked = switch14,
                                onCheckedChange = null,
                                useMonet = useMonet
                            )
                        },
                        onClick = {
                            updateSwitch("switch14", "系统均衡式性能温控", !switch14) { switch14 = it }
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))

            SmallTitle(
                text = "调速 (22000mA＝22000000μA)(重启生效)",
                modifier = Modifier.compactSmallTitle()
            )
        }

        item {
            MiuixCard {
                Column {
                    BasicComponent(
                        title = "修改最大电流数",
                        enabled = switch4 || (!switch11 && !switch13 && !switch10),
                        endActions = {
                            ThemedSwitch(
                                checked = switch4,
                                onCheckedChange = null,
                                enabled = switch4 || (!switch11 && !switch13 && !switch10),
                                useMonet = useMonet
                            )
                        },
                        onClick = {
                            if (switch4 || (!switch11 && !switch13 && !switch10)) {
                                updateSwitch("switch4", "修改最大电流数", !switch4) { switch4 = it }
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = switch4,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 12.dp
                                )
                        ) {
                            if (useMonet) {
                                OutlinedTextField(
                                    value = currentValue,
                                    onValueChange = { updateText("currentValue", "最大电流数", it) { currentValue = it } },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("22A＝22000mA＝22000000") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MiuixTheme.colorScheme.primary,
                                        focusedLabelColor = MiuixTheme.colorScheme.primary,
                                        cursorColor = MiuixTheme.colorScheme.primary,
                                        focusedTextColor = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                            } else {
                                TextField(
                                    value = currentValue,
                                    onValueChange = { updateText("currentValue", "最大电流数", it) { currentValue = it } },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = "22A＝22000mA＝22000000",
                                    singleLine = true
                                )
                            }
                        }
                    }

                    BasicComponent(
                        title = "充电调速",
                        enabled = switch11 || (!switch4 && !switch13 && !switch10),
                        endActions = {
                            ThemedSwitch(
                                checked = switch11,
                                onCheckedChange = null,
                                enabled = switch11 || (!switch4 && !switch13 && !switch10),
                                useMonet = useMonet
                            )
                        },
                        onClick = {
                            if (switch11 || (!switch4 && !switch13 && !switch10)) {
                                updateSwitch("switch11", "充电调速", !switch11) { switch11 = it }
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = switch11,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            BasicComponent(
                                title = "一限温度阈值",
                                endActions = {
                                    Text(
                                        text = "$limit1Temp°C",
                                        fontSize = 14.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Image(
                                        modifier = Modifier
                                            .size(width = 10.dp, height = 16.dp)
                                            .align(Alignment.CenterVertically),
                                        imageVector = MiuixIcons.Basic.ArrowRight,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions),
                                    )
                                },
                                onClick = {
                                    dialogInputValue = limit1Temp
                                    showLimit1Dialog = true
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )

                            Slider(
                                value = limit1Temp.toFloatOrNull() ?: 40f,
                                onValueChange = { newValue ->
                                    limit1Temp = newValue.toInt().toString()
                                },
                                valueRange = 20f..50f,
                                steps = 30,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            )
                            if (useMonet) {
                                OutlinedTextField(
                                    value = limit1Current,
                                    onValueChange = { limit1Current = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    label = { Text("一限限制电流") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MiuixTheme.colorScheme.primary,
                                        focusedLabelColor = MiuixTheme.colorScheme.primary,
                                        cursorColor = MiuixTheme.colorScheme.primary,
                                        focusedTextColor = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                            } else {
                                TextField(
                                    value = limit1Current,
                                    onValueChange = { limit1Current = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    label = "一限限制电流",
                                    singleLine = true
                                )
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = switch11,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            BasicComponent(
                                title = "二限温度阈值",
                                endActions = {
                                    Text(
                                        text = "$limit2Temp°C",
                                        fontSize = 14.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Image(
                                        modifier = Modifier
                                            .size(width = 10.dp, height = 16.dp)
                                            .align(Alignment.CenterVertically),
                                        imageVector = MiuixIcons.Basic.ArrowRight,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions),
                                    )
                                },
                                onClick = {
                                    dialogInputValue = limit2Temp
                                    showLimit2Dialog = true
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )

                            Slider(
                                value = limit2Temp.toFloatOrNull() ?: 43f,
                                onValueChange = { newValue ->
                                    limit2Temp = newValue.toInt().toString()
                                },
                                valueRange = 20f..50f,
                                steps = 30,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            )
                            if (useMonet) {
                                OutlinedTextField(
                                    value = limit2Current,
                                    onValueChange = { limit2Current = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    label = { Text("二限限制电流") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MiuixTheme.colorScheme.primary,
                                        focusedLabelColor = MiuixTheme.colorScheme.primary,
                                        cursorColor = MiuixTheme.colorScheme.primary,
                                        focusedTextColor = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                            } else {
                                TextField(
                                    value = limit2Current,
                                    onValueChange = { limit2Current = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    label = "二限限制电流",
                                    singleLine = true
                                )
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = switch11,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            BasicComponent(
                                title = "三限温度阈值",
                                endActions = {
                                    Text(
                                        text = "$limit3Temp°C",
                                        fontSize = 14.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Image(
                                        modifier = Modifier
                                            .size(width = 10.dp, height = 16.dp)
                                            .align(Alignment.CenterVertically),
                                        imageVector = MiuixIcons.Basic.ArrowRight,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions),
                                    )
                                },
                                onClick = {
                                    dialogInputValue = limit3Temp
                                    showLimit3Dialog = true
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )

                            Slider(
                                value = limit3Temp.toFloatOrNull() ?: 46f,
                                onValueChange = { newValue ->
                                    limit3Temp = newValue.toInt().toString()
                                },
                                valueRange = 20f..50f,
                                steps = 30,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            )
                            if (useMonet) {
                                OutlinedTextField(
                                    value = limit3Current,
                                    onValueChange = { limit3Current = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    label = { Text("三限限制电流") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MiuixTheme.colorScheme.primary,
                                        focusedLabelColor = MiuixTheme.colorScheme.primary,
                                        cursorColor = MiuixTheme.colorScheme.primary,
                                        focusedTextColor = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                            } else {
                                TextField(
                                    value = limit3Current,
                                    onValueChange = { limit3Current = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    label = "三限限制电流",
                                    singleLine = true
                                )
                            }

                            BasicComponent(
                                title = "延迟温度阈值",
                                summary = "为了防止电流卡在限制电流，需要设置延迟温度阈值。",
                                endActions = {
                                    Text(
                                        text = "$delayTempThreshold S",
                                        fontSize = 14.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                            )

                            Slider(
                                value = delayTempThreshold.toFloatOrNull() ?: 10f,
                                onValueChange = { newValue ->
                                    delayTempThreshold = newValue.toInt().toString()
                                },
                                valueRange = 5f..30f,
                                steps = 25,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            )
                        }
                    }

                    BasicComponent(
                        title = "自定义阶梯模式",
                        enabled = switch13 || (!switch4 && !switch11 && !switch10),
                        endActions = {
                            ThemedSwitch(
                                checked = switch13,
                                onCheckedChange = null,
                                enabled = switch13 || (!switch4 && !switch11 && !switch10),
                                useMonet = useMonet
                            )
                        },
                        onClick = {
                            if (switch13 || (!switch4 && !switch11 && !switch10)) {
                                switch13 = !switch13
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = switch13,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            BasicComponent(
                                title = "一限电量阈值",
                                endActions = {
                                    Text(
                                        text = "$step1Level %",
                                        fontSize = 14.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Image(
                                        modifier = Modifier
                                            .size(width = 10.dp, height = 16.dp)
                                            .align(Alignment.CenterVertically),
                                        imageVector = MiuixIcons.Basic.ArrowRight,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions),
                                    )
                                },
                                onClick = {
                                    dialogInputValue = step1Level
                                    showStep1Dialog = true
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )

                            Slider(
                                value = step1Level.toFloatOrNull() ?: 20f,
                                onValueChange = { newValue ->
                                    step1Level = newValue.toInt().toString()
                                },
                                valueRange = 0f..100f,
                                steps = 100,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            )
                            if (useMonet) {
                                OutlinedTextField(
                                    value = step1Current,
                                    onValueChange = { step1Current = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    label = { Text("一限电量限制电流") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MiuixTheme.colorScheme.primary,
                                        focusedLabelColor = MiuixTheme.colorScheme.primary,
                                        cursorColor = MiuixTheme.colorScheme.primary,
                                        focusedTextColor = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                            } else {
                                TextField(
                                    value = step1Current,
                                    onValueChange = { step1Current = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    label = "一限电量限制电流",
                                    singleLine = true
                                )
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = switch13,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            BasicComponent(
                                title = "二限电量阈值",
                                endActions = {
                                    Text(
                                        text = "$step2Level %",
                                        fontSize = 14.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Image(
                                        modifier = Modifier
                                            .size(width = 10.dp, height = 16.dp)
                                            .align(Alignment.CenterVertically),
                                        imageVector = MiuixIcons.Basic.ArrowRight,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions),
                                    )
                                },
                                onClick = {
                                    dialogInputValue = step2Level
                                    showStep2Dialog = true
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )

                            Slider(
                                value = step2Level.toFloatOrNull() ?: 50f,
                                onValueChange = { newValue ->
                                    step2Level = newValue.toInt().toString()
                                },
                                valueRange = 0f..100f,
                                steps = 100,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            )
                            if (useMonet) {
                                OutlinedTextField(
                                    value = step2Current,
                                    onValueChange = { step2Current = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    label = { Text("二限电量限制电流") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MiuixTheme.colorScheme.primary,
                                        focusedLabelColor = MiuixTheme.colorScheme.primary,
                                        cursorColor = MiuixTheme.colorScheme.primary,
                                        focusedTextColor = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                            } else {
                                TextField(
                                    value = step2Current,
                                    onValueChange = { step2Current = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    label = "二限电量限制电流",
                                    singleLine = true
                                )
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = switch13,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            BasicComponent(
                                title = "三限电量阈值",
                                endActions = {
                                    Text(
                                        text = "$step3Level %",
                                        fontSize = 14.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Image(
                                        modifier = Modifier
                                            .size(width = 10.dp, height = 16.dp)
                                            .align(Alignment.CenterVertically),
                                        imageVector = MiuixIcons.Basic.ArrowRight,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions),
                                    )
                                },
                                onClick = {
                                    dialogInputValue = step3Level
                                    showStep3Dialog = true
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )

                            Slider(
                                value = step3Level.toFloatOrNull() ?: 80f,
                                onValueChange = { newValue ->
                                    step3Level = newValue.toInt().toString()
                                },
                                valueRange = 0f..100f,
                                steps = 100,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            )
                            if (useMonet) {
                                OutlinedTextField(
                                    value = step3Current,
                                    onValueChange = { step3Current = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    label = { Text("三限电量限制电流") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MiuixTheme.colorScheme.primary,
                                        focusedLabelColor = MiuixTheme.colorScheme.primary,
                                        cursorColor = MiuixTheme.colorScheme.primary,
                                        focusedTextColor = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                            } else {
                                TextField(
                                    value = step3Current,
                                    onValueChange = { step3Current = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    label = "三限电量限制电流",
                                    singleLine = true
                                )
                            }
                        }
                    }


                    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"

                    // 一限温度 Dialog
                    OverlayDialog(
                        show = showLimit1Dialog,
                        title = "调整一限温度阈值",
                        summary = "输入温度 (20-50 °C)",
                        onDismissRequest = { showLimit1Dialog = false },
                        content = {
                            HomeScreenDialogContent(
                                value = dialogInputValue,
                                onValueChange = { dialogInputValue = it },
                                onConfirm = {
                                    val newValue = dialogInputValue.toIntOrNull()
                                    if (newValue != null && newValue in 20..50) {
                                        limit1Temp = newValue.toString()
                                    }
                                    showLimit1Dialog = false
                                },
                                onCancel = { showLimit1Dialog = false },
                                isZh = isZh
                            )
                        }
                    )

                    // 二限温度 Dialog
                    OverlayDialog(
                        show = showLimit2Dialog,
                        title = "调整二限温度阈值",
                        summary = "输入温度 (20-50 °C)",
                        onDismissRequest = { showLimit2Dialog = false },
                        content = {
                            HomeScreenDialogContent(
                                value = dialogInputValue,
                                onValueChange = { dialogInputValue = it },
                                onConfirm = {
                                    val newValue = dialogInputValue.toIntOrNull()
                                    if (newValue != null && newValue in 20..50) {
                                        limit2Temp = newValue.toString()
                                    }
                                    showLimit2Dialog = false
                                },
                                onCancel = { showLimit2Dialog = false },
                                isZh = isZh
                            )
                        }
                    )

                    // 三限温度 Dialog
                    OverlayDialog(
                        show = showLimit3Dialog,
                        title = "调整三限温度阈值",
                        summary = "输入温度 (20-50 °C)",
                        onDismissRequest = { showLimit3Dialog = false },
                        content = {
                            HomeScreenDialogContent(
                                value = dialogInputValue,
                                onValueChange = { dialogInputValue = it },
                                onConfirm = {
                                    val newValue = dialogInputValue.toIntOrNull()
                                    if (newValue != null && newValue in 20..50) {
                                        limit3Temp = newValue.toString()
                                    }
                                    showLimit3Dialog = false
                                },
                                onCancel = { showLimit3Dialog = false },
                                isZh = isZh
                            )
                        }
                    )

                    // 一限电量 Dialog
                    OverlayDialog(
                        show = showStep1Dialog,
                        title = "调整一限电量阈值",
                        summary = "输入电量 (0-100 %)",
                        onDismissRequest = { showStep1Dialog = false },
                        content = {
                            HomeScreenDialogContent(
                                value = dialogInputValue,
                                onValueChange = { dialogInputValue = it },
                                onConfirm = {
                                    val newValue = dialogInputValue.toIntOrNull()
                                    if (newValue != null && newValue in 0..100) {
                                        step1Level = newValue.toString()
                                    }
                                    showStep1Dialog = false
                                },
                                onCancel = { showStep1Dialog = false },
                                isZh = isZh
                            )
                        }
                    )

                    // 二限电量 Dialog
                    OverlayDialog(
                        show = showStep2Dialog,
                        title = "调整二限电量阈值",
                        summary = "输入电量 (0-100 %)",
                        onDismissRequest = { showStep2Dialog = false },
                        content = {
                            HomeScreenDialogContent(
                                value = dialogInputValue,
                                onValueChange = { dialogInputValue = it },
                                onConfirm = {
                                    val newValue = dialogInputValue.toIntOrNull()
                                    if (newValue != null && newValue in 0..100) {
                                        step2Level = newValue.toString()
                                    }
                                    showStep2Dialog = false
                                },
                                onCancel = { showStep2Dialog = false },
                                isZh = isZh
                            )
                        }
                    )

                    // 三限电量 Dialog
                    OverlayDialog(
                        show = showStep3Dialog,
                        title = "调整三限电量阈值",
                        summary = "输入电量 (0-100 %)",
                        onDismissRequest = { showStep3Dialog = false },
                        content = {
                            HomeScreenDialogContent(
                                value = dialogInputValue,
                                onValueChange = { dialogInputValue = it },
                                onConfirm = {
                                    val newValue = dialogInputValue.toIntOrNull()
                                    if (newValue != null && newValue in 0..100) {
                                        step3Level = newValue.toString()
                                    }
                                    showStep3Dialog = false
                                },
                                onCancel = { showStep3Dialog = false },
                                isZh = isZh
                            )
                        }
                    )

                    // 电量检测阈值 Dialog
                    OverlayDialog(
                        show = showStopLevelDialog,
                        title = "调整电量检测阈值",
                        summary = "输入电量检测阈值范围 0-100",
                        onDismissRequest = { showStopLevelDialog = false },
                        content = {
                            HomeScreenDialogContent(
                                value = dialogInputValue,
                                onValueChange = { dialogInputValue = it },
                                onConfirm = {
                                    val newValue = dialogInputValue.toIntOrNull()
                                    if (newValue != null && newValue in 0..100) {
                                        stopChargeLevel = newValue.toString()
                                    }
                                    showStopLevelDialog = false
                                },
                                onCancel = { showStopLevelDialog = false },
                                isZh = isZh
                            )
                        }
                    )

                    // 恢复充电电量 Dialog
                    OverlayDialog(
                        show = showResumeLevelDialog,
                        title = "调整恢复充电电量",
                        summary = "输入恢复充电电量范围 0-100",
                        onDismissRequest = { showResumeLevelDialog = false },
                        content = {
                            HomeScreenDialogContent(
                                value = dialogInputValue,
                                onValueChange = { dialogInputValue = it },
                                onConfirm = {
                                    val newValue = dialogInputValue.toIntOrNull()
                                    if (newValue != null && newValue in 0..100) {
                                        resumeChargeLevel = newValue.toString()
                                    }
                                    showResumeLevelDialog = false
                                },
                                onCancel = { showResumeLevelDialog = false },
                                isZh = isZh
                            )
                        }
                    )

                    // 一限电量 Dialog
                    OverlayDialog(
                        show = showStep1Dialog,
                        title = "调整一限电量阈值",
                        summary = "输入电量 (0-100 %)",
                        onDismissRequest = { showStep1Dialog = false },
                        content = {
                            HomeScreenDialogContent(
                                value = dialogInputValue,
                                onValueChange = { dialogInputValue = it },
                                onConfirm = {
                                    val newValue = dialogInputValue.toIntOrNull()
                                    if (newValue != null && newValue in 0..100) {
                                        step1Level = newValue.toString()
                                    }
                                    showStep1Dialog = false
                                },
                                onCancel = { showStep1Dialog = false },
                                isZh = isZh
                            )
                        }
                    )

                    // 二限电量 Dialog
                    OverlayDialog(
                        show = showStep2Dialog,
                        title = "调整二限电量阈值",
                        summary = "输入电量 (0-100 %)",
                        onDismissRequest = { showStep2Dialog = false },
                        content = {
                            HomeScreenDialogContent(
                                value = dialogInputValue,
                                onValueChange = { dialogInputValue = it },
                                onConfirm = {
                                    val newValue = dialogInputValue.toIntOrNull()
                                    if (newValue != null && newValue in 0..100) {
                                        step2Level = newValue.toString()
                                    }
                                    showStep2Dialog = false
                                },
                                onCancel = { showStep2Dialog = false },
                                isZh = isZh
                            )
                        }
                    )

                    // 三限电量 Dialog
                    OverlayDialog(
                        show = showStep3Dialog,
                        title = "调整三限电量阈值",
                        summary = "输入电量 (0-100 %)",
                        onDismissRequest = { showStep3Dialog = false },
                        content = {
                            HomeScreenDialogContent(
                                value = dialogInputValue,
                                onValueChange = { dialogInputValue = it },
                                onConfirm = {
                                    val newValue = dialogInputValue.toIntOrNull()
                                    if (newValue != null && newValue in 0..100) {
                                        step3Level = newValue.toString()
                                    }
                                    showStep3Dialog = false
                                },
                                onCancel = { showStep3Dialog = false },
                                isZh = isZh
                            )
                        }
                    )

                    BasicComponent(
                        title = "亮息屏调速",
                        enabled = switch10 || (!switch4 && !switch11 && !switch13),
                        endActions = {
                            ThemedSwitch(
                                checked = switch10,
                                onCheckedChange = null,
                                enabled = switch10 || (!switch4 && !switch11 && !switch13),
                                useMonet = useMonet
                            )
                        },
                        onClick = {
                            if (switch10 || (!switch4 && !switch11 && !switch13)) {
                                switch10 = !switch10
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = switch10,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 亮屏输入框
                            if (useMonet) {
                                OutlinedTextField(
                                    value = screenOnValue,
                                    onValueChange = { screenOnValue = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("亮屏限制电流") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MiuixTheme.colorScheme.primary,
                                        focusedLabelColor = MiuixTheme.colorScheme.primary,
                                        cursorColor = MiuixTheme.colorScheme.primary,
                                        focusedTextColor = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                            } else {
                                TextField(
                                    value = screenOnValue,
                                    onValueChange = { screenOnValue = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = "亮屏限制电流",
                                    singleLine = true
                                )
                            }

                            // 锁屏输入框
                            if (useMonet) {
                                OutlinedTextField(
                                    value = screenOffValue,
                                    onValueChange = { screenOffValue = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("锁屏限制电流") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MiuixTheme.colorScheme.primary,
                                        focusedLabelColor = MiuixTheme.colorScheme.primary,
                                        cursorColor = MiuixTheme.colorScheme.primary,
                                        focusedTextColor = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                            } else {
                                TextField(
                                    value = screenOffValue,
                                    onValueChange = { screenOffValue = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = "锁屏限制电流",
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    BasicComponent(
                        title = "当电流低于阈值执行停充",
                        endActions = {
                            ThemedSwitch(
                                checked = switch12,
                                onCheckedChange = null,
                                useMonet = useMonet
                            )
                        },
                        onClick = {
                            switch12 = !switch12
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )

                    AnimatedVisibility(
                        visible = switch12,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            BasicComponent(
                                title = "电量检测阈值",
                                endActions = {
                                    Text(
                                        text = "$stopChargeLevel %",
                                        fontSize = 13.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Image(
                                        modifier = Modifier
                                            .size(width = 10.dp, height = 16.dp)
                                            .align(Alignment.CenterVertically),
                                        imageVector = MiuixIcons.Basic.ArrowRight,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions),
                                    )
                                },
                                onClick = {
                                    dialogInputValue = stopChargeLevel
                                    showStopLevelDialog = true
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )

                            Slider(
                                value = stopChargeLevel.toFloatOrNull() ?: 80f,
                                onValueChange = { newValue ->
                                    stopChargeLevel = newValue.toInt().toString()
                                },
                                valueRange = 0f..100f,
                                steps = 100,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            )

                            BasicComponent(
                                title = "恢复充电电量",
                                endActions = {
                                    Text(
                                        text = "$resumeChargeLevel %",
                                        fontSize = 13.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Image(
                                        modifier = Modifier
                                            .size(width = 10.dp, height = 16.dp)
                                            .align(Alignment.CenterVertically),
                                        imageVector = MiuixIcons.Basic.ArrowRight,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions),
                                    )
                                },
                                onClick = {
                                    dialogInputValue = resumeChargeLevel
                                    showResumeLevelDialog = true
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )

                            Slider(
                                value = resumeChargeLevel.toFloatOrNull() ?: 75f,
                                onValueChange = { newValue ->
                                    resumeChargeLevel = newValue.toInt().toString()
                                },
                                valueRange = 0f..100f,
                                steps = 100,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            )

                            if (useMonet) {
                                OutlinedTextField(
                                    value = stopChargeCurrent,
                                    onValueChange = { stopChargeCurrent = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                                    label = { Text("停充电流阈值") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MiuixTheme.colorScheme.primary,
                                        focusedLabelColor = MiuixTheme.colorScheme.primary,
                                        cursorColor = MiuixTheme.colorScheme.primary,
                                        focusedTextColor = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                            } else {
                                TextField(
                                    value = stopChargeCurrent,
                                    onValueChange = { stopChargeCurrent = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                                    label = "停充电流阈值",
                                    singleLine = true
                                )
                            }
                        }
                    }

//                    BasicComponent(
//                        title = "分应用调速",
//                        summary = "自定义应用进行限流",
//                        endActions = {
//                            ThemedSwitch(
//                                checked = switch8,
//                                onCheckedChange = null,
//                                useMonet = useMonet
//                            )
//                        },
//                        onClick = {
//                            switch8 = !switch8
//                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
//                        }
//                    )
//
//                    BasicComponent(
//                        title = "无温控应用",
//                        summary = "自定义应用进行限流",
//                        endActions = {
//                            ThemedSwitch(
//                                checked = switch9,
//                                onCheckedChange = null,
//                                useMonet = useMonet
//                            )
//                        },
//                        onClick = {
//                            switch9 = !switch9
//                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
//                        }
//                    )
                }
            }
        }
    }
}
@Composable
private fun HomeScreenDialogContent(
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    isZh: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors()
            ) {
                Text(if (isZh) "取消" else "Cancel")
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColorsPrimary()
            ) {
                Text(if (isZh) "确认" else "Confirm")
            }
        }
    }
}

@Composable
fun FavoritesScreen(modifier: Modifier = Modifier) {
    var logContent by remember { mutableStateOf("正在加载日志...") }
    val isCenterText = logContent == "日志文件为空" || logContent == "无法读取日志文件" || logContent == "正在加载日志..."

    // 首次加载日志，之后充电时每 3 秒刷新一次
    LaunchedEffect(Unit) {
        while (true) {
            logContent = ModuleDetector.readLog()
            val batteryInfo = ModuleDetector.readBatteryInfo()
            if (batteryInfo.status == "Charging") {
                kotlinx.coroutines.delay(15000)
            } else {
                break
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            BatteryInfoCard()
        }
        item {
            SectionTitle {
                SmallTitle(
                    text = "日志",
                    modifier = Modifier.offset(y = (8).dp)
                )
            }
        }
        item {
            MiuixCard(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = if (isCenterText) Alignment.Center else Alignment.TopStart
                ) {
                    Text(
                        text = logContent,
                        fontSize = 13.sp,
                        color = if (isCenterText && logContent != "正在加载日志...") 
                                    MiuixTheme.colorScheme.onSurfaceSecondary 
                                else MiuixTheme.colorScheme.onSurface,
                        textAlign = if (isCenterText) TextAlign.Center else TextAlign.Start,
                        modifier = if (isCenterText) Modifier.fillMaxWidth() else Modifier
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    useMonet: Boolean,
    onUseMonetChange: (Boolean) -> Unit,
    onConfigImported: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val packageInfo = remember {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
    val appName = remember {
        context.applicationInfo.loadLabel(context.packageManager).toString()
    }
    val versionName = packageInfo.versionName ?: "1.0"
    val versionCode = packageInfo.longVersionCode
    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    // 定义文件导出 Launcher (保存文件)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
        onResult = { uri ->
            uri?.let {
                scope.launch {
                    val content = ModuleDetector.readConfigRaw()
                    if (content.isNotEmpty()) {
                        context.contentResolver.openOutputStream(it)?.use { stream ->
                            stream.write(content.toByteArray())
                        }
                        Toast.makeText(context, if (isZh) "导出成功" else "Export Success", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, if (isZh) "导出失败：内容为空" else "Export Failed: Empty content", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )

    // 定义文件导入 Launcher (打开文件)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                scope.launch {
                    try {
                        // 1. 先把用户选择的文件复制到 App 缓存区(做一个中转)
                        val tempFile = java.io.File(context.cacheDir, "temp_config_import.prop")
                        context.contentResolver.openInputStream(it)?.use { input ->
                            tempFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        
                        // 2. 调用 Root 命令直接复制到系统目录
                        val success = ModuleDetector.importConfigFile(tempFile.absolutePath)
                        
                        // 3. 删除临时文件
                        tempFile.delete()

                        if (success) {
                            onConfigImported()
                            Toast.makeText(context, if (isZh) "导入成功，配置已重载" else "Import Success, Config reloaded", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, if (isZh) "导入失败" else "Import Failed", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "错误: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )

    // 设备信息
    val deviceModel = android.os.Build.MODEL
    val androidVersion = android.os.Build.VERSION.RELEASE
    val osVersion = android.os.Build.DISPLAY

    // “应用名”的动态配色(用于标题文字 + 关于页背景底色)
    // --- 新背景：HyperOS 3 动态混色模糊 ---
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    var logoHeightDp by remember { mutableStateOf(300.dp) }
    val fadeDistancePx = remember(density) { with(density) { 360.dp.toPx() } }
    val progress by remember {
        derivedStateOf {
            val idx = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset.toFloat()
            val scrollPx = if (idx <= 0) offset else fadeDistancePx
            (scrollPx / fadeDistancePx).coerceIn(0f, 1f)
        }
    }
    val spacerHeightPx = remember(density) { with(density) { 170.dp.toPx() } }
    val aboutProgress by remember {
        derivedStateOf {
            val idx = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset.toFloat()
            if (idx <= 0) (offset / spacerHeightPx).coerceIn(0f, 1f) else 1f
        }
    }
    val shaderSupported = remember { isRuntimeShaderSupported() }
    val backdrop = rememberBlurBackdrop()
    val dynamicBackground = remember { shaderSupported }
    val isInDark = isSystemInDarkTheme()
    var noiseCoefficient by remember { mutableFloatStateOf(BlurDefaults.NoiseCoefficient) }
    val logoBlend = remember(isInDark) {
        if (isInDark) {
            listOf(
                BlendColorEntry(Color(0xe6a1a1a1), BlurBlendMode.ColorDodge),
                BlendColorEntry(Color(0x4de6e6e6), BlurBlendMode.LinearLight),
                BlendColorEntry(Color(0xff1af500), BlurBlendMode.Lab),
            )
        } else {
            listOf(
                BlendColorEntry(Color(0xcc4a4a4a), BlurBlendMode.ColorBurn),
                BlendColorEntry(Color(0xff4f4f4f), BlurBlendMode.LinearLight),
                BlendColorEntry(Color(0xff1af200), BlurBlendMode.Lab),
            )
        }
    }

    BgEffectBackground(
        dynamicBackground = dynamicBackground,
        isOs3Effect = true,
        isFullSize = true,
        modifier = Modifier.fillMaxSize(),
        bgModifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier,
        alpha = { 1f - progress },
    ) {
        // "关于" 标题，滚动时从顶部居中渐显
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 12.dp)
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    alpha = aboutProgress
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "关于",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onBackground,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 170.dp)
                .onSizeChanged { size ->
                    with(density) { logoHeightDp = size.height.toDp() }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(88.dp)
                    .graphicsLayer {
                        val iconProgress = ((progress - 0.35f) / 0.15f).coerceIn(0f, 1f)
                        clip = true
                        shape = RoundedCornerShape(24.dp)
                        alpha = 1 - iconProgress
                        scaleX = 1 - (iconProgress * 0.05f)
                        scaleY = 1 - (iconProgress * 0.05f)
                    }
                    .background(Color.White)
                    .padding(15.dp)
            ) {
                Icon(
                    imageVector = MiuixIcons.Contacts,
                    contentDescription = "App Icon",
                    modifier = Modifier.fillMaxSize(),
                    tint = Color(0xFF9C27B0)
                )
            }
            Text(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .graphicsLayer {
                        val projectNameProgress = ((progress - 0.20f) / 0.15f).coerceIn(0f, 1f)
                        alpha = 1 - projectNameProgress
                        scaleX = 1 - (projectNameProgress * 0.05f)
                        scaleY = 1 - (projectNameProgress * 0.05f)
                    }
                    .then(
                        if (backdrop != null) {
                            Modifier.textureBlur(
                                backdrop = backdrop,
                                shape = RoundedCornerShape(16.dp),
                                blurRadius = 150f,
                                noiseCoefficient = noiseCoefficient,
                                colors = BlurDefaults.blurColors(
                                    blendColors = logoBlend,
                                ),
                                contentBlendMode = ComposeBlendMode.DstIn,
                            )
                        } else {
                            Modifier
                        },
                    ),
                text = appName,
                color = MiuixTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 42.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .graphicsLayer {
                        val versionCodeProgress = ((progress - 0.05f) / 0.15f).coerceIn(0f, 1f)
                        alpha = 1 - versionCodeProgress
                        scaleX = 1 - (versionCodeProgress * 0.05f)
                        scaleY = 1 - (versionCodeProgress * 0.05f)
                    },
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                text = "$versionName ($versionCode) | release",
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(80.dp))
        }

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 170.dp, bottom = 16.dp),
        ) {
            item(key = "logoSpacer") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(
                            logoHeightDp + 80.dp,
                        ),
                    contentAlignment = Alignment.TopCenter,
                    content = { },
                )
            }

            // 底部的小设置项卡片
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    cornerRadius = 24.dp,
                    colors = CardDefaults.defaultColors(
                        // “界面风格”卡片：50% 透明，且随深/浅色使用主题色
                        color = MiuixTheme.colorScheme.surface.copy(alpha = 0.25f),
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    )
                ) {
                    WindowDropdownPreference(
                        items = listOf("MiuiX", "Material"),
                        selectedIndex = if (useMonet) 1 else 0,
                        title = if (isZh) "界面风格" else "UI Style",
                        onSelectedIndexChange = { index ->
                            onUseMonetChange(index == 1)
                        }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    cornerRadius = 24.dp,
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.surface.copy(alpha = 0.25f),
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    )
                ) {
                    val uriHandler = LocalUriHandler.current
                    ArrowPreference(
                        title = if (isZh) "SetoSkins" else "SetoSkins",

                        startAction = {
                            Box(modifier = Modifier.padding(end = 10.dp)) {
                                Image(
                                    painter = painterResource(id = R.drawable.seto),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                )
                            }
                        },
                        onClick = { uriHandler.openUri("https://github.com/SetoSkins") }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    cornerRadius = 24.dp,
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.surface.copy(alpha = 0.25f),
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    )
                ) {
                    ArrowPreference(
                        title = if (isZh) "导出软件配置" else "Export App Config",
                        onClick = {
                            exportLauncher.launch("SetoSkins_配置_备份.prop")
                        }
                    )
                    ArrowPreference(
                        title = if (isZh) "导入软件配置" else "Import App Config",
                        onClick = {
                            importLauncher.launch(arrayOf("*/*"))
                        }
                    )
                    var showResetDialog by remember { mutableStateOf(false) }
                    ArrowPreference(
                        title = if (isZh) "重置模块配置" else "Reset Module Config",
                        onClick = { showResetDialog = true }
                    )
                    OverlayDialog(
                        show = showResetDialog,
                        title = if (isZh) "重置模块配置" else "Reset Module Config",
                        summary = if (isZh) "确定要重置模块配置吗？所有开关将被关闭。" else "Are you sure you want to reset the module config? All switches will be turned off.",
                        onDismissRequest = { showResetDialog = false },
                        content = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { showResetDialog = false },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors()
                                ) {
                                    Text(if (isZh) "取消" else "Cancel")
                                }
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val success = ModuleDetector.resetConfig()
                                            if (success) {
                                                prefs.edit().clear().apply()
                                                kotlinx.coroutines.delay(200)
                                                onConfigImported()
                                                Toast.makeText(context, if (isZh) "已重置并关闭所有开关" else "All configs reset and turned off", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, if (isZh) "重置失败" else "Reset Failed", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        showResetDialog = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColorsPrimary()
                                ) {
                                    Text(if (isZh) "确定" else "Confirm")
                                }
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    cornerRadius = 24.dp,
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.surface.copy(alpha = 0.25f),
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    )
                ) {
                    ArrowPreference(
                        title = if (isZh) "检查更新" else "Check Update"
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceInfoItem(value: String, label: String) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            text = value,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun MiuixCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        content()
    }
}

@Composable
fun MiuixListItem(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                color = MiuixTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("主页", Icons.Filled.Home),
    FAVORITES("日志", MiuixIcons.File),
    PROFILE("关于", MiuixIcons.Info),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemedSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean = true,
    useMonet: Boolean
) {
    val colors = MiuixTheme.colorScheme
    if (useMonet) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,

            thumbContent = {
                Icon(
                    imageVector = if (checked) {
                        Icons.Rounded.Check
                    } else {
                        Icons.Rounded.Close
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },

            colors = SwitchDefaults.colors(

                // 开启
                checkedThumbColor =
                    if (isSystemInDarkTheme()) {
                        colors.primaryContainer
                    } else {
                        Color.White
                    },

                checkedTrackColor = colors.primary,

                checkedIconColor = colors.primary,

                // 关闭(重点)
                uncheckedThumbColor = Color(0xFF7E8785),
                uncheckedTrackColor = Color.Transparent,
                uncheckedBorderColor = Color(0xFF7E8785),
                uncheckedIconColor = Color.White,

                // 禁用
                disabledCheckedThumbColor = Color.White.copy(alpha = 0.5f),
                disabledCheckedTrackColor = colors.primary.copy(alpha = 0.3f),

                disabledUncheckedThumbColor = Color(0xFFBEBEBE),
                disabledUncheckedTrackColor = Color.Transparent,
                disabledUncheckedBorderColor = Color(0xFFD8D8D8),
                disabledUncheckedIconColor = Color.White.copy(alpha = 0.7f)
            ),

            modifier = Modifier.scale(1.02f)
        )
    } else {
        MiuixSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}
@Composable
fun SectionTitle(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .offset(x = (-12).dp)
            .layout { measurable, constraints ->

                val placeable =
                    measurable.measure(constraints)

                layout(
                    placeable.width,
                    placeable.height - 8.dp.roundToPx()
                ) {

                    placeable.place(0, 0)
                }
            }
    ) {
        content()
    }
}
/**
 * 黄色"检测到更新"卡片。
 * - 设计参考红色卡片,但使用琥珀色/黄色系
 */
@Composable
fun YellowUpdateCard() {
    val uriHandler = LocalUriHandler.current
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) {
        Color(0xFF3E2C00) // 深棕黄
    } else {
        Color(0xFFFFF8E1) // 浅奶油黄
    }
    val titleColor = if (isDark) {
        Color.White
    } else {
        Color(0xFF855A00) // 深琥珀
    }
    val subColor = if (isDark) {
        Color(0xFFFFE082)
    } else {
        Color(0xFFB08900)
    }
    val iconColor = if (isDark) {
        Color(0xFFFFD54F)
    } else {
        Color(0xFFFFB300)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .clickable {
                uriHandler.openUri("https://github.com/SetoSkins/SetoSkins_Thermal/releases")
            }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "检测到更新",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "新版本已发布,点击去下载",
                    fontSize = 14.sp,
                    color = subColor
                )
            }
            Canvas(modifier = Modifier.size(32.dp)) {
                val strokeWidth = 2.dp.toPx()
                drawCircle(
                    color = iconColor,
                    radius = (size.minDimension - strokeWidth) / 2,
                    style = Stroke(width = strokeWidth),
                    center = center
                )
                // 绘制一个简单的向下箭头
                val arrowPath = Path().apply {
                    moveTo(center.x, center.y - 6.dp.toPx())
                    lineTo(center.x, center.y + 6.dp.toPx())
                    moveTo(center.x - 4.dp.toPx(), center.y + 2.dp.toPx())
                    lineTo(center.x, center.y + 6.dp.toPx())
                    lineTo(center.x + 4.dp.toPx(), center.y + 2.dp.toPx())
                }
                drawPath(
                    path = arrowPath,
                    color = iconColor,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

/**
 * 红色"未安装"卡片。
 * - 仅 MIUIX 主题:深色模式用深红黑,浅色模式用淡红粉(同色系更亮)
 * - 整张卡片可点击,跳转到 GitHub releases
 */
@Composable
fun RedNotInstalledCard() {
    val uriHandler = LocalUriHandler.current
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) {
        Color(0xFF3D0C0C)
    } else {
        Color(0xFFFFE5E5)
    }
    val titleColor = if (isDark) {
        Color.White
    } else {
        Color(0xFF8A1A1A)
    }
    val subColor = if (isDark) {
        Color(0xFFE0E0E0)
    } else {
        Color(0xFFB33A3A)
    }
    val iconColor = if (isDark) {
        Color(0xFFFF4444)
    } else {
        Color(0xFFD32F2F)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .clickable {
                uriHandler.openUri("https://github.com/SetoSkins/SetoSkins_Thermal/releases")
            }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "未激活",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "模块未安装",
                    fontSize = 14.sp,
                    color = subColor
                )
            }
            Canvas(modifier = Modifier.size(32.dp)) {
                val strokeWidth = 2.dp.toPx()
                val circleColor = iconColor
                drawCircle(
                    color = circleColor,
                    radius = (size.minDimension - strokeWidth) / 2,
                    style = Stroke(width = strokeWidth),
                    center = center
                )
                drawLine(
                    color = circleColor,
                    start = Offset(center.x, center.y - 6.dp.toPx()),
                    end = Offset(center.x, center.y + 2.dp.toPx()),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = circleColor,
                    radius = 1.5.dp.toPx(),
                    center = Offset(center.x, center.y + 7.dp.toPx())
                )
            }
        }
    }
}

/**
 * 绿色"已激活"卡片。
 * - MIUIX 主题:深色模式用深绿黑 + 亮绿对勾;浅色模式用淡薄荷绿底 + 深绿对勾
 * - Material 主题:Monet 二次色容器(secondaryContainer,跟随系统壁纸取色)
 *   参考 SukiSU Ultra 卡片样式
 */
@Composable
fun GreenActivatedCard(useMonet: Boolean, version: String) {
    val isDark = isSystemInDarkTheme()
    val miuixColors = MiuixTheme.colorScheme

    val greenAccent = if (isDark) {
        Color(0xFF34C759)
    } else {
        Color(0xFF1B7A3A)
    }
    val greenContainerBg = if (isDark) {
        Color(0xFF1A3A24)
    } else {
        Color(0xFFE0F5E5)
    }
    val greenTitleColor = if (isDark) {
        Color.White
    } else {
        Color(0xFF0F5128)
    }
    val greenSubColor = if (isDark) {
        Color(0xFFBFE9CC)
    } else {
        Color(0xFF2E7D3A)
    }

    // 决定背景、文字色和 icon 样式
    val containerBg = if (useMonet) {
        // Material 主题: 使用映射后的 Monet 颜色，避免获取延迟
        miuixColors.secondaryContainer
    } else {
        greenContainerBg
    }
    val onContainer = if (useMonet) {
        miuixColors.onSecondaryContainer
    } else {
        greenTitleColor
    }
    val onContainerSub = if (useMonet) {
        miuixColors.onSurfaceSecondary
    } else {
        greenSubColor
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerBg)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "已激活",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = onContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = version.ifEmpty { "检测中..." },
                    fontSize = 14.sp,
                    color = onContainerSub
                )
            }
            if (useMonet) {
                // Material 主题:32dp 全圆 + 对勾,主色用莫奈主色
                val circleColor = miuixColors.primary
                Canvas(modifier = Modifier.size(32.dp)) {
                    val strokeWidth = 2.dp.toPx()
                    drawArc(
                        color = circleColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        ),
                        size = Size(
                            size.width - strokeWidth,
                            size.height - strokeWidth
                        ),
                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    )
                    val checkPath = Path().apply {
                        moveTo(center.x - 5.dp.toPx(), center.y)
                        lineTo(center.x - 1.dp.toPx(), center.y + 4.dp.toPx())
                        lineTo(center.x + 6.dp.toPx(), center.y - 4.dp.toPx())
                    }
                    drawPath(
                        path = checkPath,
                        color = circleColor,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            } else {
                // MIUIX 主题:32dp 270° 缺角圆弧 + 对勾(原版样式)
                Canvas(modifier = Modifier.size(32.dp)) {
                    val strokeWidth = 2.dp.toPx()
                    drawArc(
                        color = greenAccent,
                        startAngle = 90f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        ),
                        size = Size(
                            size.width - strokeWidth,
                            size.height - strokeWidth
                        ),
                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    )
                    val checkPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(center.x - 5.dp.toPx(), center.y)
                        lineTo(center.x - 1.dp.toPx(), center.y + 4.dp.toPx())
                        lineTo(center.x + 6.dp.toPx(), center.y - 4.dp.toPx())
                    }
                    drawPath(
                        path = checkPath,
                        color = greenAccent,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun BatteryInfoCard() {
    var info by remember { mutableStateOf(ModuleDetector.BatteryInfo()) }

    LaunchedEffect(Unit) {
        while (true) {
            info = ModuleDetector.readBatteryInfo()
            kotlinx.coroutines.delay(3000)
        }
    }

    val tempText = info.temperature.let {
        if (it.isNotEmpty()) {
            try { "${"%.1f".format(it.toInt() / 10.0)}°C" } catch (_: Exception) { "${it}°C" }
        } else "..."
    }
    val currentText = info.current.let {
        if (it.isNotEmpty()) {
            try { "${it.toInt() / -1000} mA" } catch (_: Exception) { "${it} mA" }
        } else "..."
    }
    val capacityText = info.capacity.let { if (it.isNotEmpty()) "${it}%" else "..." }
    val statusText = when (info.status) {
        "Charging" -> "充电中"
        "Discharging" -> "放电中"
        "Not charging" -> "未充电"
        "Full" -> "已充满"
        "Unknown" -> "未知"
        else -> info.status.ifEmpty { "..." }
    }

    MiuixCard {
        Column(modifier = Modifier.padding(start = 16.dp, top = 2.dp, end = 16.dp, bottom = 16.dp)) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BatteryStatItem(label = "温度", value = tempText, modifier = Modifier.weight(1f))
                BatteryStatItem(label = "电流", value = currentText, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BatteryStatItem(label = "电量", value = capacityText, modifier = Modifier.weight(1f))
                BatteryStatItem(label = "充电状态", value = statusText, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun BatteryStatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

    MyApplicationTheme(
        useMonet = false
    ) {

        MyApplicationApp(
            useMonet = false,
            onUseMonetChange = {}
        )
    }
}
fun Modifier.compactSmallTitle(): Modifier = this
    .padding(start = 4.dp)
    .offset(x = (-13).dp, y = 11.dp)
    .layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val reduce = 24.dp.roundToPx()

        layout(
            placeable.width,
            (placeable.height - reduce).coerceAtLeast(1)
        ) {
            placeable.place(0, -reduce)
        }
    }
 

