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

private const val MiuixBarBlurRadius = 40f

private const val MiuixTopBarBlurRadius = 25f

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
fun BlurredMaterialTopBar(
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    modifier: Modifier = Modifier,
    alpha: () -> Float = { 1f },
    content: @Composable () -> Unit,
) {
    val blendColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)
    Box(modifier) {
        if (blurEnabled && backdrop != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { this.alpha = alpha().coerceIn(0f, 1f) }
                    .textureBlur(
                        backdrop = backdrop,
                        shape = RectangleShape,
                        blurRadius = 25f,
                        colors = BlurColors(
                            blendColors = listOf(BlendColorEntry(color = blendColor)),
                        ),
                    ),
            )
        }
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
