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
import androidx.compose.foundation.border
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
import androidx.compose.animation.core.Animatable
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
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.BlendMode as ComposeBlendMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.PlatformTextStyle
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import com.setoskins.thermal.data.ModuleDetector
import com.setoskins.thermal.data.ThemePreferences
import com.setoskins.thermal.ui.theme.MyApplicationTheme
import com.setoskins.thermal.R
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import android.app.NotificationManager
import android.provider.Settings
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.setoskins.thermal.service.SuperIslandService
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.ui.graphics.RectangleShape
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.preference.ArrowPreference

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val savedUseMonet = prefs.getBoolean("useMonet", false)

        setContent {
            var useMonet by remember { mutableStateOf(savedUseMonet) }
            MyApplicationTheme(useMonet = useMonet) {
                MyApplicationApp(
                    useMonet = useMonet,
                    onUseMonetChange = { value ->
                        useMonet = value
                        prefs.edit().putBoolean("useMonet", value).apply()
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
    val scope = rememberCoroutineScope()
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var reloadTrigger by remember { mutableIntStateOf(0) }
    var logReloadTrigger by remember { mutableIntStateOf(0) }
    var rootState by remember { mutableStateOf<Boolean?>(null) }
    var showDonatePage by remember { mutableStateOf(false) }
    var showDonateLayer by remember { mutableStateOf(false) }
    val transitionProgress = remember { Animatable(0f) }
    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"

    LaunchedEffect(showDonatePage) {
        if (showDonatePage) {
            showDonateLayer = true
            transitionProgress.animateTo(1f, tween(480, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
        } else {
            transitionProgress.animateTo(0f, tween(320, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
            showDonateLayer = false
        }
    }

    PredictiveBackHandler(enabled = showDonatePage) { progress ->
        try {
            progress.collect { backEvent ->
                // Map gesture 0..1 to transition 1..0
                transitionProgress.snapTo(1f - backEvent.progress)
            }
            showDonatePage = false
        } catch (e: Exception) {
            // Cancelled: animate back to 1
            scope.launch {
                transitionProgress.animateTo(1f, tween(400, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
            }
        }
    }

    LaunchedEffect(Unit) {
        rootState = ModuleDetector.requestRoot()
    }

    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())
    val backdrop = rememberBlurBackdrop()
    val shaderSupported = remember { isRuntimeShaderSupported() }
    val reduceExpensiveEffects = showDonatePage || showDonateLayer
    val showBlur = useMonet && shaderSupported && backdrop != null && !reduceExpensiveEffects
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(useMonet = useMonet)
        Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Previous page slides out to the left as overlay opens
                        translationX = -screenWidth.toPx() * 0.18f * transitionProgress.value
                    },
                topBar = {
                    if (currentDestination != AppDestinations.PROFILE) {
                        BlurredBar(backdrop, showBlur) {
                            TopAppBar(
                                title = currentDestination.label,
                                largeTitle = currentDestination.label,
                                scrollBehavior = scrollBehavior,
                                color = if (showBlur) Color.Transparent else MiuixTheme.colorScheme.surface,
                                actions = {
                                    if (currentDestination == AppDestinations.FAVORITES) {
                                        val haptic = LocalHapticFeedback.current
                                        IconButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                scope.launch {
                                                    if (ModuleDetector.clearLog()) {
                                                        logReloadTrigger++
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = MiuixIcons.Delete,
                                                contentDescription = "Clear Log",
                                                tint = MiuixTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
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
                        val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                        val slideAnimation = tween<IntOffset>(durationMillis = 320, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))
                        slideInHorizontally(animationSpec = slideAnimation, initialOffsetX = { it * direction }) + fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) togetherWith
                            slideOutHorizontally(animationSpec = slideAnimation, targetOffsetX = { -it * direction }) + fadeOut(animationSpec = tween(220, easing = LinearOutSlowInEasing))
                    },
                    label = "page_transition"
                ) { destination ->
                    when (destination) {
                        AppDestinations.HOME -> {
                            HomeScreen(
                                useMonet = useMonet,
                                reloadTrigger = reloadTrigger,
                                modifier = Modifier.padding(innerPadding).overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection)
                            )
                        }
                        AppDestinations.FAVORITES -> {
                            FavoritesScreen(
                                reloadTrigger = logReloadTrigger,
                                modifier = Modifier.padding(innerPadding).overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection)
                            )
                        }
                        AppDestinations.PROFILE -> {
                            ProfileScreen(
                                useMonet = useMonet,
                                onUseMonetChange = onUseMonetChange,
                                onConfigImported = { reloadTrigger++ },
                                onNavigateToDonate = { showDonatePage = true },
                                reduceEffects = reduceExpensiveEffects,
                                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()).overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection)
                            )
                        }
                    }
                }
            }

        if (showDonateLayer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 0.22f * transitionProgress.value
                    }
                    .background(Color.Black)
            )
        }

        if (rootState == false) {
            OverlayDialog(
                show = true,
                title = if (isZh) "权限缺失" else "Root Permission Required",
                summary = if (isZh) "检测到设备未获取 Root 权限或拒绝了授权，本软件无法正常工作，请授权后重新进入。" else "Root access was not detected or was denied. This app requires Root to function properly. Please grant Root permission and reopen the app.",
                onDismissRequest = { },
                content = {
                    BackHandler(enabled = true) { }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = { (context as? android.app.Activity)?.finish() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColorsPrimary()) {
                            Text(if (isZh) "退出应用" else "Exit App")
                        }
                    }
                }
            )
        }
        if (showDonateLayer) {
            DonatePage(
                useMonet = useMonet,
                onDismiss = { showDonatePage = false },
                progressProvider = { transitionProgress.value }
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
        val navModifier = if (showBlur) modifier.fillMaxWidth().then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) else modifier.fillMaxWidth()
        NavigationBar(modifier = navModifier, containerColor = miuixColors.surface.copy(alpha = barAlpha), contentColor = miuixColors.onSurface) {
            AppDestinations.entries.forEach { destination ->
                NavigationBarItem(
                    icon = { Icon(imageVector = destination.icon, contentDescription = destination.label) },
                    label = { androidx.compose.material3.Text(text = destination.label) },
                    selected = destination == currentDestination,
                    onClick = { if (destination != currentDestination) onDestinationSelected(destination) },
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
        MiuixNavigationBar(modifier = modifier.fillMaxWidth(), color = MiuixTheme.colorScheme.surface.copy(alpha = barAlpha)) {
            AppDestinations.entries.forEach { destination ->
                MiuixNavigationBarItem(icon = destination.icon, label = destination.label, selected = destination == currentDestination, onClick = { if (destination != currentDestination) onDestinationSelected(destination) })
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
    var switch5 by remember { mutableStateOf(prefs.getBoolean("switch5", false)) }
    var switch16 by remember { mutableStateOf(prefs.getBoolean("switch16", false)) }
    val isChargingState = remember { mutableStateOf(false) }

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

    // Monitor charging status while app is open
    LaunchedEffect(Unit) {
        while (true) {
            val batteryInfo = ModuleDetector.readBatteryInfo()
            val nowCharging = batteryInfo.status == "Charging"
            if (nowCharging != isChargingState.value) {
                isChargingState.value = nowCharging
                if (switch16) {
                    val intent = Intent(context, SuperIslandService::class.java)
                    if (nowCharging) {
                        ContextCompat.startForegroundService(context, intent)
                    } else {
                        context.stopService(intent)
                    }
                }
            }
            kotlinx.coroutines.delay(5000)
        }
    }

    var switch15 by remember { mutableStateOf(prefs.getBoolean("switch15", false)) }
    var switch6 by remember { mutableStateOf(prefs.getBoolean("switch6", false)) }
    var switch7 by remember { mutableStateOf(prefs.getBoolean("switch7", false)) }
    var switch14 by remember { mutableStateOf(prefs.getBoolean("switch14", false)) }
    var switch8 by remember { mutableStateOf(prefs.getBoolean("switch8", false)) }
    var switch9 by remember { mutableStateOf(prefs.getBoolean("switch9", false)) }
    var switch10 by remember { mutableStateOf(prefs.getBoolean("switch10", false)) }
    var switch11 by remember { mutableStateOf(prefs.getBoolean("switch11", false)) }
    var switch12 by remember { mutableStateOf(prefs.getBoolean("switch12", false)) }
    var switch13 by remember { mutableStateOf(prefs.getBoolean("switch13", false)) }

    var limit1Temp by rememberSaveable { mutableStateOf(prefs.getString("limit1Temp", "40") ?: "40") }
    var limit1Current by rememberSaveable { mutableStateOf(prefs.getString("limit1Current", "") ?: "") }
    var limit2Temp by rememberSaveable { mutableStateOf(prefs.getString("limit2Temp", "43") ?: "43") }
    var limit2Current by rememberSaveable { mutableStateOf(prefs.getString("limit2Current", "") ?: "") }
    var limit3Temp by rememberSaveable { mutableStateOf(prefs.getString("limit3Temp", "46") ?: "46") }
    var limit3Current by rememberSaveable { mutableStateOf(prefs.getString("limit3Current", "") ?: "") }
    var delayTempThreshold by rememberSaveable { mutableStateOf(prefs.getString("delayTempThreshold", "10") ?: "10") }

    var step1Level by rememberSaveable { mutableStateOf(prefs.getString("step1Level", "20") ?: "20") }
    var step1Current by rememberSaveable { mutableStateOf(prefs.getString("step1Current", "") ?: "") }
    var step2Level by rememberSaveable { mutableStateOf(prefs.getString("step2Level", "50") ?: "50") }
    var step2Current by rememberSaveable { mutableStateOf(prefs.getString("step2Current", "") ?: "") }
    var step3Level by rememberSaveable { mutableStateOf(prefs.getString("step3Level", "80") ?: "80") }
    var step3Current by rememberSaveable { mutableStateOf(prefs.getString("step3Current", "") ?: "") }

    var stopChargeLevel by rememberSaveable { mutableStateOf(prefs.getString("stopChargeLevel", "80") ?: "80") }
    var resumeChargeLevel by rememberSaveable { mutableStateOf(prefs.getString("resumeChargeLevel", "75") ?: "75") }
    var stopChargeCurrent by rememberSaveable { mutableStateOf(prefs.getString("stopChargeCurrent", "500") ?: "500") }

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

    var moduleInstalled by remember { mutableStateOf(false) }
    var hasUpdate by remember { mutableStateOf(false) }
    var moduleVersion by remember { mutableStateOf("") }

    LaunchedEffect(reloadTrigger) {
        launch { moduleInstalled = ModuleDetector.isModuleInstalled() }
        launch { moduleVersion = ModuleDetector.getModuleVersion() }
        launch { hasUpdate = ModuleDetector.checkUpdate() }
        launch {
            val externalConfig = ModuleDetector.readConfig()
            val editor = prefs.edit()
            fun syncSwitch(key: String, currentVal: Boolean, update: (Boolean) -> Unit, prefKey: String) {
                val fileVal = externalConfig[key]?.trim()?.lowercase() == "true"
                if (currentVal != fileVal) { update(fileVal); editor.putBoolean(prefKey, fileVal) }
            }
            fun syncText(key: String, currentVal: String, update: (String) -> Unit, prefKey: String) {
                val raw = externalConfig[key]?.trim()
                val fileVal = if (raw == null || raw.equals("false", ignoreCase = true)) "" else raw
                if (currentVal != fileVal) { update(fileVal); editor.putString(prefKey, fileVal) }
            }
            syncSwitch("快充模式", switch2, { switch2 = it }, "switch2")
            syncSwitch("温控空挂载模式", switch3, { switch3 = it }, "switch3")
            syncSwitch("修改最大电流数", switch4, { switch4 = it }, "switch4")
            syncSwitch("模块简介显示充电信息", switch5, { switch5 = it }, "switch5")
            // 灵动岛是纯软件选项，不与 config.prop 同步
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

    fun updateSwitch(prefKey: String, configKey: String, newValue: Boolean, setter: (Boolean) -> Unit) {
        setter(newValue); prefs.edit().putBoolean(prefKey, newValue).apply()
        scope.launch { ModuleDetector.updateConfig(configKey, newValue); ModuleDetector.executeThermalScript() }
    }
    fun updateText(prefKey: String, configKey: String, newValue: String, setter: (String) -> Unit) {
        setter(newValue); prefs.edit().putString(prefKey, newValue).apply()
        scope.launch { ModuleDetector.updateConfig(configKey, newValue) }
    }

    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item { if (!moduleInstalled) RedNotInstalledCard() else if (hasUpdate) YellowUpdateCard() else GreenActivatedCard(useMonet = useMonet, version = moduleVersion) }
        item { SmallTitle(text = "配置", modifier = Modifier.offset(x = (-12).dp).layout { measurable, constraints -> val placeable = measurable.measure(constraints); layout(placeable.width, placeable.height - 8.dp.roundToPx()) { placeable.place(0, 0) } }) }
        item { MiuixCard { Column(modifier = Modifier.padding(vertical = 4.dp)) { BasicComponent(title = "简洁版配置", summary = "目前无法更改", endActions = { ThemedSwitch(checked = false, onCheckedChange = null, enabled = false, useMonet = useMonet) })
                    BasicComponent(title = "模块简介显示充电信息", summary = "Magisk/KSU里显示电流、电量等充电信息,可能耗一丢丢电", endActions = { ThemedSwitch(checked = switch5, onCheckedChange = null, useMonet = useMonet) }, onClick = { updateSwitch("switch5", "模块简介显示充电信息", !switch5) { switch5 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                    BasicComponent(title = "充电时显示灵动岛", summary = "充电时在屏幕显示灵动岛风格充电信息（需后台运行）", endActions = { ThemedSwitch(checked = switch16, onCheckedChange = null, useMonet = useMonet) }, onClick = { switch16 = !switch16; prefs.edit().putBoolean("switch16", switch16).apply(); hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) }) } } }
        item { Spacer(modifier = Modifier.height(12.dp)); SmallTitle(text = "温控", modifier = Modifier.padding(start = 4.dp).offset(x = (-13).dp, y = (11).dp).layout { measurable, constraints -> val placeable = measurable.measure(constraints); val reduce = 24.dp.roundToPx(); layout(placeable.width, (placeable.height - reduce).coerceAtLeast(1)) { placeable.place(0, -reduce) } }) }
        item { MiuixCard { BasicComponent(title = "快充模式", endActions = { Row(verticalAlignment = Alignment.CenterVertically) { Text(text = if (switch2) "True" else "False", fontSize = 17.sp, color = if (switch2) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceSecondary); Spacer(modifier = Modifier.width(8.dp)); ThemedSwitch(checked = switch2, onCheckedChange = null, useMonet = useMonet) } }, onClick = { updateSwitch("switch2", "快充模式", !switch2) { switch2 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                Column(modifier = Modifier.padding(vertical = 4.dp)) { BasicComponent(title = "温控空挂载模式", summary = "非必要建议不开启此选项", endActions = { ThemedSwitch(checked = switch3, onCheckedChange = null, useMonet = useMonet) }, onClick = { updateSwitch("switch3", "温控空挂载模式", !switch3) { switch3 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                    BasicComponent(title = "还原均衡模式温控", enabled = switch15 || !switch6, endActions = { ThemedSwitch(checked = switch15, onCheckedChange = null, enabled = switch15 || !switch6, useMonet = useMonet) }, onClick = { if (switch15 || !switch6) { updateSwitch("switch15", "还原均衡模式温控", !switch15) { switch15 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) } })
                    BasicComponent(title = "还原性能模式温控", enabled = switch6 || !switch15, endActions = { ThemedSwitch(checked = switch6, onCheckedChange = null, enabled = switch6 || !switch15, useMonet = useMonet) }, onClick = { if (switch6 || !switch15) { updateSwitch("switch6", "还原性能模式温控", !switch6) { switch6 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) } })
                    BasicComponent(title = "游戏均衡式性能温控", summary = "把游戏中均衡模式的温控改成性能模式的原有温控,性能模式则无温控", endActions = { ThemedSwitch(checked = switch7, onCheckedChange = null, useMonet = useMonet) }, onClick = { updateSwitch("switch7", "游戏均衡式性能温控", !switch7) { switch7 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                    BasicComponent(title = "系统均衡式性能温控", summary = "把系统中均衡模式的温控改成性能模式的原有温控,性能模式则无温控", endActions = { ThemedSwitch(checked = switch14, onCheckedChange = null, useMonet = useMonet) }, onClick = { updateSwitch("switch14", "系统均衡式性能温控", !switch14) { switch14 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) }) } } }
        item { Spacer(modifier = Modifier.height(12.dp)); SmallTitle(text = "调速 (22000mA＝22000000μA)(重启生效)", modifier = Modifier.compactSmallTitle()) }
        item { MiuixCard { Column { BasicComponent(title = "修改最大电流数", enabled = switch4 || (!switch11 && !switch13 && !switch10), endActions = { ThemedSwitch(checked = switch4, onCheckedChange = null, enabled = switch4 || (!switch11 && !switch13 && !switch10), useMonet = useMonet) }, onClick = { if (switch4 || (!switch11 && !switch13 && !switch10)) { updateSwitch("switch4", "修改最大电流数", !switch4) { switch4 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) } })
                    AnimatedVisibility(visible = switch4, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) { Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) { if (useMonet) OutlinedTextField(value = currentValue, onValueChange = { updateText("currentValue", "最大电流数", it) { currentValue = it } }, modifier = Modifier.fillMaxWidth(), label = { Text("22A＝22000mA＝22000000") }, singleLine = true, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MiuixTheme.colorScheme.primary, focusedLabelColor = MiuixTheme.colorScheme.primary, cursorColor = MiuixTheme.colorScheme.primary, focusedTextColor = MiuixTheme.colorScheme.onSurface)) else TextField(value = currentValue, onValueChange = { updateText("currentValue", "最大电流数", it) { currentValue = it } }, modifier = Modifier.fillMaxWidth(), label = "22A＝22000mA＝22000000", singleLine = true) } }
                    BasicComponent(title = "充电调速", enabled = switch11 || (!switch4 && !switch13 && !switch10), endActions = { ThemedSwitch(checked = switch11, onCheckedChange = null, enabled = switch11 || (!switch4 && !switch13 && !switch10), useMonet = useMonet) }, onClick = { if (switch11 || (!switch4 && !switch13 && !switch10)) { updateSwitch("switch11", "充电调速", !switch11) { switch11 = it }; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) } })
                    AnimatedVisibility(visible = switch11, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) { Column { BasicComponent(title = "一限温度阈值", endActions = { Text(text = "$limit1Temp°C", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary); Spacer(modifier = Modifier.width(10.dp)); Image(modifier = Modifier.size(width = 10.dp, height = 16.dp).align(Alignment.CenterVertically), imageVector = MiuixIcons.Basic.ArrowRight, contentDescription = null, colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions)) }, onClick = { dialogInputValue = limit1Temp; showLimit1Dialog = true; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                            Slider(value = limit1Temp.toFloatOrNull() ?: 40f, onValueChange = { limit1Temp = it.toInt().toString() }, valueRange = 20f..50f, steps = 30, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp))
                            if (useMonet) OutlinedTextField(value = limit1Current, onValueChange = { limit1Current = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), label = { Text("一限限制电流") }, singleLine = true, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MiuixTheme.colorScheme.primary, focusedLabelColor = MiuixTheme.colorScheme.primary, cursorColor = MiuixTheme.colorScheme.primary, focusedTextColor = MiuixTheme.colorScheme.onSurface)) else TextField(value = limit1Current, onValueChange = { limit1Current = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), label = "一限限制电流", singleLine = true) } }
                    AnimatedVisibility(visible = switch11, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) { Column { BasicComponent(title = "二限温度阈值", endActions = { Text(text = "$limit2Temp°C", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary); Spacer(modifier = Modifier.width(10.dp)); Image(modifier = Modifier.size(width = 10.dp, height = 16.dp).align(Alignment.CenterVertically), imageVector = MiuixIcons.Basic.ArrowRight, contentDescription = null, colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions)) }, onClick = { dialogInputValue = limit2Temp; showLimit2Dialog = true; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                            Slider(value = limit2Temp.toFloatOrNull() ?: 43f, onValueChange = { limit2Temp = it.toInt().toString() }, valueRange = 20f..50f, steps = 30, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp))
                            if (useMonet) OutlinedTextField(value = limit2Current, onValueChange = { limit2Current = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), label = { Text("二限限制电流") }, singleLine = true, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MiuixTheme.colorScheme.primary, focusedLabelColor = MiuixTheme.colorScheme.primary, cursorColor = MiuixTheme.colorScheme.primary, focusedTextColor = MiuixTheme.colorScheme.onSurface)) else TextField(value = limit2Current, onValueChange = { limit2Current = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), label = "二限限制电流", singleLine = true) } }
                    AnimatedVisibility(visible = switch11, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) { Column { BasicComponent(title = "三限温度阈值", endActions = { Text(text = "$limit3Temp°C", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary); Spacer(modifier = Modifier.width(10.dp)); Image(modifier = Modifier.size(width = 10.dp, height = 16.dp).align(Alignment.CenterVertically), imageVector = MiuixIcons.Basic.ArrowRight, contentDescription = null, colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions)) }, onClick = { dialogInputValue = limit3Temp; showLimit3Dialog = true; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                            Slider(value = limit3Temp.toFloatOrNull() ?: 46f, onValueChange = { limit3Temp = it.toInt().toString() }, valueRange = 20f..50f, steps = 30, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp))
                            if (useMonet) OutlinedTextField(value = limit3Current, onValueChange = { limit3Current = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), label = { Text("三限限制电流") }, singleLine = true, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MiuixTheme.colorScheme.primary, focusedLabelColor = MiuixTheme.colorScheme.primary, cursorColor = MiuixTheme.colorScheme.primary, focusedTextColor = MiuixTheme.colorScheme.onSurface)) else TextField(value = limit3Current, onValueChange = { limit3Current = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), label = "三限限制电流", singleLine = true)
                            BasicComponent(title = "延迟温度阈值", summary = "为了防止电流卡在限制电流，需要设置延迟温度阈值。", endActions = { Text(text = "$delayTempThreshold S", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary); Spacer(modifier = Modifier.width(10.dp)) })
                            Slider(value = delayTempThreshold.toFloatOrNull() ?: 10f, onValueChange = { delayTempThreshold = it.toInt().toString() }, valueRange = 5f..30f, steps = 25, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) } }
                    BasicComponent(title = "自定义阶梯模式", enabled = switch13 || (!switch4 && !switch11 && !switch10), endActions = { ThemedSwitch(checked = switch13, onCheckedChange = null, enabled = switch13 || (!switch4 && !switch11 && !switch10), useMonet = useMonet) }, onClick = { if (switch13 || (!switch4 && !switch11 && !switch10)) { switch13 = !switch13; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) } })
                    AnimatedVisibility(visible = switch13, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) { Column { BasicComponent(title = "一限电量阈值", endActions = { Text(text = "$step1Level %", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary); Spacer(modifier = Modifier.width(10.dp)); Image(modifier = Modifier.size(width = 10.dp, height = 16.dp).align(Alignment.CenterVertically), imageVector = MiuixIcons.Basic.ArrowRight, contentDescription = null, colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions)) }, onClick = { dialogInputValue = step1Level; showStep1Dialog = true; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                            Slider(value = step1Level.toFloatOrNull() ?: 20f, onValueChange = { step1Level = it.toInt().toString() }, valueRange = 0f..100f, steps = 100, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp))
                            if (useMonet) OutlinedTextField(value = step1Current, onValueChange = { step1Current = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), label = { Text("一限电量限制电流") }, singleLine = true, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MiuixTheme.colorScheme.primary, focusedLabelColor = MiuixTheme.colorScheme.primary, cursorColor = MiuixTheme.colorScheme.primary, focusedTextColor = MiuixTheme.colorScheme.onSurface)) else TextField(value = step1Current, onValueChange = { step1Current = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), label = "一限电量限制电流", singleLine = true) } }
                    AnimatedVisibility(visible = switch13, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) { Column { BasicComponent(title = "二限电量阈值", endActions = { Text(text = "$step2Level %", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary); Spacer(modifier = Modifier.width(10.dp)); Image(modifier = Modifier.size(width = 10.dp, height = 16.dp).align(Alignment.CenterVertically), imageVector = MiuixIcons.Basic.ArrowRight, contentDescription = null, colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions)) }, onClick = { dialogInputValue = step2Level; showStep2Dialog = true; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                            Slider(value = step2Level.toFloatOrNull() ?: 50f, onValueChange = { step2Level = it.toInt().toString() }, valueRange = 0f..100f, steps = 100, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp))
                            if (useMonet) OutlinedTextField(value = step2Current, onValueChange = { step2Current = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), label = { Text("二限电量限制电流") }, singleLine = true, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MiuixTheme.colorScheme.primary, focusedLabelColor = MiuixTheme.colorScheme.primary, cursorColor = MiuixTheme.colorScheme.primary, focusedTextColor = MiuixTheme.colorScheme.onSurface)) else TextField(value = step2Current, onValueChange = { step2Current = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), label = "二限电量限制电流", singleLine = true) } }
                    AnimatedVisibility(visible = switch13, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) { Column { BasicComponent(title = "三限电量阈值", endActions = { Text(text = "$step3Level %", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary); Spacer(modifier = Modifier.width(10.dp)); Image(modifier = Modifier.size(width = 10.dp, height = 16.dp).align(Alignment.CenterVertically), imageVector = MiuixIcons.Basic.ArrowRight, contentDescription = null, colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions)) }, onClick = { dialogInputValue = step3Level; showStep3Dialog = true; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                            Slider(value = step3Level.toFloatOrNull() ?: 80f, onValueChange = { step3Level = it.toInt().toString() }, valueRange = 0f..100f, steps = 100, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp))
                            if (useMonet) OutlinedTextField(value = step3Current, onValueChange = { step3Current = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), label = { Text("三限电量限制电流") }, singleLine = true, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MiuixTheme.colorScheme.primary, focusedLabelColor = MiuixTheme.colorScheme.primary, cursorColor = MiuixTheme.colorScheme.primary, focusedTextColor = MiuixTheme.colorScheme.onSurface)) else TextField(value = step3Current, onValueChange = { step3Current = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), label = "三限电量限制电流", singleLine = true) } }
                    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"
                    OverlayDialog(show = showLimit1Dialog, title = "调整一限温度阈值", summary = "输入温度 (20-50 °C)", onDismissRequest = { showLimit1Dialog = false }, content = { HomeScreenDialogContent(value = dialogInputValue, onValueChange = { dialogInputValue = it }, onConfirm = { val v = dialogInputValue.toIntOrNull(); if (v != null && v in 20..50) limit1Temp = v.toString(); showLimit1Dialog = false }, onCancel = { showLimit1Dialog = false }, isZh = isZh) })
                    OverlayDialog(show = showLimit2Dialog, title = "调整二限温度阈值", summary = "输入温度 (20-50 °C)", onDismissRequest = { showLimit2Dialog = false }, content = { HomeScreenDialogContent(value = dialogInputValue, onValueChange = { dialogInputValue = it }, onConfirm = { val v = dialogInputValue.toIntOrNull(); if (v != null && v in 20..50) limit2Temp = v.toString(); showLimit2Dialog = false }, onCancel = { showLimit2Dialog = false }, isZh = isZh) })
                    OverlayDialog(show = showLimit3Dialog, title = "调整三限温度阈值", summary = "输入温度 (20-50 °C)", onDismissRequest = { showLimit3Dialog = false }, content = { HomeScreenDialogContent(value = dialogInputValue, onValueChange = { dialogInputValue = it }, onConfirm = { val v = dialogInputValue.toIntOrNull(); if (v != null && v in 20..50) limit3Temp = v.toString(); showLimit3Dialog = false }, onCancel = { showLimit3Dialog = false }, isZh = isZh) })
                    OverlayDialog(show = showStep1Dialog, title = "调整一限电量阈值", summary = "输入电量 (0-100 %)", onDismissRequest = { showStep1Dialog = false }, content = { HomeScreenDialogContent(value = dialogInputValue, onValueChange = { dialogInputValue = it }, onConfirm = { val v = dialogInputValue.toIntOrNull(); if (v != null && v in 0..100) step1Level = v.toString(); showStep1Dialog = false }, onCancel = { showStep1Dialog = false }, isZh = isZh) })
                    OverlayDialog(show = showStep2Dialog, title = "调整二限电量阈值", summary = "输入电量 (0-100 %)", onDismissRequest = { showStep2Dialog = false }, content = { HomeScreenDialogContent(value = dialogInputValue, onValueChange = { dialogInputValue = it }, onConfirm = { val v = dialogInputValue.toIntOrNull(); if (v != null && v in 0..100) step2Level = v.toString(); showStep2Dialog = false }, onCancel = { showStep2Dialog = false }, isZh = isZh) })
                    OverlayDialog(show = showStep3Dialog, title = "调整三限电量阈值", summary = "输入电量 (0-100 %)", onDismissRequest = { showStep3Dialog = false }, content = { HomeScreenDialogContent(value = dialogInputValue, onValueChange = { dialogInputValue = it }, onConfirm = { val v = dialogInputValue.toIntOrNull(); if (v != null && v in 0..100) step3Level = v.toString(); showStep3Dialog = false }, onCancel = { showStep3Dialog = false }, isZh = isZh) })
                    OverlayDialog(show = showStopLevelDialog, title = "调整电量检测阈值", summary = "输入电量检测阈值范围 0-100", onDismissRequest = { showStopLevelDialog = false }, content = { HomeScreenDialogContent(value = dialogInputValue, onValueChange = { dialogInputValue = it }, onConfirm = { val v = dialogInputValue.toIntOrNull(); if (v != null && v in 0..100) stopChargeLevel = v.toString(); showStopLevelDialog = false }, onCancel = { showStopLevelDialog = false }, isZh = isZh) })
                    OverlayDialog(show = showResumeLevelDialog, title = "调整恢复充电电量", summary = "输入恢复充电电量范围 0-100", onDismissRequest = { showResumeLevelDialog = false }, content = { HomeScreenDialogContent(value = dialogInputValue, onValueChange = { dialogInputValue = it }, onConfirm = { val v = dialogInputValue.toIntOrNull(); if (v != null && v in 0..100) resumeChargeLevel = v.toString(); showResumeLevelDialog = false }, onCancel = { showResumeLevelDialog = false }, isZh = isZh) })
                    BasicComponent(title = "亮息屏调速", enabled = switch10 || (!switch4 && !switch11 && !switch13), endActions = { ThemedSwitch(checked = switch10, onCheckedChange = null, enabled = switch10 || (!switch4 && !switch11 && !switch13), useMonet = useMonet) }, onClick = { if (switch10 || (!switch4 && !switch11 && !switch13)) { switch10 = !switch10; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) } })
                    AnimatedVisibility(visible = switch10, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) { Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { if (useMonet) OutlinedTextField(value = screenOnValue, onValueChange = { screenOnValue = it }, modifier = Modifier.fillMaxWidth(), label = { Text("亮屏限制电流") }, singleLine = true, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MiuixTheme.colorScheme.primary, focusedLabelColor = MiuixTheme.colorScheme.primary, cursorColor = MiuixTheme.colorScheme.primary, focusedTextColor = MiuixTheme.colorScheme.onSurface)) else TextField(value = screenOnValue, onValueChange = { screenOnValue = it }, modifier = Modifier.fillMaxWidth(), label = "亮屏限制电流", singleLine = true)
                            if (useMonet) OutlinedTextField(value = screenOffValue, onValueChange = { screenOffValue = it }, modifier = Modifier.fillMaxWidth(), label = { Text("锁屏限制电流") }, singleLine = true, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MiuixTheme.colorScheme.primary, focusedLabelColor = MiuixTheme.colorScheme.primary, cursorColor = MiuixTheme.colorScheme.primary, focusedTextColor = MiuixTheme.colorScheme.onSurface)) else TextField(value = screenOffValue, onValueChange = { screenOffValue = it }, modifier = Modifier.fillMaxWidth(), label = "锁屏限制电流", singleLine = true); Spacer(modifier = Modifier.height(8.dp)) } }
                    BasicComponent(title = "当电流低于阈值执行停充", endActions = { ThemedSwitch(checked = switch12, onCheckedChange = null, useMonet = useMonet) }, onClick = { switch12 = !switch12; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                    AnimatedVisibility(visible = switch12, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) { Column { BasicComponent(title = "电量检测阈值", endActions = { Text(text = "$stopChargeLevel %", fontSize = 13.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary); Spacer(modifier = Modifier.width(10.dp)); Image(modifier = Modifier.size(width = 10.dp, height = 16.dp).align(Alignment.CenterVertically), imageVector = MiuixIcons.Basic.ArrowRight, contentDescription = null, colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions)) }, onClick = { dialogInputValue = stopChargeLevel; showStopLevelDialog = true; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                            Slider(value = stopChargeLevel.toFloatOrNull() ?: 80f, onValueChange = { stopChargeLevel = it.toInt().toString() }, valueRange = 0f..100f, steps = 100, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp))
                            BasicComponent(title = "恢复充电电量", endActions = { Text(text = "$resumeChargeLevel %", fontSize = 13.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary); Spacer(modifier = Modifier.width(10.dp)); Image(modifier = Modifier.size(width = 10.dp, height = 16.dp).align(Alignment.CenterVertically), imageVector = MiuixIcons.Basic.ArrowRight, contentDescription = null, colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions)) }, onClick = { dialogInputValue = resumeChargeLevel; showResumeLevelDialog = true; hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                            Slider(value = resumeChargeLevel.toFloatOrNull() ?: 75f, onValueChange = { resumeChargeLevel = it.toInt().toString() }, valueRange = 0f..100f, steps = 100, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp))
                            if (useMonet) OutlinedTextField(value = stopChargeCurrent, onValueChange = { stopChargeCurrent = it }, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp), label = { Text("停充电流阈值") }, singleLine = true, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MiuixTheme.colorScheme.primary, focusedLabelColor = MiuixTheme.colorScheme.primary, cursorColor = MiuixTheme.colorScheme.primary, focusedTextColor = MiuixTheme.colorScheme.onSurface)) else TextField(value = stopChargeCurrent, onValueChange = { stopChargeCurrent = it }, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp), label = "停充电流阈值", singleLine = true) } }
                } } } } }

@Composable
private fun HomeScreenDialogContent(value: String, onValueChange: (String) -> Unit, onConfirm: () -> Unit, onCancel: () -> Unit, isZh: Boolean) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextField(value = value, onValueChange = { if (it.all { char -> char.isDigit() } || it.isEmpty()) onValueChange(it) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onCancel, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors()) { Text(if (isZh) "取消" else "Cancel") }
            Button(onClick = onConfirm, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColorsPrimary()) { Text(if (isZh) "确认" else "Confirm") }
        }
    }
}

@Composable
fun FavoritesScreen(reloadTrigger: Int = 0, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    var selectedIndex by rememberSaveable { mutableIntStateOf(prefs.getInt("logViewStyle", 0)) }
    var logContent by remember { mutableStateOf("正在加载日志...") }
    var logPoints by remember { mutableStateOf<List<ModuleDetector.LogDataPoint>>(emptyList()) }
    var showWatt by rememberSaveable { mutableStateOf(true) }
    var showLevel by rememberSaveable { mutableStateOf(true) }
    var showTemp by rememberSaveable { mutableStateOf(true) }
    var isCharging by remember { mutableStateOf(false) }
    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"
    val isCenterText = logContent == "日志文件为空" || logContent == "无法读取日志文件" || logContent == "正在加载日志..."

    LaunchedEffect(reloadTrigger, selectedIndex) {
        if (selectedIndex == 0) {
            while (true) {
                logContent = ModuleDetector.readLog()
                val batteryInfo = ModuleDetector.readBatteryInfo()
                isCharging = batteryInfo.status == "Charging"
                if (isCharging) kotlinx.coroutines.delay(15000) else break
            }
        } else {
            while (true) {
                logPoints = ModuleDetector.getParsedLogData()
                isCharging = ModuleDetector.readBatteryInfo().status == "Charging"
                if (isCharging) kotlinx.coroutines.delay(15000) else break
            }
        }
    }

    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
        item { BatteryInfoCard() }
        item { SectionTitle { SmallTitle(text = "日志", modifier = Modifier.offset(y = (8).dp)) } }
        item { MiuixCard(modifier = Modifier.padding(top = 11.dp)) { WindowDropdownPreference(items = listOf(if (isZh) "文字样式" else "Text", if (isZh) "曲线样式" else "Curve"), selectedIndex = selectedIndex, title = if (isZh) "显示样式" else "View Mode", onSelectedIndexChange = { selectedIndex = it; prefs.edit().putInt("logViewStyle", it).apply() }) } }
        item { MiuixCard(modifier = Modifier.padding(top = 16.dp)) {
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
                            Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                FilterLegend(if (isZh) "功耗" else "Watt", Color(0xFFE57373), showWatt) { showWatt = !showWatt }
                                FilterLegend(if (isZh) "电量" else "Bat", Color(0xFF81C784), showLevel) { showLevel = !showLevel }
                                FilterLegend(if (isZh) "温度" else "Temp", Color(0xFF64B5F6), showTemp) { showTemp = !showTemp }
                            }
                            LogLineChart(points = logPoints, isZh = isZh, showWatt = showWatt, showLevel = showLevel, showTemp = showTemp, isCharging = isCharging)
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

@Composable
fun LogLineChart(points: List<ModuleDetector.LogDataPoint>, isZh: Boolean, showWatt: Boolean, showLevel: Boolean, showTemp: Boolean, isCharging: Boolean = false) {
    val wattColor = Color(0xFFE57373)
    val levelColor = Color(0xFF81C784)
    val tempColor = Color(0xFF64B5F6)
    val primaryColor = MiuixTheme.colorScheme.primary
    val gridLineColor = MiuixTheme.colorScheme.onSurfaceSecondary.copy(alpha = 0.15f)
    
    val maxWattPoint = points.maxOfOrNull { it.watt } ?: 0f
    val maxWatt = maxOf(60f, kotlin.math.ceil(maxWattPoint / 20f).toInt() * 20f)
    val maxTemp = 100f
    var touchIndex by remember { mutableStateOf(-1) }
    val animatedTouchIndex = remember { Animatable(-1f) }
    val animatedAlpha = remember { Animatable(0f) }
    LaunchedEffect(touchIndex) {
        if (touchIndex == -1) {
            animatedAlpha.animateTo(0f, animationSpec = tween(150))
            animatedTouchIndex.snapTo(-1f)
        } else {
            val wasHidden = animatedTouchIndex.value < 0f
            if (wasHidden) {
                animatedTouchIndex.snapTo(0f)
                animatedAlpha.animateTo(1f, animationSpec = tween(200))
                animatedTouchIndex.animateTo(
                    targetValue = touchIndex.toFloat(),
                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                )
            } else {
                animatedTouchIndex.snapTo(touchIndex.toFloat())
                animatedAlpha.snapTo(1f)
            }
        }
    }
    fun parseTimeSeconds(time: String): Long = try {
        val p = time.substringAfter(" ").split(":")
        p[0].toLong() * 3600 + p[1].toLong() * 60 + p[2].toLong()
    } catch (_: Exception) {
        0L
    }
    val baseTimeSeconds = remember(points) { if (points.isEmpty()) 0L else parseTimeSeconds(points.first().time) }
    val endTimeSeconds = remember(points) { if (points.isEmpty()) 0L else parseTimeSeconds(points.last().time) }
    val totalDurationMinutes = ((endTimeSeconds - baseTimeSeconds) / 60).coerceAtLeast(0L)
    val averageWatt = remember(points) { points.map { it.watt }.average().toFloat() }
    val averageTemp = remember(points) { points.map { it.temp }.average().toFloat() }
    val startLevel = remember(points) { points.firstOrNull()?.level?.toInt() ?: 0 }
    val endLevel = remember(points) { points.lastOrNull()?.level?.toInt() ?: 0 }
    val minLevel = remember(points) { points.minOfOrNull { it.level.toInt() } ?: 0 }
    val maxLevel = remember(points) { points.maxOfOrNull { it.level.toInt() } ?: 0 }
    val isTouching = touchIndex in points.indices
    val markerData = remember(points, baseTimeSeconds, isCharging) {
        val data = mutableListOf<Pair<Int, String>>()
        if (points.isEmpty()) return@remember data
        val charge95Index = points.indexOfFirst { it.level >= 95f }
        if (charge95Index == -1) {
            // 未到达95%：每5分钟一个marker
            var lastM = -1L
            points.forEachIndexed { index, point ->
                try {
                    val s = parseTimeSeconds(point.time)
                    val m = (s - baseTimeSeconds) / 60
                    if (lastM == -1L || m - lastM >= 5) {
                        data.add(index to "${m}m")
                        lastM = m
                    }
                } catch (_: Exception) { }
            }
        } else {
            // 95%之前的点：每5分钟一个marker
            var lastM = -1L
            points.forEachIndexed { index, point ->
                if (index < charge95Index) {
                    try {
                        val s = parseTimeSeconds(point.time)
                        val m = (s - baseTimeSeconds) / 60
                        if (lastM == -1L || m - lastM >= 5) {
                            data.add(index to "${m}m")
                            lastM = m
                        }
                    } catch (_: Exception) { }
                }
            }
            // 95%之后：只在100%或充电结束时标记
            val lastIdx = points.lastIndex
            if (!isCharging) {
                // 已拔掉数据线：标记最后一个点
                try {
                    val s = parseTimeSeconds(points[lastIdx].time)
                    val m = (s - baseTimeSeconds) / 60
                    data.add(lastIdx to "${m}m")
                } catch (_: Exception) { }
            } else if (points[lastIdx].level >= 100f) {
                // 充电到100%：标记100%点
                val fullIdx = points.indexOfLast { it.level >= 100f }
                if (fullIdx >= charge95Index) {
                    try {
                        val s = parseTimeSeconds(points[fullIdx].time)
                        val m = (s - baseTimeSeconds) / 60
                        data.add(fullIdx to "${m}m")
                    } catch (_: Exception) { }
                }
            }
            // 否则（充电中且未到100%）：95%之后不添加marker
        }
        data
    }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        run {
            val lastPoint = points.lastOrNull()
            val animatedIndexValue = animatedTouchIndex.value
            val isAnimatingTouch = animatedIndexValue >= 0f
            val activeIndex = if (isAnimatingTouch) animatedIndexValue.toInt().coerceIn(points.indices) else points.lastIndex
            val durationValue = if (isAnimatingTouch) {
                val point = points[activeIndex]
                "${((parseTimeSeconds(point.time) - baseTimeSeconds) / 60).coerceAtLeast(0L)}m"
            } else {
                "${totalDurationMinutes}m"
            }
            val wattValue = if (isAnimatingTouch) {
                "%.1f W".format(points[activeIndex].watt)
            } else {
                "%.1f W".format(averageWatt)
            }
            val levelValue = if (isAnimatingTouch) {
                "${minLevel}% → ${maxLevel}%"
            } else {
                "${minLevel}% → ${maxLevel}%"
            }
            val tempValue = if (isAnimatingTouch) {
                "${points[activeIndex].temp.toInt()}°C"
            } else {
                "${averageTemp.toInt()}°C"
            }
            val shouldCollapse = isTouching || isCharging
            val summaryTransition = updateTransition(targetState = shouldCollapse, label = "log_summary_transition")
            val durationWeight by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 420, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)) },
                label = "duration_weight"
            ) { touching -> if (touching) 4f else 1f }
            val compactOtherWeight by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 420, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)) },
                label = "compact_other_weight"
            ) { touching -> if (touching) 0.001f else 0.9f }
            val levelWeight by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 420, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)) },
                label = "level_weight"
            ) { touching -> if (touching) 0.001f else 1.2f }
            val otherAlpha by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 320, easing = FastOutSlowInEasing) },
                label = "other_alpha"
            ) { touching -> if (touching) 0f else 1f }
            val otherScale by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 420, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)) },
                label = "other_scale"
            ) { touching -> if (touching) 0.82f else 1f }
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp), colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(durationWeight), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(durationValue, fontSize = 14.sp, color = primaryColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
                            Text(if (isZh) "时长" else "Dur", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary, textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }
                    Box(modifier = Modifier.weight(compactOtherWeight).graphicsLayer { alpha = otherAlpha; scaleX = otherScale; scaleY = otherScale }, contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(wattValue, fontSize = 14.sp, color = wattColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
                            Text(if (isZh) "平均功耗" else "Avg Watt", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary, textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }
                    Box(modifier = Modifier.weight(levelWeight).graphicsLayer { alpha = otherAlpha; scaleX = otherScale; scaleY = otherScale }, contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(levelValue, fontSize = 14.sp, color = levelColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
                            Text(if (isZh) "电量" else "Bat", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary, textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }
                    Box(modifier = Modifier.weight(compactOtherWeight).graphicsLayer { alpha = otherAlpha; scaleX = otherScale; scaleY = otherScale }, contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(tempValue, fontSize = 14.sp, color = tempColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
                            Text(if (isZh) "平均温度" else "Avg Temp", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary, textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 28.dp).pointerInput(points) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    val sp = size.width / (points.size.coerceAtLeast(2) - 1)
                    touchIndex = (offset.x / sp).toInt().coerceIn(points.indices)
                },
                onDrag = { change, _ ->
                    val sp = size.width / (points.size.coerceAtLeast(2) - 1)
                    touchIndex = (change.position.x / sp).toInt().coerceIn(points.indices)
                    change.consume()
                },
                onDragEnd = { touchIndex = -1 },
                onDragCancel = { touchIndex = -1 }
            )
        }) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width; val h = size.height; val sp = w / (points.size.coerceAtLeast(2) - 1)
                val zoneH = h * 0.25f
                val gap = h * 0.125f
                val levelBase = 0f
                val tempBase = zoneH + gap
                val wattBase = 2f * (zoneH + gap)
                for (i in 0..4) { val y = h - (i.toFloat() / 4 * h); drawLine(gridLineColor, Offset(0f, y), Offset(w, y), strokeWidth = 0.5.dp.toPx()) }
                if (showWatt) drawLogPathZone(points.map { (it.watt / maxWatt).coerceIn(0f, 1f) }, sp, zoneH, wattBase, wattColor)
                if (showLevel) drawLogPathZone(points.map { it.level / 100f }, sp, zoneH, levelBase, levelColor)
                if (showTemp) drawLogPathZone(points.map { (it.temp / maxTemp).coerceIn(0f, 1f) }, sp, zoneH, tempBase, tempColor)
                val animatedIndex = animatedTouchIndex.value
                val lastRealTimeX = if (points.isNotEmpty()) points.lastIndex * sp else -1f
                markerData.forEach { (index, _) ->
                    val x = index * sp
                    // 充电时若marker与实时点距离过近则隐藏该marker（实时点始终显示）
                    if (isCharging && lastRealTimeX >= 0f && kotlin.math.abs(x - lastRealTimeX) < 30.dp.toPx()) return@forEach
                    val p = points[index]
                    drawLine(gridLineColor, Offset(x, 0f), Offset(x, h), strokeWidth = 0.8.dp.toPx())
                    val textPaint = Paint().apply {
                        textSize = 32f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        textAlign = Paint.Align.CENTER
                    }
                    if (showWatt) {
                        val y = wattBase + zoneH - (p.watt / maxWatt) * zoneH
                        drawCircle(wattColor, radius = 4.5.dp.toPx(), center = Offset(x, y))
                        if (animatedIndex < 0f) {
                            textPaint.color = wattColor.toArgb()
                            drawContext.canvas.nativeCanvas.drawText("%.1fW".format(p.watt), x, (y - 12.dp.toPx()).coerceAtLeast(wattBase + 16.dp.toPx()), textPaint)
                        }
                    }
                    if (showLevel) {
                        val y = levelBase + zoneH - (p.level / 100f) * zoneH
                        drawCircle(levelColor, radius = 4.5.dp.toPx(), center = Offset(x, y))
                        if (animatedIndex < 0f) {
                            textPaint.color = levelColor.toArgb()
                            drawContext.canvas.nativeCanvas.drawText("${p.level.toInt()}%", x, (y + 22.dp.toPx()).coerceAtMost(levelBase + zoneH - 8.dp.toPx()), textPaint)
                        }
                    }
                    if (showTemp) {
                        val y = tempBase + zoneH - (p.temp / maxTemp) * zoneH
                        drawCircle(tempColor, radius = 4.5.dp.toPx(), center = Offset(x, y))
                        if (animatedIndex < 0f) {
                            textPaint.color = tempColor.toArgb()
                            drawContext.canvas.nativeCanvas.drawText("${p.temp.toInt()}°", x, (y + 22.dp.toPx()).coerceAtMost(tempBase + zoneH - 8.dp.toPx()), textPaint)
                        }
                    }
                }
                // 充电时始终显示最右边数据点（圆点始终显示，文字与marker重叠时隐藏）
                val isChargeTouching = animatedIndex >= 0f
                if (isCharging && !isChargeTouching && points.isNotEmpty()) {
                    val lastIdx = points.lastIndex
                    val isLastPointMarker = markerData.any { it.first == lastIdx }
                    val x = lastIdx * sp
                    val p = points[lastIdx]
                    // 圆点始终绘制
                    if (showWatt) {
                        val y = wattBase + zoneH - (p.watt / maxWatt).coerceIn(0f, 1f) * zoneH
                        drawCircle(wattColor, radius = 4.5.dp.toPx(), center = Offset(x, y))
                    }
                    if (showLevel) {
                        val y = levelBase + zoneH - (p.level / 100f) * zoneH
                        drawCircle(levelColor, radius = 4.5.dp.toPx(), center = Offset(x, y))
                    }
                    if (showTemp) {
                        val y = tempBase + zoneH - (p.temp / maxTemp).coerceIn(0f, 1f) * zoneH
                        drawCircle(tempColor, radius = 4.5.dp.toPx(), center = Offset(x, y))
                    }
                    // 文字标注仅在非 marker 点时显示（避免同一个点绘制两次）
                    if (!isLastPointMarker) {
                        val pointPaint = Paint().apply {
                            textSize = 32f
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            textAlign = Paint.Align.CENTER
                        }
                        if (showWatt) {
                            val y = wattBase + zoneH - (p.watt / maxWatt).coerceIn(0f, 1f) * zoneH
                            pointPaint.color = wattColor.toArgb()
                            drawContext.canvas.nativeCanvas.drawText("%.1fW".format(p.watt), x, (y - 12.dp.toPx()).coerceAtLeast(wattBase + 16.dp.toPx()), pointPaint)
                        }
                        if (showLevel) {
                            val y = levelBase + zoneH - (p.level / 100f) * zoneH
                            pointPaint.color = levelColor.toArgb()
                            drawContext.canvas.nativeCanvas.drawText("${p.level.toInt()}%", x, (y + 22.dp.toPx()).coerceAtMost(levelBase + zoneH - 8.dp.toPx()), pointPaint)
                        }
                        if (showTemp) {
                            val y = tempBase + zoneH - (p.temp / maxTemp).coerceIn(0f, 1f) * zoneH
                            pointPaint.color = tempColor.toArgb()
                            drawContext.canvas.nativeCanvas.drawText("${p.temp.toInt()}°", x, (y + 22.dp.toPx()).coerceAtMost(tempBase + zoneH - 8.dp.toPx()), pointPaint)
                        }
                    }
                }
                val alpha = animatedAlpha.value
                if (animatedIndex >= 0f && animatedIndex <= points.lastIndex) {
                    val x = animatedIndex * sp
                    val p = points[animatedIndex.toInt().coerceIn(points.indices)]
                    drawLine(primaryColor.copy(alpha = alpha), Offset(x, 0f), Offset(x, h), strokeWidth = 1.5.dp.toPx())
                    val textPaint = Paint().apply {
                        textSize = 34f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        textAlign = Paint.Align.LEFT
                    }
                    val textOffsetX = 12.dp.toPx()
                    val rightLimit = w - 8.dp.toPx()
                    fun textX(value: String): Float {
                        val desiredX = x + textOffsetX
                        val textWidth = textPaint.measureText(value)
                        return if (desiredX + textWidth <= rightLimit) desiredX else (x - textOffsetX - textWidth).coerceAtLeast(8.dp.toPx())
                    }
                    if (showWatt) {
                        val value = "%.1fW".format(p.watt)
                        val y = wattBase + zoneH - (p.watt / maxWatt).coerceIn(0f, 1f) * zoneH
                        drawCircle(wattColor.copy(alpha = alpha), radius = 5.dp.toPx(), center = Offset(x, y))
                        textPaint.color = wattColor.copy(alpha = alpha).toArgb()
                        drawContext.canvas.nativeCanvas.drawText(value, textX(value), (y - 14.dp.toPx()).coerceAtLeast(wattBase + 16.dp.toPx()), textPaint)
                    }
                    if (showLevel) {
                        val value = "${p.level.toInt()}%"
                        val y = levelBase + zoneH - (p.level / 100f) * zoneH
                        drawCircle(levelColor.copy(alpha = alpha), radius = 5.dp.toPx(), center = Offset(x, y))
                        textPaint.color = levelColor.copy(alpha = alpha).toArgb()
                        drawContext.canvas.nativeCanvas.drawText(value, textX(value), (y + 22.dp.toPx()).coerceAtMost(levelBase + zoneH - 8.dp.toPx()), textPaint)
                    }
                    if (showTemp) {
                        val value = "${p.temp.toInt()}°"
                        val y = tempBase + zoneH - (p.temp / maxTemp).coerceIn(0f, 1f) * zoneH
                        drawCircle(tempColor.copy(alpha = alpha), radius = 5.dp.toPx(), center = Offset(x, y))
                        textPaint.color = tempColor.copy(alpha = alpha).toArgb()
                        drawContext.canvas.nativeCanvas.drawText(value, textX(value), (y - 14.dp.toPx()).coerceAtLeast(tempBase + 16.dp.toPx()), textPaint)
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0m", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary)
            if (points.isNotEmpty()) {
                Text("${totalDurationMinutes}m", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary)
            }
        }
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLogPath(values: List<Float>, sp: Float, h: Float, color: Color) {
    val path = Path(); values.forEachIndexed { i, v -> val x = i * sp; val y = h - (v * h); if (i == 0) path.moveTo(x, y) else { val px = (i - 1) * sp; val py = h - (values[i - 1] * h); path.cubicTo((px + x) / 2, py, (px + x) / 2, y, x, y) } }
    drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLogPathZone(values: List<Float>, sp: Float, zoneH: Float, baseY: Float, color: Color) {
    val path = Path(); values.forEachIndexed { i, v -> val x = i * sp; val y = baseY + zoneH - (v * zoneH); if (i == 0) path.moveTo(x, y) else { val px = (i - 1) * sp; val py = baseY + zoneH - (values[i - 1] * zoneH); path.cubicTo((px + x) / 2, py, (px + x) / 2, y, x, y) } }
    drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
}

@Composable
fun ProfileScreen(useMonet: Boolean, onUseMonetChange: (Boolean) -> Unit, onConfigImported: () -> Unit = {}, onNavigateToDonate: () -> Unit = {}, reduceEffects: Boolean = false, modifier: Modifier = Modifier) {
    val context = LocalContext.current; val packageInfo = remember { context.packageManager.getPackageInfo(context.packageName, 0) }
    val appName = remember { context.applicationInfo.loadLabel(context.packageManager).toString() }; val versionName = packageInfo.versionName ?: "1.0"; val versionCode = packageInfo.longVersionCode
    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"; val scope = rememberCoroutineScope(); val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    val exportLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("text/plain"), onResult = { uri -> uri?.let { scope.launch { val content = ModuleDetector.readConfigRaw(); if (content.isNotEmpty()) { context.contentResolver.openOutputStream(it)?.use { s -> s.write(content.toByteArray()) }; Toast.makeText(context, if (isZh) "导出成功" else "Export Success", Toast.LENGTH_SHORT).show() } else { Toast.makeText(context, if (isZh) "导出失败：内容为空" else "Export Failed: Empty content", Toast.LENGTH_SHORT).show() } } } })
    val importLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument(), onResult = { uri -> uri?.let { scope.launch { try { val tempFile = java.io.File(context.cacheDir, "temp_config_import.prop"); context.contentResolver.openInputStream(it)?.use { input -> tempFile.outputStream().use { output -> input.copyTo(output) } }; val success = ModuleDetector.importConfigFile(tempFile.absolutePath); tempFile.delete(); if (success) { onConfigImported(); Toast.makeText(context, if (isZh) "导入成功，配置已重载" else "Import Success, Config reloaded", Toast.LENGTH_SHORT).show() } else { Toast.makeText(context, if (isZh) "导入失败" else "Import Failed", Toast.LENGTH_SHORT).show() } } catch (e: Exception) { Toast.makeText(context, "错误: ${e.message}", Toast.LENGTH_SHORT).show() } } } })
    val listState = rememberLazyListState(); val density = LocalDensity.current; var logoHeightDp by remember { mutableStateOf(300.dp) }; val fadeDistancePx = remember(density) { with(density) { 360.dp.toPx() } }
    val progress by remember { derivedStateOf { val idx = listState.firstVisibleItemIndex; val offset = listState.firstVisibleItemScrollOffset.toFloat(); val scrollPx = if (idx <= 0) offset else fadeDistancePx; (scrollPx / fadeDistancePx).coerceIn(0f, 1f) } }
    val spacerHeightPx = remember(density) { with(density) { 170.dp.toPx() } }; val aboutProgress by remember { derivedStateOf { val idx = listState.firstVisibleItemIndex; val offset = listState.firstVisibleItemScrollOffset.toFloat(); if (idx <= 0) (offset / spacerHeightPx).coerceIn(0f, 1f) else 1f } }
    val shaderSupported = remember { isRuntimeShaderSupported() }; val backdrop = rememberBlurBackdrop(); val dynamicBackground = shaderSupported && !reduceEffects; val isInDark = isSystemInDarkTheme(); var noiseCoefficient by remember { mutableFloatStateOf(BlurDefaults.NoiseCoefficient) }
    val logoBlend = remember(isInDark) { if (isInDark) listOf(BlendColorEntry(Color(0xe6a1a1a1), BlurBlendMode.ColorDodge), BlendColorEntry(Color(0x4de6e6e6), BlurBlendMode.LinearLight), BlendColorEntry(Color(0xff1af500), BlurBlendMode.Lab)) else listOf(BlendColorEntry(Color(0xcc4a4a4a), BlurBlendMode.ColorBurn), BlendColorEntry(Color(0xff4f4f4f), BlurBlendMode.LinearLight), BlendColorEntry(Color(0xff1af200), BlurBlendMode.Lab)) }
    BgEffectBackground(dynamicBackground = dynamicBackground, isOs3Effect = true, isFullSize = true, modifier = Modifier.fillMaxSize(), bgModifier = if (backdrop != null && !reduceEffects) Modifier.layerBackdrop(backdrop) else Modifier, alpha = { 1f - progress }) {
        Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 12.dp).align(Alignment.TopCenter).graphicsLayer { alpha = aboutProgress }, contentAlignment = Alignment.Center) { Text(text = "关于", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MiuixTheme.colorScheme.onBackground) }
        Column(modifier = Modifier.fillMaxWidth().padding(top = 170.dp).onSizeChanged { size -> with(density) { logoHeightDp = size.height.toDp() } }, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(88.dp).graphicsLayer { val iconProgress = ((progress - 0.35f) / 0.15f).coerceIn(0f, 1f); clip = true; shape = RoundedCornerShape(24.dp); alpha = 1 - iconProgress; scaleX = 1 - (iconProgress * 0.05f); scaleY = 1 - (iconProgress * 0.05f) }.background(Color.White).padding(15.dp)) { Icon(imageVector = MiuixIcons.Contacts, contentDescription = "App Icon", modifier = Modifier.fillMaxSize(), tint = Color(0xFF9C27B0)) }
            Text(modifier = Modifier.padding(top = 16.dp).fillMaxWidth().graphicsLayer { val projectNameProgress = ((progress - 0.20f) / 0.15f).coerceIn(0f, 1f); alpha = 1 - projectNameProgress; scaleX = 1 - (projectNameProgress * 0.05f); scaleY = 1 - (projectNameProgress * 0.05f) }.then(if (backdrop != null && !reduceEffects) { Modifier.textureBlur(backdrop = backdrop, shape = RoundedCornerShape(16.dp), blurRadius = 96f, noiseCoefficient = noiseCoefficient, colors = BlurDefaults.blurColors(blendColors = logoBlend), contentBlendMode = ComposeBlendMode.DstIn) } else Modifier), text = appName, color = MiuixTheme.colorScheme.onBackground, fontWeight = FontWeight.ExtraBold, fontSize = 42.sp, textAlign = TextAlign.Center)
            Text(modifier = Modifier.padding(top = 8.dp).fillMaxWidth().graphicsLayer { val versionCodeProgress = ((progress - 0.05f) / 0.15f).coerceIn(0f, 1f); alpha = 1 - versionCodeProgress; scaleX = 1 - (versionCodeProgress * 0.05f); scaleY = 1 - (versionCodeProgress * 0.05f) }, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, text = "$versionName ($versionCode) | release", fontSize = 15.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(80.dp))
        }
        LazyColumn(modifier = modifier.fillMaxSize(), state = listState, horizontalAlignment = Alignment.CenterHorizontally, contentPadding = PaddingValues(top = 170.dp, bottom = 16.dp)) {
            item(key = "logoSpacer") { Box(Modifier.fillMaxWidth().height(logoHeightDp + 80.dp), contentAlignment = Alignment.TopCenter, content = { }) }
            item { Spacer(modifier = Modifier.height(16.dp)); Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), cornerRadius = 24.dp, colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface.copy(alpha = 0.25f), contentColor = MiuixTheme.colorScheme.onSurface)) { WindowDropdownPreference(items = listOf("MiuiX", "Material"), selectedIndex = if (useMonet) 1 else 0, title = if (isZh) "界面风格" else "UI Style", onSelectedIndexChange = { onUseMonetChange(it == 1) }) } }
            item { Spacer(modifier = Modifier.height(16.dp)); Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), cornerRadius = 24.dp, colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface.copy(alpha = 0.25f), contentColor = MiuixTheme.colorScheme.onSurface)) { val uriHandler = LocalUriHandler.current; ArrowPreference(title = if (isZh) "SetoSkins" else "SetoSkins", startAction = { Box(modifier = Modifier.padding(end = 10.dp)) { Image(painter = painterResource(id = R.drawable.seto), contentDescription = null, modifier = Modifier.size(48.dp).clip(CircleShape)) } }, onClick = { uriHandler.openUri("https://github.com/SetoSkins") }) } }
            item { Spacer(modifier = Modifier.height(16.dp)); Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), cornerRadius = 24.dp, colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface.copy(alpha = 0.25f), contentColor = MiuixTheme.colorScheme.onSurface)) {
                    ArrowPreference(title = if (isZh) "导出软件配置" else "Export App Config", onClick = { exportLauncher.launch("SetoSkins_配置_备份.prop") })
                    ArrowPreference(title = if (isZh) "导入软件配置" else "Import App Config", onClick = { importLauncher.launch(arrayOf("*/*")) })
                    var showResetDialog by remember { mutableStateOf(false) }
                    ArrowPreference(title = if (isZh) "重置模块配置" else "Reset Module Config", onClick = { showResetDialog = true })
                    OverlayDialog(show = showResetDialog, title = if (isZh) "重置模块配置" else "Reset Module Config", summary = if (isZh) "确定要重置模块配置吗？所有开关将被关闭。" else "Are you sure you want to reset the module config? All switches will be turned off.", onDismissRequest = { showResetDialog = false }, content = { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { Button(onClick = { showResetDialog = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors()) { Text(if (isZh) "取消" else "Cancel") }; Button(onClick = { scope.launch { val success = ModuleDetector.resetConfig(); if (success) { prefs.edit().clear().apply(); kotlinx.coroutines.delay(200); onConfigImported(); Toast.makeText(context, if (isZh) "已重置并关闭所有开关" else "All configs reset and turned off", Toast.LENGTH_SHORT).show() } else { Toast.makeText(context, if (isZh) "重置失败" else "Reset Failed", Toast.LENGTH_SHORT).show() } }; showResetDialog = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColorsPrimary()) { Text(if (isZh) "确定" else "Confirm") } } })
                } }
            item { Spacer(modifier = Modifier.height(16.dp)); Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), cornerRadius = 24.dp, colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface.copy(alpha = 0.25f), contentColor = MiuixTheme.colorScheme.onSurface)) { ArrowPreference(title = if (isZh) "捐赠" else "Donate", onClick = onNavigateToDonate) } }
            item { Spacer(modifier = Modifier.height(16.dp)); Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), cornerRadius = 24.dp, colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface.copy(alpha = 0.25f), contentColor = MiuixTheme.colorScheme.onSurface)) { ArrowPreference(title = if (isZh) "检查更新" else "Check Update", onClick = { Toast.makeText(context, if (isZh) "功能正在开发" else "Feature is under development", Toast.LENGTH_SHORT).show() }) } }
        }
    }
}

