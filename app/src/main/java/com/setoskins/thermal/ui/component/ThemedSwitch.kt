package com.setoskins.thermal.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Switch as MiuixSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemedSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, enabled: Boolean = true, useMonet: Boolean) {
    val hapticFeedback = LocalHapticFeedback.current
    if (useMonet) {
        val colorScheme = MaterialTheme.colorScheme
        val switchColors = SwitchDefaults.colors(
            checkedIconColor = colorScheme.primary,
            uncheckedIconColor = colorScheme.surfaceContainerHighest,
            disabledCheckedThumbColor = colorScheme.surface.copy(alpha = 0.38f),
            disabledCheckedTrackColor = colorScheme.onSurface.copy(alpha = 0.12f),
            disabledCheckedIconColor = colorScheme.onSurface.copy(alpha = 0.12f),
            disabledUncheckedThumbColor = colorScheme.outline.copy(alpha = 0.38f),
            disabledUncheckedTrackColor = colorScheme.surfaceContainerHighest.copy(alpha = 0.12f),
            disabledUncheckedBorderColor = colorScheme.onSurface.copy(alpha = 0.12f),
            disabledUncheckedIconColor = colorScheme.surfaceContainerHighest,
        )

        var isFirstComposition by remember { mutableStateOf(true) }
        var scale by mutableFloatStateOf(1f)
        val animatedScale by animateFloatAsState(
            targetValue = scale,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "SwitchBounce"
        )
        LaunchedEffect(checked) {
            if (isFirstComposition) {
                isFirstComposition = false
                return@LaunchedEffect
            }
            scale = 1.15f
            delay(80)
            scale = 1f
        }

        Box(modifier = Modifier.graphicsLayer { scaleX = animatedScale; scaleY = animatedScale }) {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange?.let { callback ->
                    {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        callback(it)
                    }
                },
                enabled = enabled,
                thumbContent = {
                    Icon(
                        imageVector = if (checked) Icons.Filled.Check else Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                },
                colors = switchColors,
            )
        }
    } else {
        MiuixSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange?.let { callback ->
                {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    callback(it)
                }
            },
            enabled = enabled,
        )
    }
}
