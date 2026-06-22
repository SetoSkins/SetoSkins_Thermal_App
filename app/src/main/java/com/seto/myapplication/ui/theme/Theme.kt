package com.seto.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun MyApplicationTheme(
    useMonet: Boolean = false,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val controller = remember(useMonet, darkTheme) {
        ThemeController(
            colorSchemeMode = if (useMonet) ColorSchemeMode.MonetSystem else ColorSchemeMode.System,
            isDark = darkTheme
        )
    }
    
    MiuixTheme(
        controller = controller,
        content = content
    )
}