@Composable
fun DonatePage(
    useMonet: Boolean,
    onDismiss: () -> Unit,
    progressProvider: () -> Float,
    modifier: Modifier = Modifier
) {
    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"
    val isDark = isSystemInDarkTheme()
    val colors = MiuixTheme.colorScheme
    val scrollState = rememberScrollState()
    val donateCardShape = RoundedCornerShape(24.dp)
    val donateCardColor = if (isDark) {
        colors.surfaceVariant.copy(alpha = if (useMonet) 0.82f else 0.78f)
    } else {
        colors.surface.copy(alpha = if (useMonet) 0.72f else 0.62f)
    }
    val donateCardBorderColor = if (isDark) {
        colors.primary.copy(alpha = if (useMonet) 0.30f else 0.22f)
    } else {
        colors.outline.copy(alpha = if (useMonet) 0.18f else 0.12f)
    }
    val donateCardShadowColor = if (isDark) {
        colors.primary.copy(alpha = if (useMonet) 0.18f else 0.10f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }
    val donateCardModifier = Modifier
        .fillMaxWidth()
        .shadow(
            elevation = if (isDark) 10.dp else 4.dp,
            shape = donateCardShape,
            clip = false,
            ambientColor = donateCardShadowColor,
            spotColor = donateCardShadowColor
        )
        .border(1.dp, donateCardBorderColor, donateCardShape)

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                val transitionProgress = progressProvider()
                // Donation page slides out to the right (0 to screenWidth)
                translationX = (1f - transitionProgress) * size.width
                
                // No scaling or rounding for a clean push transition
                scaleX = 1f
                scaleY = 1f
            }
            .background(MiuixTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(text = if (isZh) "捐赠" else "Donate", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(scrollState).padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(16.dp))
                Image(painter = painterResource(id = R.drawable.seto), contentDescription = null, modifier = Modifier.size(80.dp).clip(CircleShape).align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(12.dp))
                Text(text = "SetoSkins", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MiuixTheme.colorScheme.onBackground, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Text(text = if (isZh) "温度调控模块" else "Thermal Control Module", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                Card(modifier = donateCardModifier, cornerRadius = 24.dp, colors = CardDefaults.defaultColors(color = donateCardColor, contentColor = colors.onSurface)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = if (isZh) "支持项目" else "Support the Project", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(text = if (isZh) "如果你喜欢这个项目，欢迎通过以下方式支持我" else "If you like this project, feel free to support us:", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = donateCardModifier,
                    cornerRadius = 24.dp,
                    colors = CardDefaults.defaultColors(
                        color = donateCardColor,
                        contentColor = colors.onSurface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        DonateQrImage(
                            title = if (isZh) "微信" else "WeChat",
                            imageRes = R.drawable.weixin,
                            contentDescription = "WeChat QR Code",
                            height = 180.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        DonateQrImage(
                            title = if (isZh) "支付宝" else "Alipay",
                            imageRes = R.drawable.zfb,
                            contentDescription = "Alipay QR Code",
                            height = 260.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DonateQrImage(
    title: String,
    imageRes: Int,
    contentDescription: String,
    height: androidx.compose.ui.unit.Dp,
    imageOffsetY: androidx.compose.ui.unit.Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = imageOffsetY)
                .clip(RoundedCornerShape(18.dp))
        )
    }
}

@Composable
fun DeviceInfoItem(value: String, label: String) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        Text(text = label, fontSize = 13.sp, color = Color.Gray)
    }
}

@Composable
fun MiuixCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(modifier = modifier.fillMaxWidth(), cornerRadius = 16.dp) { content() }
}

@Composable
fun MiuixListItem(title: String, subtitle: String?, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, color = MiuixTheme.colorScheme.onSurface)
            if (subtitle != null) { Spacer(modifier = Modifier.height(2.dp)); Text(text = subtitle, fontSize = 13.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary) }
        }
    }
}

