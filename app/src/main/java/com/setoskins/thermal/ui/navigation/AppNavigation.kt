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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton as MaterialIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text as MaterialText
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Info

import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.setoskins.thermal.data.ModuleDetector
import com.setoskins.thermal.data.UpdateManager
import com.setoskins.thermal.ui.component.RestartDialog
import com.setoskins.thermal.ui.component.RootCheckDialog
import com.setoskins.thermal.ui.component.UpdateDialog
import com.setoskins.thermal.ui.component.liquid.IosLiquidGlassNavigationBar
import com.setoskins.thermal.ui.component.liquid.NavTab
import com.setoskins.thermal.ui.component.liquid.Os4FloatingNavigationBar
import com.setoskins.thermal.ui.component.BlurredBar
import com.setoskins.thermal.ui.component.MaterialPageTopBarScaffold
import com.setoskins.thermal.ui.component.rememberBlurBackdrop
import com.setoskins.thermal.ui.component.rememberMaterialBlurBackdrop
import com.setoskins.thermal.ui.screen.HomeScreen
import com.setoskins.thermal.ui.screen.FavoritesScreen
import com.setoskins.thermal.ui.screen.ProfileScreen
import com.setoskins.thermal.ui.screen.CreditsPage
import com.setoskins.thermal.ui.screen.DonatePage
import com.setoskins.thermal.ui.screen.BlacklistPage
import com.setoskins.thermal.ui.screen.BypassListPage
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import com.setoskins.thermal.R

enum class AppDestinations(val label: String, val labelEn: String, val icon: ImageVector, val filledIcon: ImageVector) {
    HOME("主页", "Home", Icons.Outlined.Home, Icons.Filled.Home),
    FAVORITES("日志", "Log", Icons.Outlined.Description, Icons.Filled.Description),
    PROFILE("关于", "About", Icons.Outlined.Info, Icons.Filled.Info),
}

