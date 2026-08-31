package com.setoskins.thermal.ui.navigation


import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationItem
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

import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.setoskins.thermal.data.ModuleDetector
import com.setoskins.thermal.data.UpdateManager
import com.setoskins.thermal.ui.component.RestartDialog
import com.setoskins.thermal.ui.component.RootCheckDialog
import com.setoskins.thermal.ui.component.UpdateDialog
import com.setoskins.thermal.ui.component.liquid.IosLiquidGlassNavigationBar
import com.setoskins.thermal.ui.component.MaterialFloatingNavigationBar
import com.setoskins.thermal.ui.component.rememberBlurBackdrop
import com.setoskins.thermal.ui.screen.HomeScreen
import com.setoskins.thermal.ui.screen.FavoritesScreen
import com.setoskins.thermal.ui.screen.ProfileScreen
import com.setoskins.thermal.ui.screen.DonatePage
import com.setoskins.thermal.ui.screen.BlacklistPage
import com.setoskins.thermal.ui.screen.BypassListPage
import android.util.Log
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.res.painterResource
import com.setoskins.thermal.R

enum class AppDestinations(val label: String, val icon: ImageVector, val filledIcon: ImageVector) {
    HOME("主页", Icons.Outlined.Home, Icons.Filled.Home),
    FAVORITES("日志", Icons.Outlined.Description, Icons.Filled.Description),
    PROFILE("关于", Icons.Outlined.Info, Icons.Filled.Info),
}

