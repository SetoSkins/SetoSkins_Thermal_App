package com.setoskins.thermal.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 底栏使用的 miuix 模糊半径（dp）。 */
private const val MiuixBarBlurRadius = 40f

// 以下顶栏参数与 miuix 示例中标题栏外层的 BlurredBar 完全一致。
/** 标题栏模糊半径（dp），对应 miuix 示例标题栏的 blurRadius = 25f。 */
private const val MiuixTopBarBlurRadius = 25f

/** 标题栏覆盖在模糊之上的底色不透明度，对应 miuix 示例标题栏的 surface.copy(alpha = 0.8f)。 */
private const val MiuixTopBarTintAlpha = 0.8f

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
 * 与 miuix 标题栏完全一致的模糊层。
 *
 * 参数取自 miuix 示例中包裹标题栏的 `BlurredBar`：`textureBlur`、半径 25dp、
 * 底色 `surface.copy(alpha = 0.8f)`。调用方需要把标题栏下方的滚动内容通过
 * `Modifier.layerBackdrop(backdrop)` 记录进 [backdrop]，并把标题栏自身的 `color` 设为
 * [Color.Transparent]；静止时底色与页面背景一致，滚动后内容从模糊层下透出。
 *
 * @param backdrop 记录标题栏下方滚动内容的背景层。
 * @param blurEnabled 是否启用模糊（不支持 RuntimeShader 或需要节省性能时传 false）。
 * @param alpha 模糊层不透明度，可用于随滚动渐显的标题栏（如「关于」页）。
 */
@Composable
fun BlurredTopBar(
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    modifier: Modifier = Modifier,
    alpha: () -> Float = { 1f },
    content: @Composable () -> Unit,
) {
    val colors = BlurDefaults.blurColors(
        blendColors = listOf(
            BlendColorEntry(MiuixTheme.colorScheme.surface.copy(alpha = MiuixTopBarTintAlpha), BlurBlendMode.SrcOver),
        ),
    )
    Box(modifier) {
        if (blurEnabled && backdrop != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { this.alpha = alpha().coerceIn(0f, 1f) }
                    .textureBlur(
                        backdrop = backdrop,
                        shape = RectangleShape,
                        blurRadius = MiuixTopBarBlurRadius,
                        colors = colors,
                    ),
            )
        }
        content()
    }
}