@Composable
fun MyApplicationApp(
    useMonet: Boolean,
    onUseMonetChange: (Boolean) -> Unit,
    miuixMonet: Boolean,
    onMiuixMonetChange: (Boolean) -> Unit,
    floatingNavBar: Boolean,
    onFloatingNavBarChange: (Boolean) -> Unit,
    initialPage: Int = 0,
    onPageChange: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { AppDestinations.entries.size }
    )
    LaunchedEffect(pagerState.currentPage) {
        onPageChange(pagerState.currentPage)
    }
    val currentDestination by remember(pagerState) {
        derivedStateOf { AppDestinations.entries[pagerState.currentPage] }
    }
    var reloadTrigger by remember { mutableIntStateOf(0) }
    var logReloadTrigger by remember { mutableIntStateOf(0) }
    var showDonatePage by remember { mutableStateOf(false) }
    var showDonateLayer by remember { mutableStateOf(false) }
    val transitionProgress = remember { Animatable(0f) }
    var showCreditsPage by remember { mutableStateOf(false) }
    var showCreditsLayer by remember { mutableStateOf(false) }
    val creditsTransitionProgress = remember { Animatable(0f) }
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

    var showRootDialog by rememberSaveable { mutableStateOf(false) }

    var showRestartDialog by remember { mutableStateOf(false) }

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

    LaunchedEffect(Unit) {
        delay(700)
        ModuleDetector.prewarm()
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

                transitionProgress.snapTo(1f - backEvent.progress)
            }
            showDonatePage = false
        } catch (e: Exception) {

            scope.launch {
                transitionProgress.animateTo(1f, tween(400, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
            }
        }
    }

    LaunchedEffect(showCreditsPage) {
        if (showCreditsPage) {
            showCreditsLayer = true
            creditsTransitionProgress.animateTo(1f, tween(480, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
        } else {
            creditsTransitionProgress.animateTo(0f, tween(320, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
            showCreditsLayer = false
        }
    }

    PredictiveBackHandler(enabled = showCreditsPage) { progress ->
        try {
            progress.collect { backEvent ->
                creditsTransitionProgress.snapTo(1f - backEvent.progress)
            }
            showCreditsPage = false
        } catch (e: Exception) {
            scope.launch {
                creditsTransitionProgress.animateTo(1f, tween(400, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
            }
        }
    }

    val backdrop = if (useMonet) rememberMaterialBlurBackdrop() else rememberBlurBackdrop()
    val shaderSupported = remember { isRuntimeShaderSupported() }
    val reduceExpensiveEffects = showDonatePage || showDonateLayer || showCreditsPage || showCreditsLayer || showBlacklistPage || showBlacklistLayer || showWhitelistPage || showWhitelistLayer
    val showBlur = shaderSupported && backdrop != null && !reduceExpensiveEffects
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val homeScrollBehavior = MiuixScrollBehavior()
    val favoritesScrollBehavior = MiuixScrollBehavior()

    val logPageActive by remember(pagerState) {
        derivedStateOf { pagerState.currentPage == 1 && pagerState.currentPageOffsetFraction == 0f }
    }

    val pagerBusy by remember(pagerState) { derivedStateOf { pagerState.isScrollInProgress } }
    val isDark = isSystemInDarkTheme()
    val view = LocalView.current

    val pageSurface = if (useMonet) {
        MaterialTheme.colorScheme.surfaceContainer
    } else if (isDark) {
        Color.Black
    } else {
        MiuixTheme.colorScheme.surface
    }

    val effectiveFloatingNavBar = floatingNavBar && !useMonet
    val contentRecorded = effectiveFloatingNavBar && backdrop != null
    val normalMiuixBlur = !floatingNavBar && !useMonet && showBlur

    val normalMaterialBlur = !floatingNavBar && useMonet && showBlur

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageSurface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = -screenWidth.toPx() * 0.18f * maxOf(transitionProgress.value, blacklistTransitionProgress.value, whitelistTransitionProgress.value)
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()

                    .then(if (contentRecorded) Modifier.layerBackdrop(backdrop) else Modifier)
                    .background(pageSurface)
            ) {
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = if (effectiveFloatingNavBar) ({}) else ({
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

                    val bottomContentPadding = if (effectiveFloatingNavBar || normalMiuixBlur || normalMaterialBlur) 0.dp else innerPadding.calculateBottomPadding()

                    val bottomContentInset = if (normalMaterialBlur || normalMiuixBlur) innerPadding.calculateBottomPadding() else 0.dp
                    Box(modifier = Modifier.fillMaxSize()) {
                        HorizontalPager(
                            state = pagerState,
                            beyondViewportPageCount = 1,
                            modifier = Modifier
                                .fillMaxSize()

                                .then(if (normalMiuixBlur || normalMaterialBlur) Modifier.layerBackdrop(backdrop) else Modifier),
                            flingBehavior = PagerDefaults.flingBehavior(
                                state = pagerState,
                                snapAnimationSpec = tween(390, easing = CubicBezierEasing(0.12f, 0.0f, 0.05f, 1.0f))
                            )
                        ) { page ->
                            when (AppDestinations.entries[page]) {
                                AppDestinations.HOME -> {
                                    if (useMonet) {

                                        MaterialPageTopBarScaffold(
                                            title = if (isZh) "Seto温控" else "Seto Thermal",
                                            actions = {
                                                MaterialIconButton(
                                                    onClick = {
                                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        showRestartDialog = true
                                                        barVisible = false
                                                    }
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.restart),
                                                        contentDescription = if (isZh) "重启" else "Restart",

                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                }
                                            }
                                        ) { contentPaddingTop, scrollConnection ->
                                            HomeScreen(
                                                useMonet = true,
                                                reloadTrigger = reloadTrigger,
                                                scrollBehavior = null,
                                                materialScrollConnection = scrollConnection,
                                                contentPaddingTop = contentPaddingTop,
                                                bottomContentInset = bottomContentInset,
                                                floatingNavBar = effectiveFloatingNavBar,
                                                onNavigateToBlacklist = { showBlacklistPage = true },
                                                onNavigateToWhitelist = { showWhitelistPage = true },
                                                onNavigateToBypassList = { showBypassListPage = true },
                                                onDialogVisibilityChange = onDialogVisibilityChange,
                                                modifier = Modifier.padding(bottom = bottomContentPadding)
                                            )
                                        }
                                    } else {

                                        PageTopBarScaffold(
                                            title = if (isZh) "Seto温控" else "Seto Thermal",
                                            scrollBehavior = homeScrollBehavior,
                                            actions = {
                                                IconButton(
                                                    onClick = {
                                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        showRestartDialog = true
                                                        barVisible = false
                                                    },

                                                    modifier = Modifier.offset(y = 6.dp)
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.restart),
                                                        contentDescription = if (isZh) "重启" else "Restart",
                                                        tint = MiuixTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                }
                                            }
                                        ) { contentPaddingTop ->
                                            HomeScreen(
                                                useMonet = false,
                                                reloadTrigger = reloadTrigger,
                                                scrollBehavior = homeScrollBehavior,

                                                contentPaddingTop = (contentPaddingTop - 6.dp).coerceAtLeast(0.dp),
                                                bottomContentInset = bottomContentInset,
                                                floatingNavBar = effectiveFloatingNavBar,
                                                onNavigateToBlacklist = { showBlacklistPage = true },
                                                onNavigateToWhitelist = { showWhitelistPage = true },
                                                onNavigateToBypassList = { showBypassListPage = true },
                                                onDialogVisibilityChange = onDialogVisibilityChange,
                                                modifier = Modifier.padding(bottom = bottomContentPadding)
                                            )
                                        }
                                    }
                                }
                                AppDestinations.FAVORITES -> {
                                    if (useMonet) {

                                        MaterialPageTopBarScaffold(
                                            title = if (isZh) "日志" else "Log",
                                            actions = {
                                                val haptic = LocalHapticFeedback.current
                                                MaterialIconButton(
                                                    onClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        scope.launch { if (ModuleDetector.clearLog()) logReloadTrigger++ }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = MiuixIcons.Delete,
                                                        contentDescription = "Clear Log"
                                                    )
                                                }
                                            }
                                        ) { contentPaddingTop, scrollConnection ->
                                            FavoritesScreen(
                                                useMonet = true,
                                                reloadTrigger = logReloadTrigger,
                                                scrollBehavior = null,
                                                materialScrollConnection = scrollConnection,
                                                contentPaddingTop = contentPaddingTop,
                                                bottomContentInset = bottomContentInset,
                                                isPageActive = logPageActive,
                                                modifier = Modifier.padding(bottom = bottomContentPadding)
                                            )
                                        }
                                    } else {
                                        PageTopBarScaffold(
                                            title = if (isZh) "日志" else "Log",
                                            scrollBehavior = favoritesScrollBehavior,
                                            actions = {
                                                val haptic = LocalHapticFeedback.current
                                                IconButton(
                                                    onClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        scope.launch { if (ModuleDetector.clearLog()) logReloadTrigger++ }
                                                    },

                                                    modifier = Modifier.offset(y = 6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = MiuixIcons.Delete,
                                                        contentDescription = "Clear Log",
                                                        tint = MiuixTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        ) { contentPaddingTop ->
                                            FavoritesScreen(
                                                useMonet = false,
                                                reloadTrigger = logReloadTrigger,
                                                scrollBehavior = favoritesScrollBehavior,

                                                contentPaddingTop = (contentPaddingTop - 6.dp).coerceAtLeast(0.dp),
                                                bottomContentInset = bottomContentInset,
                                                isPageActive = logPageActive,
                                                modifier = Modifier.padding(bottom = bottomContentPadding)
                                            )
                                        }
                                    }
                                }
                                AppDestinations.PROFILE -> {
                                    ProfileScreen(
                                        useMonet = useMonet,
                                        onUseMonetChange = onUseMonetChange,
                                        miuixMonet = miuixMonet,
                                        onMiuixMonetChange = onMiuixMonetChange,
                                        floatingNavBar = floatingNavBar,
                                        onFloatingNavBarChange = onFloatingNavBarChange,
                                        onConfigImported = { reloadTrigger++ },
                                        onNavigateToDonate = { showDonatePage = true },
                                        onNavigateToCredits = { showCreditsPage = true },
                                        reduceEffects = reduceExpensiveEffects,

                                        isPageActive = pagerState.currentPage == 2,
                                        onDialogVisibilityChange = onDialogVisibilityChange,
                                        bottomContentInset = bottomContentInset,
                                        modifier = Modifier.padding(bottom = bottomContentPadding)
                                    )
                                }
                            }
                        }

                        if (showRootDialog) {
                            RootCheckDialog(
                                show = true,
                                isZh = isZh,
                                onDismiss = { showRootDialog = false },
                                onExit = { (context as? Activity)?.finish() },
                                onDismissStart = { barVisible = true }
                            )
                        }

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

            AnimatedVisibility(
                visible = effectiveFloatingNavBar && barVisible && dialogCount == 0,
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

        if (showCreditsLayer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 0.22f * creditsTransitionProgress.value
                    }
                    .background(Color.Black)
            )
        }

        if (showCreditsLayer) {
            CreditsPage(
                useMonet = useMonet,
                onDismiss = { showCreditsPage = false },
                progressProvider = { creditsTransitionProgress.value }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"
    if (floatingNavBar) {
        if (!useMonet) {
            val isDark = isSystemInDarkTheme()
            val selectedIndex = remember {
                mutableIntStateOf(pagerState?.currentPage ?: AppDestinations.entries.indexOf(currentDestination))
            }

            var navigating by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            LaunchedEffect(pagerState) {
                val ps = pagerState ?: return@LaunchedEffect

                snapshotFlow { ps.currentPage }
                    .collect { page ->
                        if (!navigating && selectedIndex.intValue != page) {
                            selectedIndex.intValue = page
                        }
                    }
            }
            val tabs = remember(isZh) {
                AppDestinations.entries.map { dest ->
                    NavTab(
                        label = if (isZh) dest.label else dest.labelEn,

                        icon = if (dest == AppDestinations.HOME) MiuixIcons.Home else dest.filledIcon,
                        onClick = {},
                    )
                }
            }
            Os4FloatingNavigationBar(
                tabs = tabs,
                selectedIndex = selectedIndex,
                backdrop = backdrop,
                showIcons = true,
                showLabels = true,

                advancedMaterial = true,
                isDarkTheme = isDark,
                onTabSelected = { index ->

                    if (selectedIndex.intValue == index) return@Os4FloatingNavigationBar
                    selectedIndex.intValue = index
                    navigating = true
                    scope.launch {
                        try {
                            pagerState?.animateScrollToPage(
                                index,
                                animationSpec = tween(420, easing = CubicBezierEasing(0.12f, 0.0f, 0.05f, 1.0f))
                            )
                        } finally {
                            navigating = false

                            pagerState?.let { selectedIndex.intValue = it.currentPage }
                        }
                    }
                },
                modifier = modifier,
            )
        }
        return
    }
    val barAlpha = 0.25f
    if (useMonet) {

        val destinations = AppDestinations.entries
        var selectedIndex by remember {
            mutableIntStateOf(pagerState?.currentPage ?: destinations.indexOf(currentDestination))
        }
        var navigating by remember { mutableStateOf(false) }
        LaunchedEffect(pagerState) {
            val ps = pagerState ?: return@LaunchedEffect
            snapshotFlow { ps.currentPage to ps.isScrollInProgress }.collect { (page, scrolling) ->
                if (!scrolling) navigating = false
                if (!navigating && selectedIndex != page) selectedIndex = page
            }
        }
        val blurActive = showBlur && backdrop != null
        val blendColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)
        val navModifier = modifier
            .fillMaxWidth()
            .wrapContentSize()
            .then(
                backdrop?.takeIf { blurActive }?.let { bd ->
                    Modifier.textureBlur(
                        backdrop = bd,
                        shape = RectangleShape,
                        blurRadius = 25f,
                        colors = BlurColors(
                            blendColors = listOf(BlendColorEntry(color = blendColor)),
                        ),
                    )
                } ?: Modifier
            )
        ShortNavigationBar(

            modifier = navModifier,
            windowInsets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
            ),
            containerColor = if (blurActive) {
                Color.Transparent
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
            arrangement = ShortNavigationBarArrangement.EqualWeight,
        ) {
            destinations.forEachIndexed { index, destination ->
                val isSelected = index == selectedIndex
                ShortNavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (index != selectedIndex) {
                            navigating = true
                            selectedIndex = index
                            onDestinationSelected(destination)
                        }
                    },
                    iconPosition = NavigationItemIconPosition.Top,
                    icon = {
                        Icon(
                            imageVector = if (isSelected) destination.filledIcon else destination.icon,
                            contentDescription = destination.label,
                        )
                    },
                    label = { MaterialText(text = destination.label) },
                )
            }
        }
    } else {
        BlurredBar(backdrop = backdrop, blurEnabled = showBlur) {
            MiuixNavigationBar(
                modifier = modifier.fillMaxWidth(),
                color = if (showBlur && backdrop != null) Color.Transparent else MiuixTheme.colorScheme.surface.copy(alpha = barAlpha),
            ) {
                AppDestinations.entries.forEach { destination ->

                    val icon = if (destination == AppDestinations.HOME) MiuixIcons.Home else destination.icon
                    MiuixNavigationBarItem(icon = icon, label = destination.label, selected = destination == currentDestination, onClick = { if (destination != currentDestination) onDestinationSelected(destination) })
                }
            }
        }
    }
}

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
    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"
    val items = remember(isZh) {
        AppDestinations.entries.map {
            NavigationItem(label = if (isZh) it.label else it.labelEn, icon = it.filledIcon)
        }
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

@Composable
private fun PageTopBarScaffold(
    title: String,
    scrollBehavior: ScrollBehavior?,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (Dp) -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val collapsedFraction by remember(scrollBehavior) { derivedStateOf { scrollBehavior?.state?.collapsedFraction ?: 0f } }
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {

                Box(modifier = Modifier.offset(y = (-6).dp)) {
                    TopAppBar(
                        title = title,
                        largeTitle = title,
                        largeTitleColor = Color.Transparent,
                        color = if (isDark) Color.Black else MiuixTheme.colorScheme.surface,
                        scrollBehavior = scrollBehavior,
                        bottomContent = {
                            val height = (24 * (1f - collapsedFraction.coerceIn(0f, 1f))).dp
                            Spacer(modifier = Modifier.height(height))
                        },
                        actions = actions
                    )
                }
            }
        ) { innerPadding ->
            content(innerPadding.calculateTopPadding())
        }
        Text(
            text = title,
            fontSize = 36.sp,
            fontWeight = FontWeight.Normal,
            color = MiuixTheme.colorScheme.onBackground,
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 58.dp, start = 28.dp)
                .graphicsLayer {
                    val fraction = collapsedFraction.coerceIn(0f, 1f)

                    alpha = (1f - fraction * 3f).coerceIn(0f, 1f)
                    translationY = -fraction * 45.dp.toPx()
                }
        )
    }
}
