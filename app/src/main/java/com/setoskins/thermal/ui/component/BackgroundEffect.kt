package com.setoskins.thermal.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
    useMonet: Boolean = false,
) {
    val baseColor = MiuixTheme.colorScheme.surface.copy(alpha = 0.8f)
    val accentColor = MiuixTheme.colorScheme.primary.copy(alpha = 0.15f)

    val transition = rememberInfiniteTransition(label = "bg")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(baseColor, accentColor, baseColor),
                start = Offset(0f, 0f),
                end = Offset(size.width * (0.5f + progress * 0.5f), size.height)
            )
        )
    }
}
