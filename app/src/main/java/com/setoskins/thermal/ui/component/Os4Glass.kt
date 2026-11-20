package com.setoskins.thermal.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardColors
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassDialog
import top.yukonga.miuix.kmp.glass.GlassIconButton
import top.yukonga.miuix.kmp.glass.glassPanel
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.ChevronBackward
import top.yukonga.miuix.kmp.material.GlassDefaults
import top.yukonga.miuix.kmp.material.GlassEdgeStrokes
import top.yukonga.miuix.kmp.material.GlassShape
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** OS4（miuix v0.9.5-beta01 玻璃材质）样式开关 */
val LocalOs4Style = compositionLocalOf { false }

/** OS4 玻璃组件采样用的背景层；为空时组件自动走不透明兜底 */
val LocalGlassBackdrop = staticCompositionLocalOf<Backdrop?> { null }

/** OS4 对话框宿主：OS4 用 GlassDialog，OS3 保持原 OverlayDialog */
@Composable
fun Os4DialogHost(
    show: Boolean,
    title: String?,
    summary: String? = null,
    onDismissRequest: () -> Unit,
    onDismissFinished: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (LocalOs4Style.current) {
        GlassDialog(
            show = show,
            onDismissRequest = onDismissRequest,
            backdrop = LocalGlassBackdrop.current,
            title = title,
            summary = summary,
            onDismissFinished = onDismissFinished,
        ) { content() }
    } else {
        OverlayDialog(
            show = show,
            title = title ?: "",
            summary = summary ?: "",
            onDismissRequest = onDismissRequest,
            onDismissFinished = onDismissFinished,
            content = content,
        )
    }
}

/**
 * OS4 返回按钮：按 HyperMusicCover 的 PageBackButtonOs4 实现。
 *
 * 玻璃胶囊 + miuix 玻璃图标集的 ChevronBackward，24dp，RTL 时水平镜像。
 */
@Composable
fun Os4BackButton(onClick: () -> Unit, contentDescription: String = "Back") {
    GlassIconButton(onClick = onClick) {
        val layoutDirection = LocalLayoutDirection.current
        Icon(
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
                },
            imageVector = MiuixIcons.Glass.ChevronBackward,
            contentDescription = contentDescription,
            tint = MiuixTheme.colorScheme.onSurface,
        )
    }
}

/**
 * OS4 卡片：按 HyperMusicCover 的 AppCard 实现。
 *
 * 深色模式与 HyperMusicCover「卡片柔光玻璃关闭」一致：普通 miuix Card，
 * 卡片色取 surfaceContainer（#242424），不叠加边光/阴影。
 * 浅色模式外层 glassPanel 只负责边光与兜底底色（不采样页面背景，避免卡片模糊到自身），
 * 内层仍用 miuix Card 承接裁剪、内容色与内边距，填充改为透明。
 */
@Composable
fun Os4GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CardDefaults.CornerRadius,
    insideMargin: PaddingValues = CardDefaults.InsideMargin,
    colors: CardColors = CardDefaults.defaultColors(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val dark = isSystemInDarkTheme()
    if (dark) {
        Card(
            modifier = modifier,
            cornerRadius = cornerRadius,
            insideMargin = insideMargin,
            colors = colors,
            content = content,
        )
        return
    }
    val shape = remember(cornerRadius) { GlassShape(cornerRadius) }
    val fallbackShape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    Box(
        modifier = modifier.glassPanel(
            backdrop = null,
            shape = shape,
            style = GlassDefaults.style(dark),
            stroke = GlassEdgeStrokes.forTheme(dark),
            shadow = null,
            fallback = Modifier.background(colors.color, fallbackShape),
        ),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = cornerRadius,
            insideMargin = insideMargin,
            colors = colors.copy(color = Color.Transparent),
            content = content,
        )
    }
}
