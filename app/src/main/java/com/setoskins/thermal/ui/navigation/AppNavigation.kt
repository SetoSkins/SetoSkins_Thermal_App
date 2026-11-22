package com.setoskins.thermal.ui.navigation

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
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
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import kotlin.math.abs
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Refresh
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
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
import androidx.compose.ui.draw.clipToBounds
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
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import com.setoskins.thermal.ui.component.Os4GlassTopAppBar
import com.setoskins.thermal.ui.component.Os4TopBarIconButton
import com.setoskins.thermal.ui.component.PageTopBarScaffold
import top.yukonga.miuix.kmp.material.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Refresh

import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.setoskins.thermal.data.ModuleDetector
import com.setoskins.thermal.data.UpdateManager
import com.setoskins.thermal.ui.component.RestartDialog
import com.setoskins.thermal.ui.component.RootCheckDialog
import com.setoskins.thermal.ui.component.UpdateDialog
import com.setoskins.thermal.ui.component.liquid.NavTab
import com.setoskins.thermal.ui.component.liquid.Os4FloatingNavigationBar
import com.setoskins.thermal.ui.component.BlurredBar
import com.setoskins.thermal.ui.component.BlurredMaterialBar
import com.setoskins.thermal.ui.component.BlurredTopBar
import com.setoskins.thermal.ui.component.LocalGlassBackdrop
import com.setoskins.thermal.ui.component.LocalOs4Style
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
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.CoroutineScope
import com.setoskins.thermal.R

private val PagerEase = CubicBezierEasing(0.12f, 0.0f, 0.05f, 1.0f)
private val OverlayEase = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
private val PagerSettleSpec = tween<Float>(420, easing = PagerEase)
private val PagerFlingSpec = tween<Float>(390, easing = PagerEase)
private val OverlayEnterSpec = tween<Float>(480, easing = OverlayEase)
private val OverlayExitSpec = tween<Float>(320, easing = OverlayEase)
private val OverlayBackCancelSpec = tween<Float>(400, easing = OverlayEase)

@Stable
private class OverlayPageState {
    var open by mutableStateOf(false)
    var layerVisible by mutableStateOf(false)
        private set
    val progress = Animatable(0f)

    @Composable
    fun bind(scope: CoroutineScope) {
        LaunchedEffect(open) {
            if (open) {
                layerVisible = true
                progress.animateTo(1f, OverlayEnterSpec)
            } else {
                progress.animateTo(0f, OverlayExitSpec)
                layerVisible = false
            }
        }
        PredictiveBackHandler(enabled = open) { backProgress ->
            try {
                backProgress.collect { progress.snapTo(1f - it.progress) }
                open = false
            } catch (e: Exception) {
                scope.launch { progress.animateTo(1f, OverlayBackCancelSpec) }
            }
        }
    }
}

@Composable
private fun rememberOverlayPage(): OverlayPageState = remember { OverlayPageState() }

@Composable
private fun OverlayPageHost(
    page: OverlayPageState,
    content: @Composable () -> Unit,
) {
    if (!page.layerVisible) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = 0.22f * page.progress.value }
            .background(Color.Black)
    )
    content()
}

enum class AppDestinations(val label: String, val labelEn: String, val icon: ImageVector, val filledIcon: ImageVector) {
    HOME("主页", "Home", Icons.Outlined.Home, Icons.Filled.Home),
    FAVORITES("日志", "Log", Icons.Outlined.Description, Icons.Filled.Description),
    PROFILE("关于", "About", Icons.Outlined.Info, Icons.Filled.Info),
}

@Composable
private fun rememberPagerSettleSkipFlingBehavior(
    pagerState: PagerState,
    snapAnimationSpec: AnimationSpec<Float>,
): TargetedFlingBehavior {
    val snapFling = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = snapAnimationSpec
    )
    return remember(snapFling) {
        object : TargetedFlingBehavior {
            override suspend fun ScrollScope.performFling(
                initialVelocity: Float,
                onRemainingDistanceUpdated: (Float) -> Unit,
            ): Float {
                if (abs(initialVelocity) < 150f && abs(pagerState.currentPageOffsetFraction) < 0.001f) {
                    return 0f
                }
                return with(snapFling) {
                    performFling(initialVelocity, onRemainingDistanceUpdated)
                }
            }
        }
    }
}

