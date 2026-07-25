package com.setoskins.thermal.ui.navigation

import android.util.Log
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import com.setoskins.thermal.data.ModuleDetector
import com.setoskins.thermal.ui.component.rememberBlurBackdrop
import com.setoskins.thermal.ui.screen.HomeScreen
import com.setoskins.thermal.ui.screen.FavoritesScreen
import com.setoskins.thermal.ui.screen.ProfileScreen
import com.setoskins.thermal.ui.screen.DonatePage

enum class AppDestinations(val label: String, val icon: ImageVector) {
    HOME("主页", Icons.Filled.Home),
    FAVORITES("日志", MiuixIcons.File),
    PROFILE("关于", MiuixIcons.Info),
}

@Composable
fun MyApplicationApp(
    useMonet: Boolean,
    onUseMonetChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var reloadTrigger by remember { mutableIntStateOf(0) }
    var logReloadTrigger by remember { mutableIntStateOf(0) }
    var rootState by remember { mutableStateOf<Boolean?>(null) }
    var showRootDialog by remember { mutableStateOf(false) }
    var hasAcknowledgedMissingRoot by remember { mutableStateOf(false) }
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

            scope.launch {
                transitionProgress.animateTo(1f, tween(400, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
            }
        }
    }

    LaunchedEffect(Unit) {
        Log.d("SetoSkins", "Starting Root Check...")
        // 启动一个 2 秒的计时器，如果 2 秒后还没检测完且用户未点过确认，则弹窗提示
        launch {
            kotlinx.coroutines.delay(2000)
            if (rootState == null && !hasAcknowledgedMissingRoot) {
                showRootDialog = true
            }
        }
        
        val result = ModuleDetector.requestRoot()
        Log.d("SetoSkins", "Root Check Result: $result")
        rootState = result
        if (result == false && !hasAcknowledgedMissingRoot) {
            showRootDialog = true
        }
    }

    val backdrop = rememberBlurBackdrop()
    val shaderSupported = remember { isRuntimeShaderSupported() }
    val reduceExpensiveEffects = showDonatePage || showDonateLayer
    val showBlur = useMonet && shaderSupported && backdrop != null && !reduceExpensiveEffects
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val homeScrollBehavior = MiuixScrollBehavior()
    val favoritesScrollBehavior = MiuixScrollBehavior()
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (useMonet) MiuixTheme.colorScheme.background else if (isDark) Color.Black else MiuixTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = -screenWidth.toPx() * 0.18f * transitionProgress.value
                }
        ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        containerColor = Color.Transparent,
                        topBar = {
                            if (currentDestination != AppDestinations.PROFILE) {
                                val scrollBehavior = when (currentDestination) {
                                    AppDestinations.HOME -> homeScrollBehavior
                                    AppDestinations.FAVORITES -> favoritesScrollBehavior
                                    else -> null
                                }
                                val title = when (currentDestination) {
                                    AppDestinations.HOME -> "Seto温控"
                                    AppDestinations.FAVORITES -> "日志"
                                    else -> ""
                                }
                                TopAppBar(
                                    title = title,
                                    largeTitle = title,
                                    largeTitleColor = Color.Transparent,
                                    color = if (useMonet) MiuixTheme.colorScheme.background else if (isDark) Color.Black else MiuixTheme.colorScheme.surface,
                                    scrollBehavior = scrollBehavior,
                                    bottomContent = {
                                        val collapsedFraction by remember(scrollBehavior) { derivedStateOf { scrollBehavior?.state?.collapsedFraction ?: 0f } }
                                        val height = (24 * (1f - collapsedFraction.coerceIn(0f, 1f))).dp
                                        Spacer(modifier = Modifier.height(height))
                                    },
                                    actions = {
                                        if (currentDestination == AppDestinations.FAVORITES) {
                                            val haptic = LocalHapticFeedback.current
                                            IconButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    scope.launch { if (ModuleDetector.clearLog()) logReloadTrigger++ }
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
                                            scrollBehavior = homeScrollBehavior,
                                            contentPaddingTop = innerPadding.calculateTopPadding(),
                                            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()).overScrollVertical()
                                        )
                                    }
                                    AppDestinations.FAVORITES -> {
                                        FavoritesScreen(
                                            reloadTrigger = logReloadTrigger,
                                            scrollBehavior = favoritesScrollBehavior,
                                            contentPaddingTop = innerPadding.calculateTopPadding(),
                                            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()).overScrollVertical()
                                        )
                                    }
                                    AppDestinations.PROFILE -> {
                                        ProfileScreen(
                                            useMonet = useMonet,
                                            onUseMonetChange = onUseMonetChange,
                                            onConfigImported = { reloadTrigger++ },
                                            onNavigateToDonate = { showDonatePage = true },
                                            reduceEffects = reduceExpensiveEffects,
                                            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()).overScrollVertical()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (currentDestination != AppDestinations.PROFILE) {
                        val activeScrollBehavior = when (currentDestination) {
                            AppDestinations.HOME -> homeScrollBehavior
                            AppDestinations.FAVORITES -> favoritesScrollBehavior
                            else -> null
                        }
                        val activeTitle = when (currentDestination) {
                            AppDestinations.HOME -> "Seto温控"
                            AppDestinations.FAVORITES -> "日志"
                            else -> ""
                        }
                        if (activeScrollBehavior != null) {
                            val collapsedFraction by remember(activeScrollBehavior) { derivedStateOf { activeScrollBehavior.state.collapsedFraction } }
                            Text(
                                text = activeTitle,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier
                                    .statusBarsPadding()
                                    .padding(top = 64.dp, start = 26.dp)
                                    .graphicsLayer { alpha = 1f - collapsedFraction.coerceIn(0f, 1f) }
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

            if (showDonateLayer) {
                DonatePage(
                    useMonet = useMonet,
                    onDismiss = { showDonatePage = false },
                    progressProvider = { transitionProgress.value }
                )
            }

            OverlayDialog(
                show = showRootDialog,
                title = if (isZh) "权限缺失" else "Root Permission Required",
                summary = if (isZh) "检测到设备未获取 Root 权限，本软件无法正常工作，请授权后重新进入。" else "Root access was not detected. This app requires Root to function properly.",
                onDismissRequest = { 
                    showRootDialog = false 
                    hasAcknowledgedMissingRoot = true
                },
                content = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { 
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                showRootDialog = false 
                                hasAcknowledgedMissingRoot = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColorsPrimary()
                        ) {
                            Text(if (isZh) "确定" else "OK")
                        }
                    }
                }
            )
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