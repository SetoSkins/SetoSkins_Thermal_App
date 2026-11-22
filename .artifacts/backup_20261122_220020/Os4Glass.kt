package com.setoskins.thermal.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardColors
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassDialog
import top.yukonga.miuix.kmp.glass.GlassIconButton
import top.yukonga.miuix.kmp.glass.GlassTopAppBar
import top.yukonga.miuix.kmp.glass.GlassTopAppBarDefaults
import top.yukonga.miuix.kmp.glass.glassPanel
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.ChevronBackward
import top.yukonga.miuix.kmp.material.EdgeStroke
import top.yukonga.miuix.kmp.material.GlassDefaults
import top.yukonga.miuix.kmp.material.GlassEdgeStrokes
import top.yukonga.miuix.kmp.material.GlassMaterial
import top.yukonga.miuix.kmp.material.GlassShadow
import top.yukonga.miuix.kmp.material.GlassShadows
import top.yukonga.miuix.kmp.material.GlassShape
import top.yukonga.miuix.kmp.material.GlassStyle
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** OS4（miuix v0.9.5-beta03 玻璃材质）样式开�?*/
val LocalOs4Style = compositionLocalOf { false }

/** OS4 玻璃组件采样用的背景层；为空时组件自动走不透明兜底 */
val LocalGlassBackdrop = staticCompositionLocalOf<Backdrop?> { null }

/** OS4 对话框宿主：OS4 �?GlassDialog，OS3 保持�?OverlayDialog */
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
 * OS4 返回按钮：按 HyperMusicCover �?PageBackButtonOs4 实现�?
 *
 * 玻璃胶囊 + miuix 玻璃图标集的 ChevronBackward�?4dp，RTL 时水平镜像�?
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
 * OS4 卡片：按 HyperMusicCover �?AppCard 实现�?
 *
 * 外层 glassPanel 只负责边光与兜底底色（不采样页面背景，避免卡片模糊到自身），
 * 内层仍用 miuix Card 承接裁剪、内容色与内边距，填充改为透明�?
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

/**
 * OS4 顶栏右上角的玻璃图标按钮 —�?�?HyperExtreme �?`actions = { GlassIconButton { �?} }` 一致�?
 *
 * 必须放在 [Os4GlassTopAppBar] �?`actions` 槽位里调用：按钮会继承顶栏的 backdrop�?
 * 玻璃材质与收起进度，未滚动时只显示图标，开始滚动后浮出玻璃圆底�?
 */
@Composable
fun Os4TopBarIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String? = null,
) {
    GlassIconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
        )
    }
}

/**
 * OS4 玻璃顶栏 —�?参数与行为对�?HyperExtreme �?`TabHost` / `DetailTabHost`�?
 *
 * 与旧实现的区别（2026-11-22，用户要求「和 HyperExtreme 一样」）�?
 * 1. **不再自绘色带 / 压暗�?/ 加�?ramp**：全部交�?miuix [GlassTopAppBar] 的默认实现，
 *    展开态与 HyperExtreme 一样是透明顶栏，滚动后才出现默认色带；
 * 2. **默认开�?`progressiveBlur`**：顶部实、向下渐隐的模糊带由 miuix 内部录制并绘制，
 *    这正�?HyperExtreme 顶栏的观感来源；
 * 3. 大标�?�?小标题的收起动画、标题模糊、玻璃按钮浮出全部走库的默认 spec�?
 *    不再�?`bottomContent` 额外撑高收起态�?
 *
 * 页面需要把内容放在 `layerBackdrop` 里、并把顶栏叠在内容上层（�?[PageTopBarScaffold]），
 * `progressiveBlur` 才有可采样的背景�?
 *
 * 注意：这里用的是 miuix 原版摆放（大标题�?52dp 处、左�?26dp）—�?�?HyperExtreme 的大标题观感�?
 * [PageTopBarScaffold] �?OS4 分支就是直接调用它，OS3 分支仍用自己�?`TopAppBar` 摆放�?
 */
@Composable
fun Os4GlassTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    expandable: Boolean = true,
    progressiveBlur: Boolean = true,
    backdrop: Backdrop? = null,
    largeTitle: String = title,
    subtitle: String = "",
    scrollBehavior: ScrollBehavior? = null,
    bandBrush: Brush = GlassTopAppBarDefaults.bandBrush(MiuixTheme.colorScheme.surface),
    bandOverhang: Dp = GlassTopAppBarDefaults.BandOverhang,
    titleBlurRadius: Dp = GlassTopAppBarDefaults.TitleBlurRadius,
    style: GlassStyle = GlassDefaults.Style,
    material: GlassMaterial = GlassTopAppBarDefaults.buttonMaterial(),
    alpha: Float = 1f,
    buttonSize: Dp = GlassTopAppBarDefaults.ButtonSize,
    buttonShape: GlassShape = GlassShape(buttonSize / 2),
    fill: Color = GlassTopAppBarDefaults.buttonFill(),
    stroke: EdgeStroke? = GlassTopAppBarDefaults.buttonStroke(),
    buttonShadow: GlassShadow? = GlassShadows.Regular,
    defaultWindowInsetsPadding: Boolean = true,
    titleAlpha: () -> Float = { 1f },
    backgroundAlpha: () -> Float = { 1f },
    centerObstructionStart: Dp = Dp.Unspecified,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable () -> Unit = {},
) {
    GlassTopAppBar(
        title = title,
        modifier = modifier,
        contentModifier = contentModifier,
        expandable = expandable,
        progressiveBlur = progressiveBlur,
        backdrop = backdrop,
        largeTitle = largeTitle,
        subtitle = subtitle,
        scrollBehavior = scrollBehavior,
        bandBrush = bandBrush,
        bandOverhang = bandOverhang,
        titleBlurRadius = titleBlurRadius,
        style = style,
        material = material,
        alpha = alpha,
        buttonSize = buttonSize,
        buttonShape = buttonShape,
        fill = fill,
        stroke = stroke,
        buttonShadow = buttonShadow,
        defaultWindowInsetsPadding = defaultWindowInsetsPadding,
        titleAlpha = titleAlpha,
        backgroundAlpha = backgroundAlpha,
        centerObstructionStart = centerObstructionStart,
        navigationIcon = navigationIcon,
        actions = actions,
        bottomContent = bottomContent,
    )
}

