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

/**
 * 页面顶栏 + 内容宿主（OS3 / OS4 共用）。
 *
 * OS3：`Scaffold` + [TopAppBar]（`bottomContent` 展开态额外 10dp），内容按 `innerPadding` 下移。
 * OS4：内容全屏并作为玻璃采样源，玻璃顶栏浮在内容上层；顶栏直接用 miuix [Os4GlassTopAppBar]
 * （= HyperExtreme 的 `GlassTopAppBar`）—— 大标题的位置、字号、收起动画、标题模糊全部走库默认，
 * 与 HyperExtreme 逐像素一致。
 *
 * @param content 参数为内容需要预留的顶栏高度（展开态，含状态栏）。
 */
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
    // OS3 的大标题按「导航按钮实际高度」定位（顶端 = 按钮底端 + 4dp）。
    var navHeightPx by remember { mutableIntStateOf(0) }
    val measuredNavigationIcon: @Composable () -> Unit = {
        Box(modifier = Modifier.onSizeChanged { if (it.height > navHeightPx) navHeightPx = it.height }) {
            navigationIcon()
        }
    }
    if (os4) {
        // 内容全屏（并作为玻璃采样源），顶栏浮在内容上层。
        // 顶栏实测展开态高度（含状态栏留白）后交给内容做顶部留白 —— 与 HyperExtreme 的
        // `contentModifier = Modifier.onSizeChanged { … }` + `LocalTopBarHeight` 同一套。
        val glassBackdrop = rememberBlurBackdrop(barColor).takeIf { blurEnabled }
        var barHeightPx by remember { mutableIntStateOf(0) }
        val barHeight = with(LocalDensity.current) { barHeightPx.toDp() }
        // 与 OS3 的 `bottomContent` 一致：展开态大标题下方额外留 10dp（收起时归零）。
        // 少这一段，OS4 的内容整体会比 OS3 高 10dp、大标题到第一张卡片的间距也会更小。
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
                // 内容顶部留白跟随顶栏实时高度，与 OS3 的 `Scaffold.innerPadding` 一致。
                //
                // OS3 的顶栏占位由 Scaffold 按「当前顶栏高度」给出，收起时同步变小，
                // 内容因此随顶栏一起往上走 —— 这就是「吸附上移」。
                // OS4 原先恒传展开态高度，收起后留白不收缩，手指的位移全被顶栏吃掉，
                // 内容纹丝不动，看起来就是「没有吸附」。
                //
                // barHeightPx 是展开态实测高度；收起时收缩掉「展开高度 - 52dp 小标题栏」那一段。
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
                // 只记录展开态的最大高度：收起时内容头部留白不跟着变，避免内容上下跳。
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
