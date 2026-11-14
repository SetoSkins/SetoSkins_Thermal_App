package com.setoskins.thermal.ui.component

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange?.let { callback ->
                {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    callback(it)
                }
            },
            enabled = enabled,
            thumbContent = if (checked || enabled) {
                {
                    Icon(
                        imageVector = if (checked) Icons.Filled.Check else Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            } else {
                null
            },
            colors = switchColors,
        )
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
