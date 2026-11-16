package com.setoskins.thermal.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 底栏使用的 miuix 模糊半径（dp）。 */
private const val MiuixBarBlurRadius = 40f

// 以下顶栏参数与 miuix 自身的 GlassTopAppBar 保持一致（GlassTopAppBarDefaults）。
/** 顶栏渐进模糊的最大半径（dp），对应 miuix 的 GlassTopAppBarDefaults.ProgressiveBlurRadius。 */
private const val MiuixTopBarBlurRadius = 80f

/** 顶栏渐变曲线，对应 GlassTopAppBar 的 ProgressiveBlur.Top.copy(curve = 0.5f)。 */
private const val MiuixTopBarGradientCurve = 0.5f

/** 顶栏覆盖在模糊之上的底色不透明度，对应 GlassTopAppBar 的 surface.copy(alpha = 0.3f)。 */
private const val MiuixTopBarTintAlpha = 0.3f

@Composable
fun rememberBlurBackdrop(baseColor: Color = MiuixTheme.colorScheme.surface): LayerBackdrop? {
    if (!isRuntimeShaderSupported()) return null
    return rememberLayerBackdrop {
        drawRect(baseColor)
        drawContent()
    }
}

@Composable
fun rememberMaterialBlurBackdrop(): LayerBackdrop? {
    if (!isRuntimeShaderSupported()) return null
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer
    return rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
}

@Composable
fun BlurredMaterialBar(
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    content: @Composable () -> Unit,
) {
    val blendColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)
    Box(
        modifier = if (blurEnabled && backdrop != null) {
            Modifier.textureBlur(
                backdrop = backdrop,
                shape = RectangleShape,
                blurRadius = 25f,
                colors = BlurColors(
                    blendColors = listOf(BlendColorEntry(color = blendColor)),
                ),
            )
        } else {
            Modifier
        },
    ) {
        content()
    }
}

@Composable
fun BlurredBar(
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    content: @Composable () -> Unit,
) {
    val blendColors = if (isSystemInDarkTheme()) {
        listOf(
            BlendColorEntry(Color(0x75000000), BlurBlendMode.ColorBurn),
            BlendColorEntry(Color(0x52000000), BlurBlendMode.SrcOver),
        )
    } else {
        listOf(
            BlendColorEntry(Color(0x340034F9), BlurBlendMode.Overlay),
            BlendColorEntry(Color(0xB3FFFFFF), BlurBlendMode.HardLight),
        )
    }
    Box(
        modifier = if (blurEnabled && backdrop != null) {
            Modifier.textureBlur(
                backdrop = backdrop,
                shape = RectangleShape,
                blurRadius = MiuixBarBlurRadius,
                colors = BlurDefaults.blurColors(blendColors = blendColors),
            )
        } else {
            Modifier
        },
    ) {
        content()
    }
}

/**
 * Miuix 风格的可折叠标题栏模糊层。
 *
 * 参数与 miuix 自身的 GlassTopAppBar 完全一致：`progressiveTextureBlur` + `ProgressiveBlur.Top`、
 * 曲线 0.5、最大半径 80dp、底色 `surface.copy(alpha = 0.3f)`，顶部模糊最强、向下淡出到清晰，
 * 因此滚动时内容从标题栏下方经过会被模糊，且标题栏底边不会出现生硬的截断。
 *
 * 调用方需要把标题栏下方的滚动内容通过 `Modifier.layerBackdrop(backdrop)` 记录进 [backdrop]，
 * 并且把标题栏自身的 `color` 设为 [Color.Transparent]。
 *
 * @param backdrop 记录标题栏下方滚动内容的背景层。
 * @param blurEnabled 是否启用模糊（不支持 RuntimeShader 或需要节省性能时传 false）。
 * @param barColor 标题栏原来的不透明背景色，用作静止时的底色与模糊层之上的蒙版。
 * @param alpha 模糊层不透明度，可用于随滚动渐显的标题栏（如「关于」页）。
 */
@Composable
fun BlurredTopBar(
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    barColor: Color,
    modifier: Modifier = Modifier,
    alpha: () -> Float = { 1f },
    content: @Composable () -> Unit,
) {
    val tintColors = remember(barColor) {
        listOf(BlendColorEntry(barColor.copy(alpha = MiuixTopBarTintAlpha), BlurBlendMode.SrcOver))
    }
    val colors = BlurDefaults.blurColors(blendColors = tintColors)
    Box(modifier) {
        if (blurEnabled && backdrop != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { this.alpha = alpha().coerceIn(0f, 1f) }
                    .progressiveTextureBlur(
                        backdrop = backdrop,
                        shape = RectangleShape,
                        blurRadius = MiuixTopBarBlurRadius,
                        gradient = ProgressiveBlur.Top.copy(curve = MiuixTopBarGradientCurve),
                        colors = colors,
                    ),
            )
        }
        content()
    }
}