enum class AppDestinations(val label: String, val icon: ImageVector) {
    HOME("主页", Icons.Filled.Home),
    FAVORITES("日志", MiuixIcons.File),
    PROFILE("关于", MiuixIcons.Info),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemedSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, enabled: Boolean = true, useMonet: Boolean) {
    val colors = MiuixTheme.colorScheme
    if (useMonet) {
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled, thumbContent = { Icon(imageVector = if (checked) Icons.Rounded.Check else Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }, colors = SwitchDefaults.colors(checkedThumbColor = if (isSystemInDarkTheme()) colors.primaryContainer else Color.White, checkedTrackColor = colors.primary, checkedIconColor = colors.primary, uncheckedThumbColor = Color(0xFF7E8785), uncheckedTrackColor = Color.Transparent, uncheckedBorderColor = Color(0xFF7E8785), uncheckedIconColor = Color.White, disabledCheckedThumbColor = Color.White.copy(alpha = 0.5f), disabledCheckedTrackColor = colors.primary.copy(alpha = 0.3f), disabledUncheckedThumbColor = Color(0xFFBEBEBE), disabledUncheckedTrackColor = Color.Transparent, disabledUncheckedBorderColor = Color(0xFFD8D8D8), disabledUncheckedIconColor = Color.White.copy(alpha = 0.7f)), modifier = Modifier.scale(1.02f))
    } else {
        MiuixSwitch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
fun SectionTitle(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.offset(x = (-12).dp).layout { measurable, constraints -> val placeable = measurable.measure(constraints); layout(placeable.width, placeable.height - 8.dp.roundToPx()) { placeable.place(0, 0) } }) { content() }
}

@Composable
fun YellowUpdateCard() {
    val uriHandler = LocalUriHandler.current; val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) Color(0xFF3E2C00) else Color(0xFFFFF8E1)
    val titleColor = if (isDark) Color.White else Color(0xFF855A00)
    val subColor = if (isDark) Color(0xFFFFE082) else Color(0xFFB08900)
    val iconColor = if (isDark) Color(0xFFFFD54F) else Color(0xFFFFB300)
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cardBg).clickable { uriHandler.openUri("https://github.com/SetoSkins/SetoSkins_Thermal/releases") }.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) { Text(text = "检测到更新", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = titleColor); Spacer(modifier = Modifier.height(4.dp)); Text(text = "新版本已发布,点击去下载", fontSize = 14.sp, color = subColor) }
            Canvas(modifier = Modifier.size(32.dp)) {
                val sw = 2.dp.toPx(); drawCircle(color = iconColor, radius = (size.minDimension - sw) / 2, style = Stroke(width = sw), center = center)
                val arrowPath = Path().apply { moveTo(center.x, center.y - 6.dp.toPx()); lineTo(center.x, center.y + 6.dp.toPx()); moveTo(center.x - 4.dp.toPx(), center.y + 2.dp.toPx()); lineTo(center.x, center.y + 6.dp.toPx()); lineTo(center.x + 4.dp.toPx(), center.y + 2.dp.toPx()) }
                drawPath(path = arrowPath, color = iconColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}

@Composable
fun RedNotInstalledCard() {
    val uriHandler = LocalUriHandler.current; val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) Color(0xFF3D0C0C) else Color(0xFFFFE5E5)
    val titleColor = if (isDark) Color.White else Color(0xFF8A1A1A)
    val subColor = if (isDark) Color(0xFFE0E0E0) else Color(0xFFB33A3A)
    val iconColor = if (isDark) Color(0xFFFF4444) else Color(0xFFD32F2F)
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cardBg).clickable { uriHandler.openUri("https://github.com/SetoSkins/SetoSkins_Thermal/releases") }.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) { Text(text = "未激活", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = titleColor); Spacer(modifier = Modifier.height(4.dp)); Text(text = "模块未安装", fontSize = 14.sp, color = subColor) }
            Canvas(modifier = Modifier.size(32.dp)) { val sw = 2.dp.toPx(); drawCircle(color = iconColor, radius = (size.minDimension - sw) / 2, style = Stroke(width = sw), center = center); drawLine(color = iconColor, start = Offset(center.x, center.y - 6.dp.toPx()), end = Offset(center.x, center.y + 2.dp.toPx()), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round); drawCircle(color = iconColor, radius = 1.5.dp.toPx(), center = Offset(center.x, center.y + 7.dp.toPx())) }
        }
    }
}

