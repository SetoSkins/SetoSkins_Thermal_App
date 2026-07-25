package com.setoskins.thermal.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Switch as MiuixSwitch
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemedSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, enabled: Boolean = true, useMonet: Boolean) {
    val colors = MiuixTheme.colorScheme
    if (useMonet) {
        val isDark = isSystemInDarkTheme()
        val switchColors = SwitchDefaults.colors(
            checkedThumbColor = if (isDark) colors.primaryContainer else Color.White,
            checkedTrackColor = colors.primary,
            checkedIconColor = colors.primary,
            uncheckedThumbColor = ColorSwitchUncheckedThumb,
            uncheckedTrackColor = Color.Transparent,
            uncheckedBorderColor = ColorSwitchUncheckedBorder,
            uncheckedIconColor = Color.White,
            disabledCheckedThumbColor = Color.White.copy(alpha = 0.5f),
            disabledCheckedTrackColor = colors.primary.copy(alpha = 0.3f),
            disabledUncheckedThumbColor = ColorSwitchDisabledUncheckedThumb,
            disabledUncheckedTrackColor = Color.Transparent,
            disabledUncheckedBorderColor = ColorSwitchDisabledUncheckedBorder,
            disabledUncheckedIconColor = Color.White.copy(alpha = 0.7f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled, thumbContent = { Icon(imageVector = if (checked) Icons.Filled.Check else Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }, colors = switchColors, modifier = Modifier.scale(1.02f))
    } else {
        MiuixSwitch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}