@Composable
fun MyApplicationApp(
    useMonet: Boolean,
    onUseMonetChange: (Boolean) -> Unit,
    miuixMonet: Boolean,
    onMiuixMonetChange: (Boolean) -> Unit,
    floatingNavBar: Boolean,
    onFloatingNavBarChange: (Boolean) -> Unit,
    os4Style: Boolean = true,
    onOs4StyleChange: (Boolean) -> Unit = {},
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
    val donatePage = rememberOverlayPage()
    val creditsPage = rememberOverlayPage()
    val blacklistPage = rememberOverlayPage()
    val whitelistPage = rememberOverlayPage()
    val bypassListPage = rememberOverlayPage()
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

    donatePage.bind(scope)
    creditsPage.bind(scope)
    blacklistPage.bind(scope)
    whitelistPage.bind(scope)
    bypassListPage.bind(scope)

    LaunchedEffect(Unit) {
        delay(700)
        ModuleDetector.prewarm()
    }

    val backdrop = if (useMonet) rememberMaterialBlurBackdrop() else rememberBlurBackdrop()
    val shaderSupported = remember { isRuntimeShaderSupported() }

    val reduceExpensiveEffects by remember {
        derivedStateOf {
            listOf(donatePage, creditsPage, blacklistPage, whitelistPage, bypassListPage)
                .any { it.layerVisible && it.progress.value >= 1f }
        }
    }
    val showBlur = shaderSupported && backdrop != null && !reduceExpensiveEffects
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val homeScrollBehavior = MiuixScrollBehavior()
    val favoritesScrollBehavior = MiuixScrollBehavior()

    val logPageActive by remember(pagerState) {
        derivedStateOf { pagerState.currentPage == 1 && pagerState.currentPageOffsetFraction == 0f }
    }

    val isDark = isSystemInDarkTheme()
    val prefs = remember(context) { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    val pageSurface = if (useMonet) {
        MaterialTheme.colorScheme.surfaceContainer
    } else if (isDark) {
        Color.Black
    } else {
        MiuixTheme.colorScheme.surface
    }

    val effectiveFloatingNavBar = floatingNavBar && !useMonet
    val contentRecorded = effectiveFloatingNavBar && backdrop != null
    val normalBlur = !effectiveFloatingNavBar && showBlur
    val os4Active = os4Style && !useMonet
    val os4GlassBackdrop = rememberLayerBackdrop {
        drawRect(pageSurface)
        drawContent()
    }

    CompositionLocalProvider(
        LocalOs4Style provides os4Active,
        LocalGlassBackdrop provides os4GlassBackdrop,
    ) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageSurface)
    ) {
        if (os4Active) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .layerBackdrop(os4GlassBackdrop)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = -screenWidth.toPx() * 0.18f * maxOf(
                        donatePage.progress.value,
                        blacklistPage.progress.value,
                        whitelistPage.progress.value
                    )
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
                                        animationSpec = PagerSettleSpec
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

                    val bottomContentPadding = if (effectiveFloatingNavBar || normalBlur) 0.dp else innerPadding.calculateBottomPadding()

                    val bottomContentInset = if (normalBlur) innerPadding.calculateBottomPadding() else 0.dp
                    Box(modifier = Modifier.fillMaxSize()) {
                        HorizontalPager(
                            state = pagerState,
                            beyondViewportPageCount = 1,
                            overscrollEffect = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clipToBounds()
                                .then(if (normalBlur) Modifier.layerBackdrop(backdrop) else Modifier),
                            flingBehavior = rememberPagerSettleSkipFlingBehavior(pagerState, PagerFlingSpec)
                        ) { page ->
                            when (AppDestinations.entries[page]) {
                                AppDestinations.HOME -> {
                                    if (useMonet) {
                                        MaterialPageTopBarScaffold(
                                            title = if (isZh) "Seto温控" else "Seto Thermal",
                                            blurEnabled = showBlur,
                                            actions = {
                                                MaterialIconButton(
                                                    onClick = {
                                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        showRestartDialog = true
                                                        barVisible = false
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Refresh,
                                                        contentDescription = if (isZh) "重启" else "Restart",
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
                                                onNavigateToBlacklist = { blacklistPage.open = true },
                                                onNavigateToWhitelist = { whitelistPage.open = true },
                                                onNavigateToBypassList = { bypassListPage.open = true },
                                                onDialogVisibilityChange = onDialogVisibilityChange,
                                                modifier = Modifier.padding(bottom = bottomContentPadding)
                                            )
                                        }
                                    } else {
                                        PageTopBarScaffold(
                                            title = if (isZh) "Seto温控" else "Seto Thermal",
                                            scrollBehavior = homeScrollBehavior,
                                            blurEnabled = showBlur,
                                            os4 = os4Active,
                                            actions = {
                                                if (os4Active) {
                                                    Os4TopBarIconButton(
                                                        onClick = {
                                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            showRestartDialog = true
                                                            barVisible = false
                                                        },
                                                        icon = MiuixIcons.Refresh,
                                                        contentDescription = if (isZh) "重启" else "Restart",
                                                    )
                                                } else {
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
                                        ) { contentPaddingTop ->
                                            HomeScreen(
                                                useMonet = false,
                                                reloadTrigger = reloadTrigger,
                                                scrollBehavior = homeScrollBehavior,
                                                contentPaddingTop = contentPaddingTop,
                                                bottomContentInset = bottomContentInset,
                                                floatingNavBar = effectiveFloatingNavBar,
                                                onNavigateToBlacklist = { blacklistPage.open = true },
                                                onNavigateToWhitelist = { whitelistPage.open = true },
                                                onNavigateToBypassList = { bypassListPage.open = true },
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
                                            blurEnabled = showBlur,
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
                                            blurEnabled = showBlur,
                                            os4 = os4Active,
                                            actions = {
                                                val haptic = LocalHapticFeedback.current
                                                if (os4Active) {
                                                    Os4TopBarIconButton(
                                                        onClick = {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            scope.launch { if (ModuleDetector.clearLog()) logReloadTrigger++ }
                                                        },
                                                        icon = MiuixIcons.Delete,
                                                        contentDescription = "Clear Log",
                                                    )
                                                } else {
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
                                        ) { contentPaddingTop ->
                                            FavoritesScreen(
                                                useMonet = false,
                                                reloadTrigger = logReloadTrigger,
                                                scrollBehavior = favoritesScrollBehavior,
                                                contentPaddingTop = contentPaddingTop,
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
                                        os4Style = os4Style,
                                        onOs4StyleChange = onOs4StyleChange,
                                        onConfigImported = { reloadTrigger++ },
                                        onNavigateToDonate = { donatePage.open = true },
                                        onNavigateToCredits = { creditsPage.open = true },
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
                                        animationSpec = PagerSettleSpec
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

        OverlayPageHost(donatePage) {
            DonatePage(
                useMonet = useMonet,
                onDismiss = { donatePage.open = false },
                progressProvider = { donatePage.progress.value }
            )
        }

        OverlayPageHost(creditsPage) {
            CreditsPage(
                useMonet = useMonet,
                onDismiss = { creditsPage.open = false },
                progressProvider = { creditsPage.progress.value }
            )
        }

        OverlayPageHost(blacklistPage) {
            BlacklistPage(
                useMonet = useMonet,
                isZh = isZh,
                prefs = prefs,
                onBack = { blacklistPage.open = false },
                progressProvider = { blacklistPage.progress.value }
            )
        }

        OverlayPageHost(whitelistPage) {
            BlacklistPage(
                useMonet = useMonet,
                isZh = isZh,
                prefs = prefs,
                onBack = { whitelistPage.open = false },
                progressProvider = { whitelistPage.progress.value },
                mode = "whitelist"
            )
        }

        OverlayPageHost(bypassListPage) {
            BypassListPage(
                useMonet = useMonet,
                isZh = isZh,
                prefs = prefs,
                onBack = { bypassListPage.open = false },
                progressProvider = { bypassListPage.progress.value }
            )
        }
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
                                animationSpec = PagerSettleSpec
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
        BlurredMaterialBar(backdrop = backdrop, blurEnabled = blurActive) {
        ShortNavigationBar(

            modifier = modifier
                .fillMaxWidth()
                .wrapContentSize(),
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
