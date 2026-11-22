package com.setoskins.thermal.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.TopAppBarDefaults
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun PageTopBarScaffold(
    title: String,
    scrollBehavior: ScrollBehavior?,
    blurEnabled: Boolean,
    os4: Boolean = false,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (Dp) -> Unit,
) {
    val barColor = if (isSystemInDarkTheme()) Color.Black else MiuixTheme.colorScheme.surface

    var navHeightPx by remember { mutableIntStateOf(0) }
    val measuredNavigationIcon: @Composable () -> Unit = {
        Box(modifier = Modifier.onSizeChanged { if (it.height > navHeightPx) navHeightPx = it.height }) {
            navigationIcon()
        }
    }
    if (os4) {
        val glassBackdrop = rememberBlurBackdrop(barColor).takeIf { blurEnabled }
        var barHeightPx by remember { mutableIntStateOf(0) }
        val barHeight = with(LocalDensity.current) { barHeightPx.toDp() }

        val collapsedFraction by remember(scrollBehavior) {
            derivedStateOf { (scrollBehavior?.state?.collapsedFraction ?: 0f).coerceIn(0f, 1f) }
        }
        val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (glassBackdrop != null) Modifier.layerBackdrop(glassBackdrop) else Modifier)
            ) {
                val expandedPx = barHeightPx
                val collapsedPx = with(LocalDensity.current) {
                    (TopAppBarDefaults.CollapsedHeight + statusBarInset).roundToPx()
                }
                val insetPx = expandedPx - ((expandedPx - collapsedPx) * collapsedFraction).toInt()
                content(with(LocalDensity.current) { insetPx.coerceAtLeast(collapsedPx).toDp() })
            }
            Os4GlassTopAppBar(
                title = title,
                largeTitle = title,
                backdrop = glassBackdrop,
                progressiveBlur = glassBackdrop != null,
                scrollBehavior = scrollBehavior,
                modifier = Modifier.align(Alignment.TopCenter),

                contentModifier = Modifier.onSizeChanged { size ->
                    if (size.height > barHeightPx) barHeightPx = size.height
                },
                bottomContent = {
                    Spacer(modifier = Modifier.height((10 * (1f - collapsedFraction)).dp))
                },
                navigationIcon = measuredNavigationIcon,
                actions = actions,
            )
        }
        return
    }
    val backdrop = rememberBlurBackdrop(barColor)
    val blurCapable = blurEnabled && backdrop != null
    val contentScrolled by remember(scrollBehavior) {
        derivedStateOf { (scrollBehavior?.state?.contentOffset ?: 0f) < -1f }
    }
    val blurActive = blurCapable && contentScrolled
    val collapsedFraction by remember(scrollBehavior) { derivedStateOf { scrollBehavior?.state?.collapsedFraction ?: 0f } }
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                BlurredTopBar(
                    backdrop = backdrop,
                    blurEnabled = blurActive,
                ) {
                    TopAppBar(
                        title = title,
                        largeTitle = title,
                        largeTitleColor = MiuixTheme.colorScheme.onBackground,
                        color = if (blurActive) Color.Transparent else barColor,
                        scrollBehavior = scrollBehavior,
                        navigationIcon = measuredNavigationIcon,
                        bottomContent = {
                            val height = (10 * (1f - collapsedFraction.coerceIn(0f, 1f))).dp
                            Spacer(modifier = Modifier.height(height))
                        },
                        actions = actions
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (blurCapable) Modifier.layerBackdrop(backdrop) else Modifier)
            ) {
                content(innerPadding.calculateTopPadding())
            }
        }
    }
}