@Composable
fun GreenActivatedCard(useMonet: Boolean, version: String) {
    val isDark = isSystemInDarkTheme(); val miuixColors = MiuixTheme.colorScheme
    val greenAccent = if (isDark) Color(0xFF34C759) else Color(0xFF1B7A3A)
    val greenContainerBg = if (isDark) Color(0xFF1A3A24) else Color(0xFFE0F5E5)
    val greenTitleColor = if (isDark) Color.White else Color(0xFF0F5128)
    val greenSubColor = if (isDark) Color(0xFFBFE9CC) else Color(0xFF2E7D3A)
    val containerBg = if (useMonet) miuixColors.secondaryContainer else greenContainerBg
    val onContainer = if (useMonet) miuixColors.onSecondaryContainer else greenTitleColor
    val onContainerSub = if (useMonet) miuixColors.onSurfaceSecondary else greenSubColor

    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(containerBg).padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) { Text(text = "已激活", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onContainer); Spacer(modifier = Modifier.height(4.dp)); Text(text = version.ifEmpty { "检测中..." }, fontSize = 14.sp, color = onContainerSub) }
            if (useMonet) { val cc = miuixColors.primary; Canvas(modifier = Modifier.size(32.dp)) { val sw = 2.dp.toPx(); drawArc(color = cc, startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = sw, cap = StrokeCap.Round), size = Size(size.width - sw, size.height - sw), topLeft = Offset(sw / 2f, sw / 2f)); val cp = Path().apply { moveTo(center.x - 5.dp.toPx(), center.y); lineTo(center.x - 1.dp.toPx(), center.y + 4.dp.toPx()); lineTo(center.x + 6.dp.toPx(), center.y - 4.dp.toPx()) }; drawPath(path = cp, color = cc, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)) } }
            else { Canvas(modifier = Modifier.size(32.dp)) { val sw = 2.dp.toPx(); drawArc(color = greenAccent, startAngle = 90f, sweepAngle = 270f, useCenter = false, style = Stroke(width = sw, cap = StrokeCap.Round), size = Size(size.width - sw, size.height - sw), topLeft = Offset(sw / 2f, sw / 2f)); val cp = Path().apply { moveTo(center.x - 5.dp.toPx(), center.y); lineTo(center.x - 1.dp.toPx(), center.y + 4.dp.toPx()); lineTo(center.x + 6.dp.toPx(), center.y - 4.dp.toPx()) }; drawPath(path = cp, color = greenAccent, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)) } }
        }
    }
}