@Composable
fun MyApplicationApp(
    useMonet: Boolean,
    onUseMonetChange: (Boolean) -> Unit,
    floatingNavBar: Boolean,
    onFloatingNavBarChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { AppDestinations.entries.size }
    )
    val currentDestination by remember(pagerState) {
        derivedStateOf { AppDestinations.entries[pagerState.currentPage] }
    }
    var reloadTrigger by remember { mutableIntStateOf(0) }
    var logReloadTrigger by remember { mutableIntStateOf(0) }
    var showDonatePage by remember { mutableStateOf(false) }
    var showDonateLayer by remember { mutableStateOf(false) }
    val transitionProgress = remember { Animatable(0f) }
    var showBlacklistPage by remember { mutableStateOf(false) }
    var showBlacklistLayer by remember { mutableStateOf(false) }
    val blacklistTransitionProgress = remember { Animatable(0f) }
    var showWhitelistPage by remember { mutableStateOf(false) }
    var showWhitelistLayer by remember { mutableStateOf(false) }
    val whitelistTransitionProgress = remember { Animatable(0f) }
    var showBypassListPage by remember { mutableStateOf(false) }
    var showBypassListLayer by remember { mutableStateOf(false) }
    val bypassListTransitionProgress = remember { Animatable(0f) }
    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"

    // ── Root 检测 ──
    var showRootDialog by rememberSaveable { mutableStateOf(false) }

    // ── 重启对话框 ──
    var showRestartDialog by remember { mutableStateOf(false) }
    
    // ── 更新对话框 ──
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateManager.UpdateInfo?>(null) }
    
    var barVisible by remember { mutableStateOf(true) }
    var dialogCount by remember { mutableIntStateOf(0) }
    val onDialogVisibilityChange: (Boolean) -> Unit = remember {
        { visible -> if (visible) dialogCount++ else if (dialogCount > 0) dialogCount-- }
    }

    LaunchedEffect(Unit) {
        if (!ModuleDetector.requestRoot()) {
            kotlinx.coroutines.delay(1000L)
            showRootDialog = true
            barVisible = false
        }
        // App 启动时执行一次静默更新检查
        scope.launch {
            val info = UpdateManager.checkAppUpdate(context)
            if (info.hasUpdate) {
                updateInfo = info
                showUpdateDialog = true
                onDialogVisibilityChange(true)
            }
        }
    }

    LaunchedEffect(showBlacklistPage) {
        if (showBlacklistPage) {
            showBlacklistLayer = true
            blacklistTransitionProgress.animateTo(1f, tween(480, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
        } else {
            blacklistTransitionProgress.animateTo(0f, tween(320, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
            showBlacklistLayer = false
        }
    }

    PredictiveBackHandler(enabled = showBlacklistPage) { progress ->
        try {
            progress.collect { backEvent ->
                blacklistTransitionProgress.snapTo(1f - backEvent.progress)
            }
            showBlacklistPage = false
        } catch (e: Exception) {
            scope.launch {
                blacklistTransitionProgress.animateTo(1f, tween(400, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
            }
        }
    }

    LaunchedEffect(showWhitelistPage) {
        if (showWhitelistPage) {
            showWhitelistLayer = true
            whitelistTransitionProgress.animateTo(1f, tween(480, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
        } else {
            whitelistTransitionProgress.animateTo(0f, tween(320, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
            showWhitelistLayer = false
        }
    }

    PredictiveBackHandler(enabled = showWhitelistPage) { progress ->
        try {
            progress.collect { backEvent ->
                whitelistTransitionProgress.snapTo(1f - backEvent.progress)
            }
            showWhitelistPage = false
        } catch (e: Exception) {
            scope.launch {
                whitelistTransitionProgress.animateTo(1f, tween(400, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
            }
        }
    }

    LaunchedEffect(showBypassListPage) {
        if (showBypassListPage) {
            showBypassListLayer = true
            bypassListTransitionProgress.animateTo(1f, tween(480, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
        } else {
            bypassListTransitionProgress.animateTo(0f, tween(320, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
            showBypassListLayer = false
        }
    }

    PredictiveBackHandler(enabled = showBypassListPage) { progress ->
        try {
            progress.collect { backEvent ->
                bypassListTransitionProgress.snapTo(1f - backEvent.progress)
            }
            showBypassListPage = false
        } catch (e: Exception) {
            scope.launch {
                bypassListTransitionProgress.animateTo(1f, tween(400, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
            }
        }
    }

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

    val backdrop = rememberBlurBackdrop()
    val shaderSupported = remember { isRuntimeShaderSupported() }
    val reduceExpensiveEffects = showDonatePage || showDonateLayer || showBlacklistPage || showBlacklistLayer || showWhitelistPage || showWhitelistLayer
    val showBlur = useMonet && shaderSupported && backdrop != null && !reduceExpensiveEffects
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val homeScrollBehavior = MiuixScrollBehavior()
    val favoritesScrollBehavior = MiuixScrollBehavior()
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
                    translationX = -screenWidth.toPx() * 0.18f * maxOf(transitionProgress.value, blacklistTransitionProgress.value, whitelistTransitionProgress.value)
                }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        if (currentDestination != AppDestinations.PROFILE) {
                            TopAppBar(
                                title = activeTitle,
                                largeTitle = activeTitle,
                                largeTitleColor = Color.Transparent,
                                color = if (useMonet) MiuixTheme.colorScheme.background else if (isDark) Color.Black else MiuixTheme.colorScheme.surface,
                                scrollBehavior = activeScrollBehavior,
                                bottomContent = {
                                    val collapsedFraction by remember(activeScrollBehavior) { derivedStateOf { activeScrollBehavior?.state?.collapsedFraction ?: 0f } }
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
                                    if (currentDestination == AppDestinations.HOME) {
                                        IconButton(
                                            onClick = {
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                showRestartDialog = true
                                                barVisible = false
                                            }
                                        ) {
                                            Icon(
                                            painter = painterResource(R.drawable.restart),
                                            contentDescription = if (isZh) "重启" else "Restart",
                                            tint = MiuixTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        }
                                    }
                                }
                            )
                        }
                    },
                    bottomBar = if (floatingNavBar) ({}) else ({
                        ThemedNavigationBar(
                            currentDestination = currentDestination,
                            pagerState = pagerState,
                            onDestinationSelected = { dest ->
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        AppDestinations.entries.indexOf(dest),
                                        animationSpec = tween(420, easing = CubicBezierEasing(0.12f, 0.0f, 0.05f, 1.0f))
                                    )
                                }
                            },
                            useMonet = useMonet,
                            backdrop = backdrop,
                            showBlur = showBlur,
                            floatingNavBar = false
                        )
                    })
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        HorizontalPager(
                            state = pagerState,
                            beyondViewportPageCount = 1,
                            modifier = Modifier.fillMaxSize(),
                            flingBehavior = PagerDefaults.flingBehavior(
                                state = pagerState,
                                snapAnimationSpec = tween(390, easing = CubicBezierEasing(0.12f, 0.0f, 0.05f, 1.0f))
                            )
                        ) { page ->
                            when (AppDestinations.entries[page]) {
                                AppDestinations.HOME -> {
                                    HomeScreen(
                                        useMonet = useMonet,
                                        reloadTrigger = reloadTrigger,
                                        scrollBehavior = homeScrollBehavior,
                                        contentPaddingTop = innerPadding.calculateTopPadding(),
                                        floatingNavBar = floatingNavBar,
                                        onNavigateToBlacklist = { showBlacklistPage = true },
                                        onNavigateToWhitelist = { showWhitelistPage = true },
                                        onNavigateToBypassList = { showBypassListPage = true },
                                        onDialogVisibilityChange = onDialogVisibilityChange,
                                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                                    )
                                }
                                AppDestinations.FAVORITES -> {
                                    FavoritesScreen(
                                        useMonet = useMonet,
                                        reloadTrigger = logReloadTrigger,
                                        scrollBehavior = favoritesScrollBehavior,
                                        contentPaddingTop = innerPadding.calculateTopPadding(),
                                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                                    )
                                }
                                AppDestinations.PROFILE -> {
                                    ProfileScreen(
                                        useMonet = useMonet,
                                        onUseMonetChange = onUseMonetChange,
                                        floatingNavBar = floatingNavBar,
                                        onFloatingNavBarChange = onFloatingNavBarChange,
                                        onConfigImported = { reloadTrigger++ },
                                        onNavigateToDonate = { showDonatePage = true },
                                        reduceEffects = reduceExpensiveEffects,
                                        onDialogVisibilityChange = onDialogVisibilityChange,
                                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                                    )
                                }
                            }
                        }

                        // ── Root 检测对话框 (模仿工作正常的 ConfigDialog 模式) ──
                        if (showRootDialog) {
                            RootCheckDialog(
                                show = true,
                                isZh = isZh,
                                onDismiss = { showRootDialog = false },
                                onExit = { (context as? Activity)?.finish() },
                                onDismissStart = { barVisible = true }
                            )
                        }

                        // ── 重启对话框 ──
                        if (showRestartDialog) {
                            RestartDialog(
                                show = true,
                                isZh = isZh,
                                onConfirm = {
                                    showRestartDialog = false
                                    scope.launch { ModuleDetector.restartDevice() }
                                },
                                onDismiss = { showRestartDialog = false },
                                onDismissStart = { barVisible = true }
                            )
                        }

                        // ── 更新对话框 ──
                        if (showUpdateDialog) {
                            UpdateDialog(
                                show = true,
                                version = updateInfo?.latestVersion ?: "",
                                releaseNotes = updateInfo?.releaseNotes ?: "",
                                isZh = isZh,
                                onConfirm = {
                                    updateInfo?.downloadUrl?.let { url ->
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    }
                                    onDialogVisibilityChange(false)
                                    showUpdateDialog = false
                                },
                                onDismiss = {
                                    onDialogVisibilityChange(false)
                                    showUpdateDialog = false
                                }
                            )
                        }
                    }
                }
            }

            if (currentDestination != AppDestinations.PROFILE) {
                if (activeScrollBehavior != null) {
                    val collapsedFraction by remember(activeScrollBehavior) { derivedStateOf { activeScrollBehavior.state.collapsedFraction } }
                    Text(
                        text = activeTitle,
                        fontSize = 36.sp,
                        fontWeight = if (useMonet) FontWeight.ExtraBold else FontWeight.Normal,
                        color = MiuixTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(top = 64.dp, start = 26.dp)
                            .graphicsLayer { alpha = 1f - collapsedFraction.coerceIn(0f, 1f) }
                    )
                }
            }

            AnimatedVisibility(
                visible = floatingNavBar && barVisible && dialogCount == 0,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                ThemedNavigationBar(
                    currentDestination = currentDestination,
                    pagerState = pagerState,
                    onDestinationSelected = { dest ->
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        AppDestinations.entries.indexOf(dest),
                                        animationSpec = tween(420, easing = CubicBezierEasing(0.12f, 0.0f, 0.05f, 1.0f))
                                    )
                                }
                            },
                            useMonet = useMonet,
                            backdrop = backdrop,
                            showBlur = showBlur,
                            floatingNavBar = true
                )
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

        if (showBlacklistLayer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 0.22f * blacklistTransitionProgress.value
                    }
                    .background(Color.Black)
            )
        }

        if (showBlacklistLayer) {
            BlacklistPage(
                useMonet = useMonet,
                isZh = isZh,
                prefs = context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE),
                onBack = { showBlacklistPage = false },
                progressProvider = { blacklistTransitionProgress.value }
            )
        }

        if (showWhitelistLayer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 0.22f * whitelistTransitionProgress.value
                    }
                    .background(Color.Black)
            )
        }

        if (showWhitelistLayer) {
            BlacklistPage(
                useMonet = useMonet,
                isZh = isZh,
                prefs = context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE),
                onBack = { showWhitelistPage = false },
                progressProvider = { whitelistTransitionProgress.value },
                mode = "whitelist"
            )
        }

        if (showBypassListLayer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 0.22f * bypassListTransitionProgress.value
                    }
                    .background(Color.Black)
            )
        }

        if (showBypassListLayer) {
            BypassListPage(
                useMonet = useMonet,
                isZh = isZh,
                prefs = context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE),
                onBack = { showBypassListPage = false },
                progressProvider = { bypassListTransitionProgress.value }
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
    pagerState: PagerState? = null,
    backdrop: LayerBackdrop? = null,
    showBlur: Boolean = false,
    floatingNavBar: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (floatingNavBar) {
        if (useMonet) {
            val items = remember {
                AppDestinations.entries.map { NavigationItem(label = it.label, icon = it.filledIcon) }
            }
            val selectedIndex = remember(currentDestination) { AppDestinations.entries.indexOf(currentDestination) }
            MaterialFloatingNavigationBar(
                items = items,
                filledIcons = null,
                selectedIndex = selectedIndex,
                pagerState = pagerState,
                onItemClick = { onDestinationSelected(AppDestinations.entries[it]) },
                 modifier = modifier,
            )
        } else {
            iOSLikeFloatingNavigationBar(
                currentDestination = currentDestination,
                pagerState = pagerState,
                onDestinationSelected = onDestinationSelected,
                backdrop = backdrop,
                showBlur = showBlur,
                useMonet = useMonet,
                modifier = modifier,
            )
        }
        return
    }
    val barAlpha = 0.25f
    if (useMonet) {
        val miuixColors = MiuixTheme.colorScheme
        val isDark = isSystemInDarkTheme()
        val navModifier = if (showBlur) modifier.fillMaxWidth().then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) else modifier.fillMaxWidth()
        val barColor = if (isDark) miuixColors.surface.copy(alpha = barAlpha) else Color.White.copy(alpha = barAlpha)
        NavigationBar(modifier = navModifier, containerColor = barColor, contentColor = miuixColors.onSurface) {
            AppDestinations.entries.forEach { destination ->
                val isSelected = destination == currentDestination
                NavigationBarItem(
                    icon = { Icon(imageVector = if (isSelected) destination.filledIcon else destination.icon, contentDescription = destination.label) },
                    label = { androidx.compose.material3.Text(text = destination.label) },
                    selected = isSelected,
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

/**
 * iOS 风格悬浮底栏：移植 miuix example 的 [com.setoskins.thermal.ui.component.liquid.IosLiquidGlassNavigationBar]，
 * 实现真正的 iOS 液态玻璃（Liquid Glass）切换指示器，带折射、色散、内阴影与高光。
 */
@Composable
private fun iOSLikeFloatingNavigationBar(
    currentDestination: AppDestinations,
    onDestinationSelected: (AppDestinations) -> Unit,
    backdrop: LayerBackdrop?,
    showBlur: Boolean,
    pagerState: PagerState? = null,
    useMonet: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val items = remember {
        AppDestinations.entries.map { NavigationItem(label = it.label, icon = it.filledIcon) }
    }
    val selectedIndex = remember(currentDestination) { AppDestinations.entries.indexOf(currentDestination) }
    IosLiquidGlassNavigationBar(
        items = items,
        filledIcons = null,
        selectedIndex = selectedIndex,
        pagerState = pagerState,
        onItemClick = { onDestinationSelected(AppDestinations.entries[it]) },
        backdrop = backdrop,
        isBlurActive = showBlur && backdrop != null,
        useMonet = useMonet,
        modifier =  modifier,
    )
}