@Composable
fun BatteryInfoCard() {
    var info by remember { mutableStateOf(ModuleDetector.BatteryInfo()) }
    LaunchedEffect(Unit) { while (true) { info = ModuleDetector.readBatteryInfo(); kotlinx.coroutines.delay(3000) } }
    val tempText = info.temperature.let { if (it.isNotEmpty()) try { "${"%.1f".format(it.toInt() / 10.0)}°C" } catch (_: Exception) { "${it}°C" } else "..." }
    val currentText = info.current.let { if (it.isNotEmpty()) try { "${it.toInt() / -1000} mA" } catch (_: Exception) { "${it} mA" } else "..." }
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
    MiuixCard { Column(modifier = Modifier.padding(start = 16.dp, top = 2.dp, end = 16.dp, bottom = 16.dp)) { Spacer(modifier = Modifier.height(12.dp)); Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) { BatteryStatItem("温度", tempText, Modifier.weight(1f)); BatteryStatItem("电流", currentText, Modifier.weight(1f)) }; Spacer(modifier = Modifier.height(12.dp)); Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) { BatteryStatItem("电量", cap, Modifier.weight(1f)); BatteryStatItem("充电状态", st, Modifier.weight(1f)) } } }
}

@Composable
fun BatteryStatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) { Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MiuixTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()); Spacer(modifier = Modifier.height(4.dp)); Text(text = label, fontSize = 13.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() { MyApplicationTheme(useMonet = false) { MyApplicationApp(useMonet = false, onUseMonetChange = {}) } }

fun Modifier.compactSmallTitle(): Modifier = this.padding(start = 4.dp).offset(x = (-13).dp, y = 11.dp).layout { measurable, constraints -> val placeable = measurable.measure(constraints); val reduce = 24.dp.roundToPx(); layout(placeable.width, (placeable.height - reduce).coerceAtLeast(1)) { placeable.place(0, -reduce) } }
